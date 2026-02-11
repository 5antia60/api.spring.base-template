package com.santiago.base.dto;

import com.santiago.base.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 3, max = 200)
    private String title;

    private String description;

    @NotNull(message = "Status é obrigatório")
    private TaskStatus status;

    @NotNull(message = "ID do usuário é obrigatório")
    private Long userId;

    private String userName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
