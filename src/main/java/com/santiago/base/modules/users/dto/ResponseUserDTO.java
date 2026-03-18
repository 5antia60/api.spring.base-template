package com.santiago.base.modules.users.dto;

import com.santiago.base.core.base.BaseResponseDTO;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import lombok.Data;

@Data
public class ResponseUserDTO extends BaseResponseDTO {

    public ResponseUserDTO(User user) {
        super(
                user.getId(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    private String name;
    private String email;
    private UserRole role;
}
