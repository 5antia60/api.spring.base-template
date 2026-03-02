package com.santiago.base.modules.auth.dto;

import com.santiago.base.modules.users.model.UserRole;

public record AuthResponseDTO(
        String token,
        String email,
        String name,
        UserRole role
) {}
