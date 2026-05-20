package com.edsonjr.taskflow.api.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateAppUserRequestTest {

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
        CreateAppUserRequest request = new CreateAppUserRequest(
                "Edson Junior",
                "edson.junior@email.com"
        );

        Set<ConstraintViolation<CreateAppUserRequest>> violations = validator.validate(request);

        assertThat(request.name()).isEqualTo("Edson Junior");
        assertThat(request.email()).isEqualTo("edson.junior@email.com");
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRequireName() {
        CreateAppUserRequest request = new CreateAppUserRequest(
                " ",
                "edson.junior@email.com"
        );

        Set<ConstraintViolation<CreateAppUserRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name")
                        && violation.getMessage().equals("Name is required."));
    }

    @Test
    void shouldValidateNameMaxLength() {
        CreateAppUserRequest request = new CreateAppUserRequest(
                "a".repeat(121),
                "edson.junior@email.com"
        );

        Set<ConstraintViolation<CreateAppUserRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name")
                        && violation.getMessage().equals("Name must have at most 120 characters."));
    }

    @Test
    void shouldRequireEmail() {
        CreateAppUserRequest request = new CreateAppUserRequest(
                "Edson Junior",
                " "
        );

        Set<ConstraintViolation<CreateAppUserRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email")
                        && violation.getMessage().equals("Email is required."));
    }

    @Test
    void shouldValidateEmailMaxLength() {
        CreateAppUserRequest request = new CreateAppUserRequest(
                "Edson Junior",
                "a".repeat(249) + "@x.com"
        );

        Set<ConstraintViolation<CreateAppUserRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email")
                        && violation.getMessage().equals("Email must have at most 254 characters."));
    }

    @Test
    void shouldValidateEmailFormat() {
        CreateAppUserRequest request = new CreateAppUserRequest(
                "Edson Junior",
                "invalid-email"
        );

        Set<ConstraintViolation<CreateAppUserRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email")
                        && violation.getMessage().equals("Email must be valid."));
    }
}
