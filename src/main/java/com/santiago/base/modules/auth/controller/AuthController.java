package com.santiago.base.modules.auth.controller;

import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.auth.dto.AuthResponseDTO;
import com.santiago.base.modules.auth.dto.LoginRequestDTO;
import com.santiago.base.modules.auth.dto.LogoutRequestDTO;
import com.santiago.base.modules.auth.dto.RefreshRequestDTO;
import com.santiago.base.modules.auth.dto.RegisterRequestDTO;
import com.santiago.base.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth routes")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Cadastra um novo usuário", description = "Cria a conta e retorna access + refresh tokens")
    @ApiResponse(responseCode = "201", description = "Usuário criado")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Autentica um usuário", description = "Valida credenciais e retorna access + refresh tokens")
    @ApiResponse(responseCode = "200", description = "Autenticado")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Renova o access token", description = "Recebe um refresh token e retorna novos access + refresh tokens (rotação)")
    @ApiResponse(responseCode = "200", description = "Tokens renovados")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "Encerra a sessão atual", description = "Revoga o refresh token informado")
    @ApiResponse(responseCode = "204", description = "Sessão encerrada")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDTO request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Encerra todas as sessões", description = "Revoga todos os refresh tokens do usuário autenticado")
    @ApiResponse(responseCode = "204", description = "Todas as sessões encerradas")
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserSessionModel requestUser) {
        authService.logoutAll(requestUser);
        return ResponseEntity.noContent().build();
    }
}