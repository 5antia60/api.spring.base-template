package com.santiago.base.modules.tasks.dto;

import com.santiago.base.modules.tasks.model.TaskStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaskDtoValidationTest {

    private static jakarta.validation.ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void createTaskDtoShouldRejectMissingRequiredFields() {
        CreateTaskDTO dto = new CreateTaskDTO();

        Set<ConstraintViolation<CreateTaskDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "status", "userId");
    }

    @Test
    void createTaskDtoShouldAcceptValidPayload() {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setTitle("Valid title");
        dto.setDescription("Description");
        dto.setStatus(TaskStatus.PENDING);
        dto.setUserId(1L);

        Set<ConstraintViolation<CreateTaskDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void updateTaskDtoShouldAllowNullFieldsForPartialUpdate() {
        UpdateTaskDTO dto = new UpdateTaskDTO();

        Set<ConstraintViolation<UpdateTaskDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
