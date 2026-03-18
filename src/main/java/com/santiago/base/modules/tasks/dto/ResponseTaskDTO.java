package com.santiago.base.modules.tasks.dto;

import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.modules.tasks.model.TaskStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class ResponseTaskDTO {

    public ResponseTaskDTO(Task entity) {
        this.id = entity.getId();
        this.isActive = entity.getIsActive();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.status = entity.getStatus();
        this.userId = entity.getUser().getId();
        this.userName = entity.getUser().getName();
    }

    private Long id;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String title;
    private String description;
    private TaskStatus status;
    private Long userId;
    private String userName;
}
