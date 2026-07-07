package com.santiago.base.modules.auth.refresh.service;

import com.santiago.base.modules.auth.refresh.entity.RefreshToken;
import com.santiago.base.modules.auth.refresh.repository.RefreshTokenRepository;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.santiago.base.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMillis", 604800000L);
    }

    @Nested
    @DisplayName("createRefreshToken")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("should persist token with hashed value, new family and expiry")
        void shouldPersistHashedTokenWithNewFamily() {
            User user = user(1L, UserRole.USER);

            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            String rawToken = refreshTokenService.createRefreshToken(user);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            RefreshToken saved = captor.getValue();

            assertThat(rawToken).isNotBlank();
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.getTokenHash()).isNotEqualTo(rawToken);
            assertThat(saved.getTokenHash()).hasSize(64);
            assertThat(saved.getFamily()).isNotNull();
            assertThat(saved.getExpiresAt()).isAfter(Instant.now());
            assertThat(saved.getRevokedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("rotate")
    class RotateTests {

        @Test
        @DisplayName("should return new raw token and user when valid")
        void shouldRotateValidToken() {
            User user = user(1L, UserRole.USER);
            String rawToken = "raw-token-123";
            String hash = sha256Hex(rawToken);

            RefreshToken existing = new RefreshToken();
            existing.setId(10L);
            existing.setUser(user);
            existing.setTokenHash(hash);
            existing.setFamily(UUID.randomUUID());
            existing.setExpiresAt(Instant.now().plusSeconds(3600));

            when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(existing));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
                RefreshToken t = invocation.getArgument(0);
                if (t.getId() == null) t.setId(99L);
                return t;
            });

            RefreshTokenService.RotationResult result = refreshTokenService.rotate(rawToken);

            assertThat(result.user()).isSameAs(user);
            assertThat(result.newRawToken()).isNotBlank();
            assertThat(existing.getRevokedAt()).isNotNull();
            assertThat(existing.getReplacedById()).isEqualTo(99L);
        }

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token not found")
        void shouldThrowWhenNotFound() {
            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.rotate("missing"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("should revoke entire family when token is expired")
        void shouldRevokeFamilyOnExpiry() {
            User user = user(1L, UserRole.USER);
            RefreshToken expired = new RefreshToken();
            expired.setId(1L);
            expired.setUser(user);
            expired.setFamily(UUID.randomUUID());
            expired.setExpiresAt(Instant.now().minusSeconds(60));

            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));
            when(refreshTokenRepository.revokeFamily(expired.getFamily())).thenReturn(1);

            assertThatThrownBy(() -> refreshTokenService.rotate("expired"))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessage("auth.refreshToken.expired");

            verify(refreshTokenRepository).revokeFamily(expired.getFamily());
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("should revoke entire family and throw ReuseException when revoked token is reused")
        void shouldRevokeFamilyAndThrowOnReuse() {
            User user = user(1L, UserRole.USER);
            RefreshToken alreadyRevoked = new RefreshToken();
            alreadyRevoked.setId(1L);
            alreadyRevoked.setUser(user);
            alreadyRevoked.setFamily(UUID.randomUUID());
            alreadyRevoked.setExpiresAt(Instant.now().plusSeconds(3600));
            alreadyRevoked.setRevokedAt(Instant.now());

            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(alreadyRevoked));
            when(refreshTokenRepository.revokeFamily(alreadyRevoked.getFamily())).thenReturn(1);

            assertThatThrownBy(() -> refreshTokenService.rotate("reused"))
                    .isInstanceOf(RefreshTokenReuseException.class)
                    .hasMessage("auth.refreshToken.reuseDetected");

            verify(refreshTokenRepository).revokeFamily(alreadyRevoked.getFamily());
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("revoke")
    class RevokeTests {

        @Test
        @DisplayName("should mark active token as revoked")
        void shouldMarkActiveTokenRevoked() {
            RefreshToken token = new RefreshToken();
            token.setId(1L);
            token.setTokenHash("hash");
            token.setExpiresAt(Instant.now().plusSeconds(60));
            when(refreshTokenRepository.findByTokenHash(sha256Hex("raw"))).thenReturn(Optional.of(token));
            when(refreshTokenRepository.save(token)).thenReturn(token);

            refreshTokenService.revoke("raw");

            assertThat(token.getRevokedAt()).isNotNull();
            verify(refreshTokenRepository).save(token);
        }

        @Test
        @DisplayName("should be idempotent when token already revoked")
        void shouldBeIdempotent() {
            RefreshToken token = new RefreshToken();
            token.setRevokedAt(Instant.now());
            when(refreshTokenRepository.findByTokenHash(sha256Hex("raw"))).thenReturn(Optional.of(token));

            refreshTokenService.revoke("raw");

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("should do nothing when token not found")
        void shouldDoNothingWhenNotFound() {
            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

            refreshTokenService.revoke("unknown");

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("revokeAllForUser")
    class RevokeAllForUserTests {

        @Test
        @DisplayName("should delegate to repository with userId")
        void shouldDelegateToRepository() {
            when(refreshTokenRepository.revokeAllForUser(7L)).thenReturn(3);

            int count = refreshTokenService.revokeAllForUser(7L);

            assertThat(count).isEqualTo(3);
            verify(refreshTokenRepository).revokeAllForUser(7L);
        }
    }

    private static String sha256Hex(String raw) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}