package com.santiago.base.core.pagination.dto;

public record PaginationMetadataDTO(
        int pageSize,
        int pageNumber,
        int totalPages,
        long totalElements
) {
}