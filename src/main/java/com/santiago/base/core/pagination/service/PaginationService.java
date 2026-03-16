package com.santiago.base.core.pagination.service;

import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.pagination.dto.PaginationMetadataDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Function;

@Service
public class PaginationService {

    private static final int MAX_PAGE_SIZE = 999;

    public <T, R> PaginatedResponseDTO<R> build(Page<T> page, Function<T, R> mapper) {
        if (page.getSize() > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível realizar uma busca acima do limite máximo de itens por página"
            );
        }

        return new PaginatedResponseDTO<>(
                page.getContent().stream().map(mapper).toList(),
                new PaginationMetadataDTO(
                        page.getSize(),
                        page.getNumber(),
                        page.getTotalPages(),
                        page.getTotalElements()
                )
        );
    }
}