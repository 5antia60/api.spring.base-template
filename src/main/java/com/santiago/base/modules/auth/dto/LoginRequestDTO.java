package com.santiago.base.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "{validation.auth.email.notBlank}") @Email(message = "{validation.auth.email.invalid}") String email,
        @NotBlank(message = "{validation.auth.password.notBlank}") String password
) {}
