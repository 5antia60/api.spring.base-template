package com.santiago.base.modules.auth.refresh.service;

import com.santiago.base.modules.auth.refresh.entity.RefreshToken;
import com.santiago.base.modules.auth.refresh.repository.RefreshTokenRepository;
import com.santiago.base.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    public record RotationResult(String newRawToken, User user) {}

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshExpirationMillis;

    @Transactional
    public String createRefreshToken(User user) {
        return persistNewToken(user, UUID.randomUUID(), generateRawToken());
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido ou expirado."));

        if (existing.isExpired()) {
            revokeFamily(existing.getFamily());
            throw new InvalidRefreshTokenException("Refresh token expirado. Faça login novamente.");
        }

        if (existing.isRevoked()) {
            revokeFamily(existing.getFamily());
            throw new RefreshTokenReuseException("Reuso de refresh token detectado. Todas as sessões foram revogadas.");
        }

        String newRawToken = generateRawToken();
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(existing.getUser());
        newToken.setTokenHash(sha256(newRawToken));
        newToken.setFamily(existing.getFamily());
        newToken.setExpiresAt(Instant.now().plus(Duration.ofMillis(refreshExpirationMillis)));
        refreshTokenRepository.save(newToken);

        existing.setRevokedAt(Instant.now());
        existing.setReplacedById(newToken.getId());
        refreshTokenRepository.save(existing);

        return new RotationResult(newRawToken, existing.getUser());
    }

    @Transactional
    public void revoke(String rawToken) {
        String hash = sha256(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    @Transactional
    public void revokeFamily(UUID family) {
        refreshTokenRepository.revokeFamily(family);
    }

    @Transactional
    public int revokeAllForUser(Long userId) {
        return refreshTokenRepository.revokeAllForUser(userId);
    }

    private String persistNewToken(User user, UUID family, String rawToken) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(sha256(rawToken));
        token.setFamily(family);
        token.setExpiresAt(Instant.now().plus(Duration.ofMillis(refreshExpirationMillis)));
        refreshTokenRepository.save(token);
        return rawToken;
    }

    private String generateRawToken() {
        return UUID.randomUUID().toString();
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível no ambiente.", ex);
        }
    }
}