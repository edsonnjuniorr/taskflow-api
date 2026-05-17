package com.edsonjr.taskflow.domain.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppUserTest {

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
    void shouldCreateAppUserWithRequiredFields() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldTrimNameAndEmail() {
        AppUser user = AppUser.create("  John Doe  ", "  john.doe@example.com  ");

        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldNormalizeEmailToLowerCase() {
        AppUser user = AppUser.create("John Doe", "John.Doe@Example.COM");

        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFailWhenNameIsNull() {
        assertThatThrownBy(() -> AppUser.create(null, "john.doe@example.com"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("name is required");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        assertThatThrownBy(() -> AppUser.create("   ", "john.doe@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        assertThatThrownBy(() -> AppUser.create("John Doe", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("email is required");
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        assertThatThrownBy(() -> AppUser.create("John Doe", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email must not be blank");
    }

    @Test
    void shouldHaveNoValidationViolationsWhenEmailIsValid() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveValidationViolationWhenEmailFormatIsInvalid() {
        AppUser user = AppUser.create("John Doe", "invalid-email");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(user);

        assertThat(violations)
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("email")
                        && violation.getMessage().equals("email must be valid"));
    }

    @Test
    void shouldLowercaseEmail() {
        AppUser user = AppUser.create("John Doe", "JOHN.DOE@EXAMPLE.COM");

        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldHaveValidationViolationWhenNameIsNull() throws Exception {
        AppUser user = createViaJpa();
        setField(user, "email", "john.doe@example.com");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(user);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")
                        && v.getMessage().equals("name is required"));
    }

    @Test
    void shouldHaveValidationViolationWhenEmailIsNull() throws Exception {
        AppUser user = createViaJpa();
        setField(user, "name", "John Doe");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(user);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")
                        && v.getMessage().equals("email must not be blank"));
    }

    private static AppUser createViaJpa() throws Exception {
        var constructor = AppUser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}