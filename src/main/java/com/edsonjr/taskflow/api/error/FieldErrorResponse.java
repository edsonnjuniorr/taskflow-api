package com.edsonjr.taskflow.api.error;

public record FieldErrorResponse(
        String field,
        String message
) {
}