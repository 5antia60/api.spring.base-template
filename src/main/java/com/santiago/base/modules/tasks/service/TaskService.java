package com.santiago.base.modules.tasks.service;

import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.ResponseTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.core.exceptions.ResourceNotFoundException;
import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.tasks.model.TaskStatus;
import com.santiago.base.modules.tasks.repository.TaskRepository;
import com.santiago.base.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ResponseTaskDTO> findAll() {
        return taskRepository.findAll()
                .stream()
                .map(this::convertToResponseTaskDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResponseTaskDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com id: " + id));
        return convertToResponseTaskDTO(task);
    }

    @Transactional(readOnly = true)
    public List<ResponseTaskDTO> findByUserId(Long userId) {
        return taskRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponseTaskDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CreateTaskDTO create(CreateTaskDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + dto.getUserId()));

        Task task = convertToEntity(dto);
        task.setUser(user);

        Task newTask = taskRepository.save(task);
        return convertToCreateTaskDTO(newTask);
    }

    @Transactional
    public ResponseTaskDTO update(Long id, CreateTaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com id: " + id));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());

        Task updatedTask = taskRepository.save(task);
        return convertToResponseTaskDTO(updatedTask);
    }

    @Transactional
    public ResponseTaskDTO partialUpdate(Long id, UpdateTaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com id: " + id));

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
        return convertToResponseTaskDTO(updatedTask);
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarefa não encontrada com id: " + id);
        }
        taskRepository.deleteById(id);
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
