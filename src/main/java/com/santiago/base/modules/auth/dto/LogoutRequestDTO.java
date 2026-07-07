package com.santiago.base.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(
        @NotBlank(message = "{validation.auth.refreshToken.notBlank}") String refreshToken
) {}