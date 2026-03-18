package com.santiago.base.core.base;

import lombok.Data;

import java.time.Instant;

@Data
public class BaseResponseDTO {
    public BaseResponseDTO(
            Long id,
            Boolean isActive,
            Instant createdAt,
            Instant updatedAt
    ) {
        setId(id);
        setIsActive(isActive);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    private Long id;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;
}
