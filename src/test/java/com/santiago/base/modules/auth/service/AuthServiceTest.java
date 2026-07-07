package com.santiago.base.modules.auth.service;

import com.santiago.base.core.exceptions.BusinessException;
import com.santiago.base.core.security.JwtService;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.auth.dto.AuthResponseDTO;
import com.santiago.base.modules.auth.dto.LoginRequestDTO;
import com.santiago.base.modules.auth.dto.RegisterRequestDTO;
import com.santiago.base.modules.auth.refresh.service.InvalidRefreshTokenException;
import com.santiago.base.modules.auth.refresh.service.RefreshTokenService;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import com.santiago.base.modules.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.santiago.base.support.TestFixtures.loginDto;
import static com.santiago.base.support.TestFixtures.registerDto;
import static com.santiago.base.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("should persist user and return access + refresh tokens")
        void shouldCreateUserAndReturnBothTokens() {
            RegisterRequestDTO dto = registerDto("Santiago", "santiago@example.com", "secret123");
            when(userRepository.existsByEmail("santiago@example.com")).thenReturn(false);
            when(passwordEncoder.encode("secret123")).thenReturn("encoded");
            when(jwtService.generateAccessToken(any(UserSessionModel.class))).thenReturn("access-token");
            when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

            AuthResponseDTO response = authService.register(dto);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertThat(saved.getName()).isEqualTo("Santiago");
            assertThat(saved.getPassword()).isEqualTo("encoded");
            assertThat(saved.getRole()).isEqualTo(UserRole.USER);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.email()).isEqualTo("santiago@example.com");
        }

        @Test
        @DisplayName("should throw BusinessException when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {
            RegisterRequestDTO dto = registerDto("Santiago", "dup@example.com", "secret123");
            when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("dup@example.com");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("should authenticate and return access + refresh tokens")
        void shouldAuthenticateAndReturnTokens() {
            User user = user(1L, UserRole.USER);
            LoginRequestDTO dto = loginDto("user1@example.com", "secret123");
            when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(any(UserSessionModel.class))).thenReturn("access-token");
            when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh-token");

            AuthResponseDTO response = authService.login(dto);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.email()).isEqualTo(user.getEmail());
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTests {

        @Test
        @DisplayName("should rotate token and return new access + refresh")
        void shouldRotateAndReturnNewTokens() {
            User user = user(1L, UserRole.USER);
            RefreshTokenService.RotationResult rotation =
                    new RefreshTokenService.RotationResult("new-refresh", user);
            when(refreshTokenService.rotate("old-refresh")).thenReturn(rotation);
            when(jwtService.generateAccessToken(any(UserSessionModel.class))).thenReturn("new-access");

            AuthResponseDTO response = authService.refresh("old-refresh");

            verify(refreshTokenService).rotate("old-refresh");
            assertThat(response.accessToken()).isEqualTo("new-access");
            assertThat(response.refreshToken()).isEqualTo("new-refresh");
            assertThat(response.email()).isEqualTo(user.getEmail());
        }

        @Test
        @DisplayName("should propagate InvalidRefreshTokenException from rotation")
        void shouldPropagateInvalidRefreshToken() {
            when(refreshTokenService.rotate("bad")).thenThrow(new InvalidRefreshTokenException("invalid"));

            assertThatThrownBy(() -> authService.refresh("bad"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        @DisplayName("should revoke the refresh token")
        void shouldRevokeRefreshToken() {
            authService.logout("some-refresh");

            verify(refreshTokenService).revoke("some-refresh");
        }

        @Test
        @DisplayName("should ignore InvalidRefreshTokenException during logout (idempotent)")
        void shouldBeIdempotentWhenTokenInvalid() {
            doThrow(new InvalidRefreshTokenException("invalid"))
                    .when(refreshTokenService).revoke("bad");

            authService.logout("bad");
        }

        @Test
        @DisplayName("should do nothing when token is null or blank")
        void shouldDoNothingWhenTokenIsBlank() {
            authService.logout(null);
            authService.logout("  ");

            verify(refreshTokenService, never()).revoke(any());
        }
    }

    @Nested
    @DisplayName("logoutAll")
    class LogoutAllTests {

        @Test
        @DisplayName("should revoke all tokens for the user")
        void shouldRevokeAllForUser() {
            UserSessionModel requestUser = new UserSessionModel(user(7L, UserRole.USER));

            authService.logoutAll(requestUser);

            verify(refreshTokenService).revokeAllForUser(7L);
        }
    }
}