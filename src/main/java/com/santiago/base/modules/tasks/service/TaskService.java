package com.santiago.base.modules.tasks.service;

import com.santiago.base.core.exceptions.ResourceNotFoundException;
import com.santiago.base.core.event.DomainEvent;
import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.pagination.service.PaginationService;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.ResponseTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.core.event.EventType;
import com.santiago.base.modules.tasks.model.TaskStatus;
import com.santiago.base.modules.tasks.repository.TaskRepository;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import com.santiago.base.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;
    private final ApplicationEventPublisher eventPublisher;

    public PaginatedResponseDTO<ResponseTaskDTO> baseFindAll(Long userId, Pageable pageable, UserSessionModel requestUser) {
        if (userId != null || !requestUser.getRole().equals(UserRole.ADMIN)) {
            Long targetUserId = userId != null ? userId : requestUser.getId();
            return findByUserId(targetUserId, pageable);
        } else {
            return findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<ResponseTaskDTO> findAll(Pageable pageable) {
        return paginationService.build(
                taskRepository.findAll(pageable),
                this::convertToResponseTaskDTO
        );
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<ResponseTaskDTO> findByUserId(Long userId, Pageable pageable) {
        return paginationService.build(
                taskRepository.findByUserId(userId, pageable),
                this::convertToResponseTaskDTO
        );
    }

    @Transactional(readOnly = true)
    public ResponseTaskDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("task.notFound", id));
        return convertToResponseTaskDTO(task);
    }

    @Transactional
    public CreateTaskDTO create(CreateTaskDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("user.notFound", dto.getUserId()));

        Task task = convertToEntity(dto);
        task.setUser(user);

        Task newTask = taskRepository.save(task);
        eventPublisher.publishEvent(new DomainEvent<>(EventType.CREATED_TASK, newTask));
        return convertToCreateTaskDTO(newTask);
    }

    @Transactional
    public ResponseTaskDTO update(Long id, CreateTaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("task.notFound", id));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());

        Task updatedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new DomainEvent<>(EventType.UPDATED_TASK, updatedTask));
        return convertToResponseTaskDTO(updatedTask);
    }

    @Transactional
    public ResponseTaskDTO partialUpdate(Long id, UpdateTaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("task.notFound", id));

        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        Task updatedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new DomainEvent<>(EventType.UPDATED_TASK, updatedTask));
        return convertToResponseTaskDTO(updatedTask);
    }

    @Transactional
    public ResponseTaskDTO activate(Long id, UserSessionModel requestUser) {
        if (requestUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("task.accessDenied.activate");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("task.notFound", id));

        task.setIsActive(true);
        Task updatedTask = taskRepository.save(task);
        return this.convertToResponseTaskDTO(updatedTask);
    }

    @Transactional
    public ResponseTaskDTO deactivate(Long id, UserSessionModel requestUser) {
        if (requestUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("task.accessDenied.deactivate");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("task.notFound", id));

        task.setIsActive(false);
        Task updatedTask = taskRepository.save(task);
        return this.convertToResponseTaskDTO(updatedTask);
    }

    @Transactional
    public void delete(Long id) {
        Task deletedTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("task.notFound", id));

        eventPublisher.publishEvent(new DomainEvent<>(EventType.DELETED_TASK, deletedTask));
        taskRepository.delete(deletedTask);
    }

    private ResponseTaskDTO convertToResponseTaskDTO(Task task) {
        return new ResponseTaskDTO(task);
    }

    private CreateTaskDTO convertToCreateTaskDTO(Task task) {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setUserId(task.getUser().getId());
        dto.setUserName(task.getUser().getName());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }

    private Task convertToEntity(CreateTaskDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING);
        return task;
    }
}
