package com.santiago.base.core.pagination.dto;

public record PaginationMetadataDTO(
        int pageSize,
        int pageNumber,
        int totalPages,
        long totalElements
) {
    public PaginationMetadataDTO {
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize deve ser maior ou igual a 0");
        }

        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber deve ser maior ou igual a 0");
        }

        if (totalPages < 0) {
            throw new IllegalArgumentException("totalPages deve ser maior ou igual a 0");
        }

        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements deve ser maior ou igual a 0");
        }
    }
}
