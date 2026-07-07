package com.santiago.base.modules.tasks.dto;

import com.santiago.base.modules.tasks.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDTO {
    private Long id;

    @NotBlank(message = "{validation.task.title.notBlank}")
    @Size(min = 3, max = 200, message = "{validation.task.title.size}")
    private String title;

    private String description;

    @NotNull(message = "{validation.task.status.notNull}")
    private TaskStatus status;

    @NotNull(message = "{validation.task.userId.notNull}")
    private Long userId;

    private String userName;

    private Instant createdAt;
    private Instant updatedAt;
}
