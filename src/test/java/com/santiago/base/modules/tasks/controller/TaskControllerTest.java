package com.santiago.base.modules.tasks.controller;

import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.pagination.dto.PaginationMetadataDTO;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.ResponseTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.modules.tasks.model.TaskStatus;
import com.santiago.base.modules.tasks.service.TaskService;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.santiago.base.support.TestFixtures.createTaskDto;
import static com.santiago.base.support.TestFixtures.session;
import static com.santiago.base.support.TestFixtures.task;
import static com.santiago.base.support.TestFixtures.updateTaskDto;
import static com.santiago.base.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @Test
    void findAllShouldReturnOkWithPaginatedTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSessionModel requestUser = session(1L, UserRole.USER);
        PaginatedResponseDTO<ResponseTaskDTO> expected = paginatedTask();
        when(taskService.baseFindAll(1L, pageable, requestUser)).thenReturn(expected);

        ResponseEntity<PaginatedResponseDTO<ResponseTaskDTO>> response = taskController.findAll(1L, pageable, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(taskService).baseFindAll(1L, pageable, requestUser);
    }

    @Test
    void findByIdShouldReturnOkWithTask() {
        ResponseTaskDTO expected = new ResponseTaskDTO(task(1L, user(1L, UserRole.USER)));
        when(taskService.findById(1L)).thenReturn(expected);

        ResponseEntity<ResponseTaskDTO> response = taskController.findById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(taskService).findById(1L);
    }

    @Test
    void createShouldReturnCreatedWithCreatedTask() {
        CreateTaskDTO dto = createTaskDto(1L);
        CreateTaskDTO expected = createTaskDto(1L);
        expected.setId(10L);
        when(taskService.create(dto)).thenReturn(expected);

        ResponseEntity<CreateTaskDTO> response = taskController.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expected);
        verify(taskService).create(dto);
    }

    @Test
    void updateShouldReturnOkWithUpdatedTask() {
        CreateTaskDTO dto = createTaskDto(1L);
        ResponseTaskDTO expected = new ResponseTaskDTO(task(1L, user(1L, UserRole.USER)));
        when(taskService.update(1L, dto)).thenReturn(expected);

        ResponseEntity<ResponseTaskDTO> response = taskController.update(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(taskService).update(1L, dto);
    }

    @Test
    void partialUpdateShouldReturnOkWithUpdatedTask() {
        UpdateTaskDTO dto = updateTaskDto("Updated", null, TaskStatus.COMPLETED);
        ResponseTaskDTO expected = new ResponseTaskDTO(task(1L, user(1L, UserRole.USER)));
        when(taskService.partialUpdate(1L, dto)).thenReturn(expected);

        ResponseEntity<ResponseTaskDTO> response = taskController.partialUpdate(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(taskService).partialUpdate(1L, dto);
    }

    @Test
    void activateShouldReturnOkWithActivatedTask() {
        UserSessionModel requestUser = session(99L, UserRole.ADMIN);
        ResponseTaskDTO expected = new ResponseTaskDTO(task(1L, user(1L, UserRole.USER)));
        when(taskService.activate(1L, requestUser)).thenReturn(expected);

        ResponseEntity<ResponseTaskDTO> response = taskController.activate(1L, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(taskService).activate(1L, requestUser);
    }

    @Test
    void deactivateShouldReturnOkWithDeactivatedTask() {
        UserSessionModel requestUser = session(99L, UserRole.ADMIN);
        ResponseTaskDTO expected = new ResponseTaskDTO(task(1L, user(1L, UserRole.USER)));
        when(taskService.deactivate(1L, requestUser)).thenReturn(expected);

        ResponseEntity<ResponseTaskDTO> response = taskController.deactivate(1L, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(taskService).deactivate(1L, requestUser);
    }

    @Test
    void deleteShouldReturnNoContent() {
        ResponseEntity<Void> response = taskController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(taskService).delete(1L);
    }

    private static PaginatedResponseDTO<ResponseTaskDTO> paginatedTask() {
        User user = user(1L, UserRole.USER);
        return new PaginatedResponseDTO<>(
                List.of(new ResponseTaskDTO(task(1L, user))),
                new PaginationMetadataDTO(10, 0, 1, 1)
        );
    }
}
