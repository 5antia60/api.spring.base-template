package com.santiago.base.core.pagination.service;

import com.santiago.base.core.exceptions.BusinessException;
import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationServiceTest {

    private final PaginationService paginationService = new PaginationService();

    @Test
    void buildShouldMapContentAndMetadata() {
        Page<Integer> page = new PageImpl<>(List.of(1, 2), PageRequest.of(1, 2), 5);

        PaginatedResponseDTO<String> response = paginationService.build(page, item -> "item-" + item);

        assertThat(response.content()).containsExactly("item-1", "item-2");
        assertThat(response.pagination().pageSize()).isEqualTo(2);
        assertThat(response.pagination().pageNumber()).isEqualTo(1);
        assertThat(response.pagination().totalPages()).isEqualTo(3);
        assertThat(response.pagination().totalElements()).isEqualTo(5);
    }

    @Test
    void buildShouldRejectPagesAboveMaximumSize() {
        Page<Integer> page = new PageImpl<>(List.of(), PageRequest.of(0, 1000), 0);

        assertThatThrownBy(() -> paginationService.build(page, Object::toString))
                .isInstanceOf(BusinessException.class);
    }
}
