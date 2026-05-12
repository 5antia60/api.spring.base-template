package com.santiago.base.modules.tasks.service;

import com.santiago.base.core.exceptions.ResourceNotFoundException;
import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.pagination.dto.PaginationMetadataDTO;
import com.santiago.base.core.pagination.service.PaginationService;
import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.ResponseTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.modules.tasks.model.TaskStatus;
import com.santiago.base.modules.tasks.repository.TaskRepository;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import com.santiago.base.modules.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.santiago.base.support.TestFixtures.createTaskDto;
import static com.santiago.base.support.TestFixtures.session;
import static com.santiago.base.support.TestFixtures.task;
import static com.santiago.base.support.TestFixtures.updateTaskDto;
import static com.santiago.base.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaginationService paginationService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void baseFindAllShouldFindAllTasksWhenRequestUserIsAdminAndUserIdIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskPage(pageable);
        PaginatedResponseDTO<ResponseTaskDTO> expected = paginatedTaskResponse();
        when(taskRepository.findAll(pageable)).thenReturn(page);
        doReturn(expected).when(paginationService).build(eq(page), any(Function.class));

        PaginatedResponseDTO<ResponseTaskDTO> response = taskService.baseFindAll(null, pageable, session(99L, UserRole.ADMIN));

        assertThat(response).isSameAs(expected);
        verify(taskRepository).findAll(pageable);
        verify(taskRepository, never()).findByUserId(any(), any());
    }

    @Test
    void baseFindAllShouldFindByProvidedUserIdEvenWhenRequestUserIsAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskPage(pageable);
        PaginatedResponseDTO<ResponseTaskDTO> expected = paginatedTaskResponse();
        when(taskRepository.findByUserId(1L, pageable)).thenReturn(page);
        doReturn(expected).when(paginationService).build(eq(page), any(Function.class));

        PaginatedResponseDTO<ResponseTaskDTO> response = taskService.baseFindAll(1L, pageable, session(99L, UserRole.ADMIN));

        assertThat(response).isSameAs(expected);
        verify(taskRepository).findByUserId(1L, pageable);
    }

    @Test
    void baseFindAllShouldFindByRequestUserIdWhenRequestUserIsNotAdminAndUserIdIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = taskPage(pageable);
        PaginatedResponseDTO<ResponseTaskDTO> expected = paginatedTaskResponse();
        when(taskRepository.findByUserId(1L, pageable)).thenReturn(page);
        doReturn(expected).when(paginationService).build(eq(page), any(Function.class));

        PaginatedResponseDTO<ResponseTaskDTO> response = taskService.baseFindAll(null, pageable, session(1L, UserRole.USER));

        assertThat(response).isSameAs(expected);
        verify(taskRepository).findByUserId(1L, pageable);
    }

    @Test
    void findByIdShouldReturnTask() {
        Task task = task(1L, user(1L, UserRole.USER));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        ResponseTaskDTO response = taskService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void findByIdShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void createShouldSaveTaskForExistingUser() {
        User user = user(1L, UserRole.USER);
        CreateTaskDTO dto = createTaskDto(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(10L);
            saved.setCreatedAt(user.getCreatedAt());
            saved.setUpdatedAt(user.getUpdatedAt());
            return saved;
        });

        CreateTaskDTO response = taskService.create(dto);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo(user.getName());
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getUser()).isSameAs(user);
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void createShouldDefaultStatusToPendingWhenStatusIsNull() {
        User user = user(1L, UserRole.USER);
        CreateTaskDTO dto = createTaskDto(1L);
        dto.setStatus(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.create(dto);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void createShouldThrowWhenUserDoesNotExist() {
        CreateTaskDTO dto = createTaskDto(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(taskRepository);
    }

    @Test
    void updateShouldUpdateExistingTask() {
        Task existing = task(1L, user(1L, UserRole.USER));
        CreateTaskDTO dto = createTaskDto(1L);
        dto.setTitle("Updated title");
        dto.setDescription("Updated description");
        dto.setStatus(TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        ResponseTaskDTO response = taskService.update(1L, dto);

        assertThat(response.getTitle()).isEqualTo("Updated title");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(taskRepository).save(existing);
    }

    @Test
    void updateShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(1L, createTaskDto(1L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void partialUpdateShouldApplyOnlyPresentFields() {
        Task existing = task(1L, user(1L, UserRole.USER));
        String originalDescription = existing.getDescription();
        UpdateTaskDTO dto = updateTaskDto("Partial title", null, TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        ResponseTaskDTO response = taskService.partialUpdate(1L, dto);

        assertThat(response.getTitle()).isEqualTo("Partial title");
        assertThat(response.getDescription()).isEqualTo(originalDescription);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(taskRepository).save(existing);
    }

    @Test
    void partialUpdateShouldSaveTaskEvenWhenNoFieldIsPresent() {
        Task existing = task(1L, user(1L, UserRole.USER));
        UpdateTaskDTO dto = updateTaskDto(null, null, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        ResponseTaskDTO response = taskService.partialUpdate(1L, dto);

        assertThat(response.getTitle()).isEqualTo(existing.getTitle());
        verify(taskRepository).save(existing);
    }

    @Test
    void partialUpdateShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.partialUpdate(1L, updateTaskDto("Title", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activateShouldActivateTaskWhenRequestUserIsAdmin() {
        Task existing = task(1L, user(1L, UserRole.USER));
        existing.setIsActive(false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        ResponseTaskDTO response = taskService.activate(1L, session(99L, UserRole.ADMIN));

        assertThat(response.getIsActive()).isTrue();
        verify(taskRepository).save(existing);
    }

    @Test
    void activateShouldDenyWhenRequestUserIsNotAdmin() {
        assertThatThrownBy(() -> taskService.activate(1L, session(1L, UserRole.USER)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(taskRepository);
    }

    @Test
    void deactivateShouldDeactivateTaskWhenRequestUserIsAdmin() {
        Task existing = task(1L, user(1L, UserRole.USER));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        ResponseTaskDTO response = taskService.deactivate(1L, session(99L, UserRole.ADMIN));

        assertThat(response.getIsActive()).isFalse();
        verify(taskRepository).save(existing);
    }

    @Test
    void deactivateShouldDenyWhenRequestUserIsNotAdmin() {
        assertThatThrownBy(() -> taskService.deactivate(1L, session(1L, UserRole.USER)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(taskRepository);
    }

    @Test
    void deleteShouldDeleteExistingTask() {
        Task existing = task(1L, user(1L, UserRole.USER));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        taskService.delete(1L);

        verify(taskRepository).delete(existing);
    }

    @Test
    void deleteShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Page<Task> taskPage(Pageable pageable) {
        return new PageImpl<>(List.of(task(1L, user(1L, UserRole.USER))), pageable, 1);
    }

    private static PaginatedResponseDTO<ResponseTaskDTO> paginatedTaskResponse() {
        return new PaginatedResponseDTO<>(
                List.of(new ResponseTaskDTO(task(1L, user(1L, UserRole.USER)))),
                new PaginationMetadataDTO(10, 0, 1, 1)
        );
    }
}
