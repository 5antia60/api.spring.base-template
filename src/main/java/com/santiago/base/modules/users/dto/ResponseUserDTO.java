package com.santiago.base.modules.users.dto;

import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import lombok.Data;

import java.time.Instant;

@Data
public class ResponseUserDTO {

    public ResponseUserDTO(User entity) {
        this.id = entity.getId();
        this.isActive = entity.getIsActive();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.name = entity.getName();
        this.role = entity.getRole();
        this.email = entity.getEmail();
    }

    private Long id;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String name;
    private String email;
    private UserRole role;
}
