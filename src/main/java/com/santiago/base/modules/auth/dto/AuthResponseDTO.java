package com.santiago.base.modules.auth.dto;

public record AuthResponseDTO(
        String token,
        String email,
        String name
) {}
