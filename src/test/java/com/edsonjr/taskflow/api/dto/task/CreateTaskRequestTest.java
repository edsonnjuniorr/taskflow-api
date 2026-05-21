package com.edsonjr.taskflow.api.dto.task;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static com.edsonjr.taskflow.domain.model.TaskStatus.COMPLETED;
import static com.edsonjr.taskflow.domain.model.TaskStatus.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;

class CreateTaskRequestTest {

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
    void shouldExposeProvidedValues() {
        UUID userId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest(
                "Implement task creation",
                "Create POST /tasks endpoint",
                userId,
                IN_PROGRESS
        );

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);

        assertThat(request.title()).isEqualTo("Implement task creation");
        assertThat(request.description()).isEqualTo("Create POST /tasks endpoint");
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.status()).isEqualTo(IN_PROGRESS);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRequireTitle() {
        CreateTaskRequest request = new CreateTaskRequest(
                " ",
                "Create POST /tasks endpoint",
                UUID.randomUUID(),
                IN_PROGRESS
        );

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("title")
                        && violation.getMessage().equals("Title is required."));
    }

    @Test
    void shouldRequireUserId() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Implement task creation",
                "Create POST /tasks endpoint",
                null,
                IN_PROGRESS
        );

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("userId")
                        && violation.getMessage().equals("User id is required."));
    }

    @Test
    void shouldRequireStatus() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Implement task creation",
                "Create POST /tasks endpoint",
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("status")
                        && violation.getMessage().equals("Status is required."));
    }

    @Test
    void shouldLimitTitleLength() {
        CreateTaskRequest request = new CreateTaskRequest(
                "a".repeat(161),
                "Create POST /tasks endpoint",
                UUID.randomUUID(),
                COMPLETED
        );

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("title")
                        && violation.getMessage().equals("Title must have at most 160 characters."));
    }

    @Test
    void shouldLimitDescriptionLength() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Implement task creation",
                "a".repeat(1001),
                UUID.randomUUID(),
                COMPLETED
        );

        Set<ConstraintViolation<CreateTaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("description")
                        && violation.getMessage().equals("Description must have at most 1000 characters."));
    }
}
