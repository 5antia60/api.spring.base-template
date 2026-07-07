package com.santiago.base.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    private Long id;

    @Size(min = 3, max = 100, message = "{validation.user.name.size}")
    private String name;

    @Email(message = "{validation.user.email.invalid}")
    private String email;
}
