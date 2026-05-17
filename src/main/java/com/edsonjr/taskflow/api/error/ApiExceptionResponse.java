package com.edsonjr.taskflow.api.error;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiExceptionResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorResponse> fields
) {

    public ApiExceptionResponse {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    public static ApiExceptionResponse of(HttpStatus status, String message, String path) {
        return new ApiExceptionResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                List.of()
        );
    }

    public static ApiExceptionResponse withFields(
            HttpStatus status,
            String message,
            String path,
            List<FieldErrorResponse> fields
    ) {
        return new ApiExceptionResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fields
        );
    }
}