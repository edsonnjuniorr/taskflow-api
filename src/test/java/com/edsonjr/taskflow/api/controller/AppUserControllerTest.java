package com.edsonjr.taskflow.api.controller;

import com.edsonjr.taskflow.api.error.ApiExceptionHandler;
import com.edsonjr.taskflow.api.mapper.AppUserMapper;
import com.edsonjr.taskflow.application.usecase.AppUserUseCase;
import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.exception.EmailAlreadyExistsException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppUserController.class)
@Import({
        AppUserMapper.class,
        ApiExceptionHandler.class
})
class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppUserUseCase appUserUseCase;

    @Test
    void shouldReturnCreatedWhenCreateAppUserWithValidPayload() throws Exception {
        UUID userId = UUID.randomUUID();

        AppUser appUser = AppUser.create("John Doe", "john.doe@email.com");
        ReflectionTestUtils.setField(appUser, "id", userId);

        when(appUserUseCase.create("John Doe", "john.doe@email.com"))
                .thenReturn(appUser);

        String payload = """
                {
                  "name": "John Doe",
                  "email": "john.doe@email.com"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + userId))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@email.com"));

        verify(appUserUseCase).create("John Doe", "john.doe@email.com");
        verifyNoMoreInteractions(appUserUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenCreateAppUserWithInvalidPayload() throws Exception {

        String payload = """
                {
                  "name": "",
                  "email": "invalid-email"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("email")));

        verifyNoInteractions(appUserUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenCreateAppUserExceedsMaxLengths() throws Exception {
        String payload = """
                {
                  "name": "%s",
                  "email": "%s"
                }
                """.formatted(
                "a".repeat(121),
                "a".repeat(249) + "@x.com"
        );

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("email")));

        verifyNoInteractions(appUserUseCase);
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        when(appUserUseCase.create("John Doe", "john.doe@email.com"))
                .thenThrow(new EmailAlreadyExistsException("Unable to create user with provided data."));

        String payload = """
                {
                  "name": "John Doe",
                  "email": "john.doe@email.com"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Unable to create user with provided data."));

        verify(appUserUseCase).create("John Doe", "john.doe@email.com");
        verifyNoMoreInteractions(appUserUseCase);
    }

    @Test
    void shouldReturnOkWhenFindExistingAppUserById() throws Exception {
        UUID userId = UUID.randomUUID();

        AppUser appUser = AppUser.create("John Doe", "john.doe@email.com");
        ReflectionTestUtils.setField(appUser, "id", userId);

        when(appUserUseCase.findById(userId)).thenReturn(appUser);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@email.com"));

        verify(appUserUseCase).findById(userId);
        verifyNoMoreInteractions(appUserUseCase);
    }

    @Test
    void shouldReturnNotFoundWhenAppUserDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();

        when(appUserUseCase.findById(userId))
                .thenThrow(new NotFoundException("User not found."));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found."));

        verify(appUserUseCase).findById(userId);
        verifyNoMoreInteractions(appUserUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenFindAppUserWithInvalidUuid() throws Exception {
        mockMvc.perform(get("/users/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request."));

        verifyNoInteractions(appUserUseCase);
    }
}
