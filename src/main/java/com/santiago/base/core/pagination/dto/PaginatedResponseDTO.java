package com.santiago.base.core.pagination.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

public record PaginatedResponseDTO<T>(
        List<T> content,
        @JsonUnwrapped PaginationMetadataDTO pagination
) {}
