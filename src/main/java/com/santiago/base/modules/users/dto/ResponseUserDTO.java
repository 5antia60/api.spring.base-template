package com.santiago.base.modules.users.dto;

import com.santiago.base.modules.users.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseUserDTO {

    public ResponseUserDTO(User entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
