package com.santiago.base.modules.tasks.dto;

import com.santiago.base.modules.tasks.model.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDTO {

    @Size(min = 3, max = 200, message = "{validation.task.title.size}")
    private String title;

    private String description;

    private TaskStatus status;
}
