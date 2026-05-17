package com.edsonjr.taskflow.api.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionResponseTest {

    @Test
    @DisplayName("Should initialize fields as empty list when fields is null")
    void shouldInitializeFieldsAsEmptyListWhenFieldsIsNull() {
        ApiExceptionResponse response = new ApiExceptionResponse(
                LocalDateTime.now(),
                500,
                "Internal Server Error",
                "An unexpected error occurred.",
                "/test/errors/unexpected",
                null
        );

        assertThat(response.fields()).isNotNull();
        assertThat(response.fields()).isEmpty();
    }

    @Test
    @DisplayName("Should keep provided field errors")
    void shouldKeepProvidedFieldErrors() {
        FieldErrorResponse fieldError = new FieldErrorResponse(
                "email",
                "Email must be valid."
        );

        ApiExceptionResponse response = new ApiExceptionResponse(
                LocalDateTime.now(),
                400,
                "Bad Request",
                "Validation failed.",
                "/users",
                List.of(fieldError)
        );

        assertThat(response.fields())
                .hasSize(1)
                .containsExactly(fieldError);
    }
}