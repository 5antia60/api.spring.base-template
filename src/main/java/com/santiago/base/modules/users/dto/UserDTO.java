package com.santiago.base.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDTO(
        @NotBlank(message = "{validation.user.name.notBlank}")
        @Size(min = 3, max = 100, message = "{validation.user.name.size}")
        String name,

        @NotBlank(message = "{validation.user.email.notBlank}")
        @Email(message = "{validation.user.email.invalid}")
        String email
) {}
