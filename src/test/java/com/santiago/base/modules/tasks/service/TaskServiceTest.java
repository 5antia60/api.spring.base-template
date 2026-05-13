package com.santiago.base.modules.tasks.service;

import com.santiago.base.core.event.DomainEvent;
import com.santiago.base.core.event.EventType;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.santiago.base.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService")
@SuppressWarnings({"unchecked", "rawtypes"})
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaginationService paginationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    @Nested
    @DisplayName("baseFindAll")
    class BaseFindAllTests {

        @Nested
        @DisplayName("when user is ADMIN and userId is null")
        class AdminWithoutUserIdTests {

            @Test
            @DisplayName("should find all tasks")
            void shouldFindAllTasks() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Task> taskPage = taskPage(pageable);
                PaginatedResponseDTO<ResponseTaskDTO> expectedResponse = paginatedTaskResponse();

                when(taskRepository.findAll(pageable)).thenReturn(taskPage);
                doReturn(expectedResponse).when(paginationService).build(eq(taskPage), any(Function.class));

                PaginatedResponseDTO<ResponseTaskDTO> response = taskService.baseFindAll(
                        null,
                        pageable,
                        session(99L, UserRole.ADMIN)
                );

                assertThat(response).isSameAs(expectedResponse);
                verify(taskRepository).findAll(pageable);
                verify(taskRepository, never()).findByUserId(any(), any());
            }
        }

        @Nested
        @DisplayName("when user is ADMIN with specific userId")
        class AdminWithUserIdTests {

            @Test
            @DisplayName("should find tasks for the specified user")
            void shouldFindTasksByProvidedUserId() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Task> taskPage = taskPage(pageable);
                PaginatedResponseDTO<ResponseTaskDTO> expectedResponse = paginatedTaskResponse();

                when(taskRepository.findByUserId(1L, pageable)).thenReturn(taskPage);
                doReturn(expectedResponse).when(paginationService).build(eq(taskPage), any(Function.class));

                PaginatedResponseDTO<ResponseTaskDTO> response = taskService.baseFindAll(
                        1L,
                        pageable,
                        session(99L, UserRole.ADMIN)
                );

                assertThat(response).isSameAs(expectedResponse);
                verify(taskRepository).findByUserId(1L, pageable);
                verify(taskRepository, never()).findAll(any(Pageable.class));
            }
        }

        @Nested
        @DisplayName("when user is USER (non-admin)")
        class NonAdminUserTests {

            @Test
            @DisplayName("should find only tasks for the authenticated user")
            void shouldFindTasksByAuthenticatedUserId() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<Task> taskPage = taskPage(pageable);
                PaginatedResponseDTO<ResponseTaskDTO> expectedResponse = paginatedTaskResponse();

                when(taskRepository.findByUserId(1L, pageable)).thenReturn(taskPage);
                doReturn(expectedResponse).when(paginationService).build(eq(taskPage), any(Function.class));

                PaginatedResponseDTO<ResponseTaskDTO> response = taskService.baseFindAll(
                        null,
                        pageable,
                        session(1L, UserRole.USER)
                );

                assertThat(response).isSameAs(expectedResponse);
                verify(taskRepository).findByUserId(1L, pageable);
            }
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("should return task when it exists")
        void shouldReturnTaskWhenFound() {
            Task task = task(1L, user(1L, UserRole.USER));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            ResponseTaskDTO response = taskService.findById(1L);

            assertThat(response)
                    .isNotNull()
                    .satisfies(dto -> {
                        assertThat(dto.getId()).isEqualTo(1L);
                        assertThat(dto.getUserId()).isEqualTo(1L);
                    });
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when task does not exist")
        void shouldThrowResourceNotFoundExceptionWhenTaskDoesNotExist() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("1");
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Nested
        @DisplayName("successful creation behavior")
        class SuccessfulCreationTests {

            @Test
            @DisplayName("should create task with all correct data")
            void shouldCreateTaskWithCorrectData() {
                User user = user(1L, UserRole.USER);
                CreateTaskDTO dto = createTaskDto(1L);
                dto.setTitle("Test Task");
                dto.setDescription("Test Description");

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                    Task saved = invocation.getArgument(0);
                    saved.setId(10L);
                    saved.setCreatedAt(user.getCreatedAt());
                    saved.setUpdatedAt(user.getUpdatedAt());
                    return saved;
                });

                CreateTaskDTO response = taskService.create(dto);

                assertThat(response)
                        .isNotNull()
                        .satisfies(created -> {
                            assertThat(created.getId()).isEqualTo(10L);
                            assertThat(created.getUserId()).isEqualTo(1L);
                            assertThat(created.getUserName()).isEqualTo(user.getName());
                            assertThat(created.getTitle()).isEqualTo("Test Task");
                        });
            }

            @Test
            @DisplayName("should persist task with correct user")
            void shouldPersistTaskWithCorrectUser() {
                User user = user(1L, UserRole.USER);
                CreateTaskDTO dto = createTaskDto(1L);

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                    Task saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

                taskService.create(dto);

                ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
                verify(taskRepository).save(taskCaptor.capture());

                Task capturedTask = taskCaptor.getValue();
                assertThat(capturedTask.getUser()).isSameAs(user);
            }

            @Test
            @DisplayName("should apply IN_PROGRESS status when provided")
            void shouldApplyProvidedStatus() {
                User user = user(1L, UserRole.USER);
                CreateTaskDTO dto = createTaskDto(1L);
                dto.setStatus(TaskStatus.IN_PROGRESS);

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

                taskService.create(dto);

                ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
                verify(taskRepository).save(taskCaptor.capture());
                assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            }

            @Test
            @DisplayName("should default status to PENDING when null")
            void shouldDefaultStatusToPendingWhenNull() {
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
        }

        @Nested
        @DisplayName("event publishing")
        class EventPublisherTests {

            @Test
            @DisplayName("should publish CREATED_TASK event after creation")
            void shouldPublishCreatedTaskEvent() {
                User user = user(1L, UserRole.USER);
                CreateTaskDTO dto = createTaskDto(1L);

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                    Task saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

                taskService.create(dto);

                ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                DomainEvent event = eventCaptor.getValue();
                assertThat(event.type()).isEqualTo(EventType.CREATED_TASK);
            }

            @Test
            @DisplayName("should publish event with created task as payload")
            void shouldPublishEventWithCreatedTaskPayload() {
                User user = user(1L, UserRole.USER);
                CreateTaskDTO dto = createTaskDto(1L);

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                    Task saved = invocation.getArgument(0);
                    saved.setId(10L);
                    saved.setTitle(dto.getTitle());
                    return saved;
                });

                taskService.create(dto);

                ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                DomainEvent event = eventCaptor.getValue();
                Task eventPayload = (Task) event.payload();
                assertThat(eventPayload.getId()).isEqualTo(10L);
                assertThat(eventPayload.getTitle()).isEqualTo(dto.getTitle());
            }

            @Test
            @DisplayName("should not publish event when user does not exist")
            void shouldNotPublishEventWhenUserDoesNotExist() {
                CreateTaskDTO dto = createTaskDto(1L);
                when(userRepository.findById(1L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.create(dto))
                        .isInstanceOf(ResourceNotFoundException.class);

                verify(eventPublisher, never()).publishEvent(any());
            }
        }

        @Nested
        @DisplayName("error handling")
        class ErrorHandlingTests {

            @Test
            @DisplayName("should throw ResourceNotFoundException when user does not exist")
            void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
                CreateTaskDTO dto = createTaskDto(1L);
                when(userRepository.findById(1L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.create(dto))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("1");
            }

            @Test
            @DisplayName("should not save task when user does not exist")
            void shouldNotSaveTaskWhenUserDoesNotExist() {
                CreateTaskDTO dto = createTaskDto(1L);
                when(userRepository.findById(1L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.create(dto))
                        .isInstanceOf(ResourceNotFoundException.class);

                verifyNoInteractions(taskRepository);
            }
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("should update all provided fields")
        void shouldUpdateAllProvidedFields() {
            Task existing = task(1L, user(1L, UserRole.USER));
            CreateTaskDTO dto = createTaskDto(1L);
            dto.setTitle("Updated title");
            dto.setDescription("Updated description");
            dto.setStatus(TaskStatus.COMPLETED);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            ResponseTaskDTO response = taskService.update(1L, dto);

            assertThat(response)
                    .satisfies(updated -> {
                        assertThat(updated.getTitle()).isEqualTo("Updated title");
                        assertThat(updated.getDescription()).isEqualTo("Updated description");
                        assertThat(updated.getStatus()).isEqualTo(TaskStatus.COMPLETED);
                    });
        }

        @Test
        @DisplayName("should publish UPDATED_TASK event after update")
        void shouldPublishUpdatedTaskEvent() {
            Task existing = task(1L, user(1L, UserRole.USER));
            CreateTaskDTO dto = createTaskDto(1L);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            taskService.update(1L, dto);

            ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().type()).isEqualTo(EventType.UPDATED_TASK);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when task does not exist")
        void shouldThrowWhenTaskDoesNotExist() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.update(1L, createTaskDto(1L)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("partialUpdate")
    class PartialUpdateTests {

        @Test
        @DisplayName("should apply only present fields in DTO")
        void shouldApplyOnlyPresentFields() {
            Task existing = task(1L, user(1L, UserRole.USER));
            String originalDescription = existing.getDescription();
            UpdateTaskDTO dto = updateTaskDto("Partial title", null, TaskStatus.COMPLETED);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            ResponseTaskDTO response = taskService.partialUpdate(1L, dto);

            assertThat(response)
                    .satisfies(updated -> {
                        assertThat(updated.getTitle()).isEqualTo("Partial title");
                        assertThat(updated.getDescription()).isEqualTo(originalDescription);
                        assertThat(updated.getStatus()).isEqualTo(TaskStatus.COMPLETED);
                    });
        }

        @Test
        @DisplayName("should save task even when no fields are present")
        void shouldSaveTaskEvenWhenNoFieldIsPresent() {
            Task existing = task(1L, user(1L, UserRole.USER));
            UpdateTaskDTO dto = updateTaskDto(null, null, null);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            ResponseTaskDTO response = taskService.partialUpdate(1L, dto);

            assertThat(response.getTitle()).isEqualTo(existing.getTitle());
            verify(taskRepository).save(existing);
        }

        @Test
        @DisplayName("should publish UPDATED_TASK event after partial update")
        void shouldPublishUpdatedTaskEventOnPartialUpdate() {
            Task existing = task(1L, user(1L, UserRole.USER));
            UpdateTaskDTO dto = updateTaskDto("New title", null, null);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            taskService.partialUpdate(1L, dto);

            verify(eventPublisher).publishEvent(any(DomainEvent.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when task does not exist")
        void shouldThrowWhenTaskDoesNotExist() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.partialUpdate(1L, updateTaskDto("Title", null, null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("activate")
    class ActivateTests {

        @Test
        @DisplayName("should activate task when user is ADMIN")
        void shouldActivateTaskWhenUserIsAdmin() {
            Task existing = task(1L, user(1L, UserRole.USER));
            existing.setIsActive(false);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            ResponseTaskDTO response = taskService.activate(1L, session(99L, UserRole.ADMIN));

            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("should persist changes in repository")
        void shouldPersistChangesInRepository() {
            Task existing = task(1L, user(1L, UserRole.USER));
            existing.setIsActive(false);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            taskService.activate(1L, session(99L, UserRole.ADMIN));

            verify(taskRepository).save(existing);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not ADMIN")
        void shouldDenyAccessForNonAdminUser() {
            assertThatThrownBy(() -> taskService.activate(1L, session(1L, UserRole.USER)))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("permissão");
        }

        @Test
        @DisplayName("should not access repository when access is denied")
        void shouldNotAccessRepositoryWhenAccessDenied() {
            assertThatThrownBy(() -> taskService.activate(1L, session(1L, UserRole.USER)))
                    .isInstanceOf(AccessDeniedException.class);

            verifyNoInteractions(taskRepository);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class DeactivateTests {

        @Test
        @DisplayName("should deactivate task when user is ADMIN")
        void shouldDeactivateTaskWhenUserIsAdmin() {
            Task existing = task(1L, user(1L, UserRole.USER));

            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(taskRepository.save(existing)).thenReturn(existing);

            ResponseTaskDTO response = taskService.deactivate(1L, session(99L, UserRole.ADMIN));

            assertThat(response.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not ADMIN")
        void shouldDenyAccessForNonAdminUser() {
            assertThatThrownBy(() -> taskService.deactivate(1L, session(1L, UserRole.USER)))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should not access repository when access is denied")
        void shouldNotAccessRepositoryWhenAccessDenied() {
            assertThatThrownBy(() -> taskService.deactivate(1L, session(1L, UserRole.USER)))
                    .isInstanceOf(AccessDeniedException.class);

            verifyNoInteractions(taskRepository);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("should delete existing task")
        void shouldDeleteExistingTask() {
            Task existing = task(1L, user(1L, UserRole.USER));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

            taskService.delete(1L);

            verify(taskRepository).delete(existing);
        }

        @Test
        @DisplayName("should publish DELETED_TASK event before deletion")
        void shouldPublishDeletedTaskEventBeforeDeletion() {
            Task existing = task(1L, user(1L, UserRole.USER));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

            taskService.delete(1L);

            ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            DomainEvent event = eventCaptor.getValue();
            assertThat(event.type()).isEqualTo(EventType.DELETED_TASK);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when task does not exist")
        void shouldThrowWhenTaskDoesNotExist() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.delete(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should not delete when task does not exist")
        void shouldNotDeleteWhenTaskDoesNotExist() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.delete(1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any());
        }
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