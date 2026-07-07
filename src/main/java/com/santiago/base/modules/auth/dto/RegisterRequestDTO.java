package com.santiago.base.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "{validation.auth.name.notBlank}") @Size(min = 3, max = 100, message = "{validation.auth.name.size}") String name,
        @NotBlank(message = "{validation.auth.email.notBlank}") @Email(message = "{validation.auth.email.invalid}") String email,
        @NotBlank(message = "{validation.auth.password.notBlank}") @Size(min = 6, message = "{validation.auth.password.size}") String password
) {}
