package com.santiago.base.modules.users.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoValidationTest {

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
    void userDtoShouldRejectBlankNameAndInvalidEmail() {
        UserDTO dto = new UserDTO("", "invalid-email");

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "email");
    }

    @Test
    void updateUserDtoShouldAllowNullFieldsForPartialUpdate() {
        UpdateUserDTO dto = new UpdateUserDTO();

        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
