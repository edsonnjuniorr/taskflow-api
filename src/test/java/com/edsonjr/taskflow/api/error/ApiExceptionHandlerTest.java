package com.edsonjr.taskflow.api.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestErrorController.class)
@Import(ApiExceptionHandler.class)
class ApiExceptionHandlerTest {

    private final MockMvc mockMvc;

    @Autowired
    ApiExceptionHandlerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("Should return 400 with field errors when validation fails")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        String body = """
                {
                  "name": "",
                  "email": "invalid-email"
                }
                """;

        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.path").value("/test/errors/validation"))
                .andExpect(jsonPath("$.fields", hasSize(2)))
                .andExpect(jsonPath("$.fields[0].field").exists())
                .andExpect(jsonPath("$.fields[0].message").exists())
                .andExpect(jsonPath("$.fields[1].field").exists())
                .andExpect(jsonPath("$.fields[1].message").exists())
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should return 404 when resource is not found")
    void shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/test/errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found."))
                .andExpect(jsonPath("$.path").value("/test/errors/not-found"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should return 409 when conflict exception is thrown")
    void shouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test/errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Resource already exists."))
                .andExpect(jsonPath("$.path").value("/test/errors/conflict"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should return 409 when database unique constraint is violated")
    void shouldReturnConflictWhenDataIntegrityViolationOccurs() throws Exception {
        mockMvc.perform(get("/test/errors/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Resource already exists or violates a unique constraint."))
                .andExpect(jsonPath("$.path").value("/test/errors/data-integrity"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should return 422 when business rule is violated")
    void shouldReturnUnprocessableEntity() throws Exception {
        mockMvc.perform(get("/test/errors/business"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message").value("Task cannot be completed while subtasks are pending."))
                .andExpect(jsonPath("$.path").value("/test/errors/business"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should return 500 with generic message when unexpected error occurs")
    void shouldReturnInternalServerErrorWithoutExposingInternalDetails() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.message").value(not("Sensitive internal database failure.")))
                .andExpect(jsonPath("$.path").value("/test/errors/unexpected"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should preserve framework status when JSON request body is malformed")
    void shouldPreserveFrameworkStatusWhenJsonRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request."))
                .andExpect(jsonPath("$.path").value("/test/errors/validation"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should preserve framework status when HTTP method is not supported")
    void shouldPreserveFrameworkStatusWhenHttpMethodIsNotSupported() throws Exception {
        mockMvc.perform(put("/test/errors/not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message").value("Invalid request."))
                .andExpect(jsonPath("$.path").value("/test/errors/not-found"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should preserve framework status when media type is not supported")
    void shouldPreserveFrameworkStatusWhenMediaTypeIsNotSupported() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.message").value("Invalid request."))
                .andExpect(jsonPath("$.path").value("/test/errors/validation"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should preserve framework 5xx status with generic unexpected message")
    void shouldPreserveFramework5xxStatusWithGenericUnexpectedMessage() throws Exception {
        mockMvc.perform(get("/test/errors/framework-service-unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.path").value("/test/errors/framework-service-unavailable"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should preserve framework non standard HTTP status")
    void shouldPreserveFrameworkNonStandardHttpStatus() throws Exception {
        mockMvc.perform(get("/test/errors/framework-unknown-status"))
                .andExpect(status().is(599))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(599))
                .andExpect(jsonPath("$.error").value("HTTP 599"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.path").value("/test/errors/framework-unknown-status"))
                .andExpect(jsonPath("$.fields", hasSize(0)))
                .andExpect(jsonPath("$", not(hasKey("trace"))))
                .andExpect(jsonPath("$", not(hasKey("stackTrace"))))
                .andExpect(jsonPath("$", not(hasKey("exception"))));
    }

    @Test
    @DisplayName("Should return 200 when validation request is valid")
    void shouldReturnOkWhenValidationRequestIsValid() throws Exception {
        String body = """
            {
              "name": "Edson",
              "email": "edson@email.com"
            }
            """;

        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
