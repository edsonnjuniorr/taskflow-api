package com.edsonjr.taskflow.api.dto.task;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.edsonjr.taskflow.domain.model.TaskStatus.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;

class UpdateTaskStatusRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldExposeProvidedStatus() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(COMPLETED);

        Set<ConstraintViolation<UpdateTaskStatusRequest>> violations = validator.validate(request);

        assertThat(request.status()).isEqualTo(COMPLETED);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRequireStatus() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(null);

        Set<ConstraintViolation<UpdateTaskStatusRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("status")
                        && violation.getMessage().equals("Status is required."));
    }
}
