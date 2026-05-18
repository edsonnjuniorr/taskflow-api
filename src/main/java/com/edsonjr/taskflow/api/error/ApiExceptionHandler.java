package com.edsonjr.taskflow.api.error;

import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.ConflictException;
import com.edsonjr.taskflow.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static final String VALIDATION_ERROR_MESSAGE = "Validation failed.";
    private static final String INVALID_REQUEST_MESSAGE = "Invalid request.";
    private static final String DATA_INTEGRITY_ERROR_MESSAGE = "Resource already exists or violates a unique constraint.";
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<FieldErrorResponse> fields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(fieldError -> new FieldErrorResponse(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ApiExceptionResponse response = ApiExceptionResponse.withFields(
                status,
                VALIDATION_ERROR_MESSAGE,
                request.getRequestURI(),
                fields
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiExceptionResponse> handleMethodArgumentTypeMismatchException(
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiExceptionResponse response = ApiExceptionResponse.of(
                status,
                INVALID_REQUEST_MESSAGE,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiExceptionResponse> handleHttpMessageNotReadableException(
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiExceptionResponse response = ApiExceptionResponse.of(
                status,
                INVALID_REQUEST_MESSAGE,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiExceptionResponse> handleNotFoundException(
            NotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiExceptionResponse response = ApiExceptionResponse.of(
                status,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiExceptionResponse> handleConflictException(
            ConflictException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        ApiExceptionResponse response = ApiExceptionResponse.of(
                status,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiExceptionResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn(
                "Data integrity violation while processing request. method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        HttpStatus status = HttpStatus.CONFLICT;

        ApiExceptionResponse response = ApiExceptionResponse.of(
                status,
                DATA_INTEGRITY_ERROR_MESSAGE,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiExceptionResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;

        ApiExceptionResponse response = ApiExceptionResponse.of(
                status,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        if (exception instanceof ErrorResponse errorResponse) {
            return handleFrameworkErrorResponse(errorResponse, request);
        }

        LOGGER.error(
                "Unexpected error while processing request. method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiExceptionResponse response = ApiExceptionResponse.of(
                status,
                UNEXPECTED_ERROR_MESSAGE,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<ApiExceptionResponse> handleFrameworkErrorResponse(
            ErrorResponse errorResponse,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = errorResponse.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());

        ApiExceptionResponse response = new ApiExceptionResponse(
                LocalDateTime.now(),
                statusCode.value(),
                resolveReasonPhrase(statusCode),
                resolveFrameworkMessage(statusCode),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity.status(statusCode).body(response);
    }

    private String resolveReasonPhrase(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());

        if (status == null) {
            return "HTTP " + statusCode.value();
        }

        return status.getReasonPhrase();
    }

    private String resolveFrameworkMessage(HttpStatusCode statusCode) {
        if (statusCode.is4xxClientError()) {
            return INVALID_REQUEST_MESSAGE;
        }

        return UNEXPECTED_ERROR_MESSAGE;
    }
}
