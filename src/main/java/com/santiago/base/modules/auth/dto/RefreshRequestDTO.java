package com.santiago.base.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank(message = "Refresh token é obrigatório") String refreshToken
) {}