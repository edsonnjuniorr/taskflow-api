package com.edsonjr.taskflow.api.error;

import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.ConflictException;
import com.edsonjr.taskflow.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/errors")
class TestErrorController {

    @PostMapping("/validation")
    void validateRequest(@Valid @RequestBody TestValidationRequest request) {
    }

    @GetMapping("/not-found")
    void throwNotFoundException() {
        throw new NotFoundException("Resource not found.");
    }

    @GetMapping("/conflict")
    void throwConflictException() {
        throw new ConflictException("Resource already exists.");
    }

    @GetMapping("/data-integrity")
    void throwDataIntegrityViolationException() {
        throw new DataIntegrityViolationException("duplicate key value violates unique constraint app_users_email_key");
    }

    @GetMapping("/business")
    void throwBusinessException() {
        throw new BusinessException("Task cannot be completed while subtasks are pending.");
    }

    @GetMapping("/unexpected")
    void throwUnexpectedException() {
        throw new IllegalStateException("Sensitive internal database failure.");
    }

    @GetMapping("/framework-service-unavailable")
    void throwFrameworkServiceUnavailableException() {
        throw new ErrorResponseException(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/framework-unknown-status")
    void throwFrameworkUnknownStatusException() {
        throw new ErrorResponseException(HttpStatusCode.valueOf(599));
    }

    record TestValidationRequest(
            @NotBlank(message = "Name is required.")
            String name,

            @NotBlank(message = "Email is required.")
            @Email(message = "Email must be valid.")
            String email
    ) {
    }
}