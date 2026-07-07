package com.santiago.base.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank(message = "{validation.auth.refreshToken.notBlank}") String refreshToken
) {}