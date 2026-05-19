package com.edsonjr.taskflow.api.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.edsonjr.taskflow.domain.model.TaskStatus.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;

class CreateSubtaskRequestTest {

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
        CreateSubtaskRequest request = new CreateSubtaskRequest(
                "Write controller tests",
                "Cover subtask endpoints",
                IN_PROGRESS
        );

        Set<ConstraintViolation<CreateSubtaskRequest>> violations = validator.validate(request);

        assertThat(request.title()).isEqualTo("Write controller tests");
        assertThat(request.description()).isEqualTo("Cover subtask endpoints");
        assertThat(request.status()).isEqualTo(IN_PROGRESS);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRequireTitle() {
        CreateSubtaskRequest request = new CreateSubtaskRequest(
                " ",
                "Cover subtask endpoints",
                null
        );

        Set<ConstraintViolation<CreateSubtaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("title")
                        && violation.getMessage().equals("Title is required."));
    }

    @Test
    void shouldLimitTitleLength() {
        CreateSubtaskRequest request = new CreateSubtaskRequest(
                "a".repeat(161),
                "Cover subtask endpoints",
                null
        );

        Set<ConstraintViolation<CreateSubtaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("title")
                        && violation.getMessage().equals("Title must have at most 160 characters."));
    }

    @Test
    void shouldLimitDescriptionLength() {
        CreateSubtaskRequest request = new CreateSubtaskRequest(
                "Write controller tests",
                "a".repeat(1001),
                null
        );

        Set<ConstraintViolation<CreateSubtaskRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("description")
                        && violation.getMessage().equals("Description must have at most 1000 characters."));
    }
}
