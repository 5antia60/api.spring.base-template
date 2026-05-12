package com.santiago.base.support;

import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.modules.tasks.model.TaskStatus;
import com.santiago.base.modules.users.dto.UpdateUserDTO;
import com.santiago.base.modules.users.dto.UserDTO;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;

import java.time.Instant;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-01-02T00:00:00Z"));
        return user;
    }

    public static UserSessionModel session(Long id, UserRole role) {
        return new UserSessionModel(user(id, role));
    }

    public static Task task(Long id, User user) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Task " + id);
        task.setDescription("Description " + id);
        task.setStatus(TaskStatus.PENDING);
        task.setUser(user);
        task.setIsActive(true);
        task.setCreatedAt(Instant.parse("2026-01-03T00:00:00Z"));
        task.setUpdatedAt(Instant.parse("2026-01-04T00:00:00Z"));
        return task;
    }

    public static UserDTO userDto(String name, String email) {
        return new UserDTO(name, email);
    }

    public static UpdateUserDTO updateUserDto(String name, String email) {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    public static CreateTaskDTO createTaskDto(Long userId) {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setTitle("New task");
        dto.setDescription("New description");
        dto.setStatus(TaskStatus.IN_PROGRESS);
        dto.setUserId(userId);
        return dto;
    }

    public static UpdateTaskDTO updateTaskDto(String title, String description, TaskStatus status) {
        UpdateTaskDTO dto = new UpdateTaskDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setStatus(status);
        return dto;
    }
}
