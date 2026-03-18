package com.santiago.base.modules.tasks.dto;

import com.santiago.base.core.base.BaseResponseDTO;
import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.modules.tasks.model.TaskStatus;
import lombok.Data;

@Data
public class ResponseTaskDTO extends BaseResponseDTO {

    public ResponseTaskDTO(Task task) {
        super(
                task.getId(),
                task.getIsActive(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );

        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.userId = task.getUser().getId();
        this.userName = task.getUser().getName();
    }

    private String title;
    private String description;
    private TaskStatus status;
    private Long userId;
    private String userName;
}
