package com.santiago.base.modules.auth.controller;

import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.auth.dto.AuthResponseDTO;
import com.santiago.base.modules.auth.dto.LoginRequestDTO;
import com.santiago.base.modules.auth.dto.LogoutRequestDTO;
import com.santiago.base.modules.auth.dto.RefreshRequestDTO;
import com.santiago.base.modules.auth.dto.RegisterRequestDTO;
import com.santiago.base.modules.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.santiago.base.support.TestFixtures.loginDto;
import static com.santiago.base.support.TestFixtures.logoutDto;
import static com.santiago.base.support.TestFixtures.refreshDto;
import static com.santiago.base.support.TestFixtures.registerDto;
import static com.santiago.base.support.TestFixtures.session;
import static com.santiago.base.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("should return 201 with auth response")
        void shouldReturnCreated() {
            RegisterRequestDTO dto = registerDto("Santiago", "santiago@example.com", "secret123");
            AuthResponseDTO expected = new AuthResponseDTO("access", "refresh", "santiago@example.com", "Santiago", user(1L, com.santiago.base.modules.users.model.UserRole.USER).getRole());
            when(authService.register(dto)).thenReturn(expected);

            ResponseEntity<AuthResponseDTO> response = authController.register(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(expected);
            verify(authService).register(dto);
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("should return 200 with auth response")
        void shouldReturnOk() {
            LoginRequestDTO dto = loginDto("santiago@example.com", "secret123");
            AuthResponseDTO expected = new AuthResponseDTO("access", "refresh", "santiago@example.com", "Santiago", com.santiago.base.modules.users.model.UserRole.USER);
            when(authService.login(dto)).thenReturn(expected);

            ResponseEntity<AuthResponseDTO> response = authController.login(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
            verify(authService).login(dto);
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTests {

        @Test
        @DisplayName("should return 200 with new tokens")
        void shouldReturnOkWithNewTokens() {
            RefreshRequestDTO dto = refreshDto("old-refresh");
            AuthResponseDTO expected = new AuthResponseDTO("new-access", "new-refresh", "santiago@example.com", "Santiago", com.santiago.base.modules.users.model.UserRole.USER);
            when(authService.refresh("old-refresh")).thenReturn(expected);

            ResponseEntity<AuthResponseDTO> response = authController.refresh(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(expected);
            verify(authService).refresh("old-refresh");
        }
    }

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        @DisplayName("should return 204 and call logout")
        void shouldReturnNoContent() {
            LogoutRequestDTO dto = logoutDto("some-refresh");

            ResponseEntity<Void> response = authController.logout(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
            verify(authService).logout("some-refresh");
        }
    }

    @Nested
    @DisplayName("logoutAll")
    class LogoutAllTests {

        @Test
        @DisplayName("should return 204 and revoke all for authenticated user")
        void shouldReturnNoContentAndRevokeAll() {
            UserSessionModel requestUser = session(7L, com.santiago.base.modules.users.model.UserRole.USER);

            ResponseEntity<Void> response = authController.logoutAll(requestUser);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(authService).logoutAll(requestUser);
        }
    }
}