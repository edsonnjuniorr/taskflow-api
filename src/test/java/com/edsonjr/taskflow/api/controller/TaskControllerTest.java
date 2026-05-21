package com.edsonjr.taskflow.api.controller;

import com.edsonjr.taskflow.api.error.ApiExceptionHandler;
import com.edsonjr.taskflow.api.mapper.TaskMapper;
import com.edsonjr.taskflow.domain.service.TaskService;
import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.edsonjr.taskflow.domain.model.TaskStatus.COMPLETED;
import static com.edsonjr.taskflow.domain.model.TaskStatus.IN_PROGRESS;
import static com.edsonjr.taskflow.domain.model.TaskStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({
        TaskMapper.class,
        ApiExceptionHandler.class
})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void shouldReturnCreatedWhenCreateTaskWithValidPayload() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = taskWithIds(taskId, "Implement task creation", "Create POST /tasks endpoint", IN_PROGRESS, userId);

        when(taskService.create("Implement task creation", "Create POST /tasks endpoint", userId, IN_PROGRESS))
                .thenReturn(task);

        String payload = """
                {
                  "title": "Implement task creation",
                  "description": "Create POST /tasks endpoint",
                  "userId": "%s",
                  "status": "IN_PROGRESS"
                }
                """.formatted(userId);

        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tasks/" + taskId))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Implement task creation"))
                .andExpect(jsonPath("$.description").value("Create POST /tasks endpoint"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(taskService).create("Implement task creation", "Create POST /tasks endpoint", userId, IN_PROGRESS);
        verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturnBadRequestWhenCreateTaskWithInvalidPayload() throws Exception {
        String payload = """
                {
                  "title": "",
                  "description": "Create POST /tasks endpoint"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("title")))
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("userId")))
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("status")));

        verifyNoInteractions(taskService);
    }

    @Test
    void shouldReturnNotFoundWhenTaskUserDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();

        when(taskService.create("Implement task creation", null, userId, PENDING))
                .thenThrow(new NotFoundException("User not found."));

        String payload = """
                {
                  "title": "Implement task creation",
                  "userId": "%s",
                  "status": "PENDING"
                }
                """.formatted(userId);

        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found."));

        verify(taskService).create("Implement task creation", null, userId, PENDING);
        verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldListTasksUsingFiltersAndPagination() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = taskWithIds(taskId, "Implement filters", null, COMPLETED, userId);

        when(taskService.list(eq(COMPLETED), eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));

        mockMvc.perform(get("/tasks")
                        .param("status", "COMPLETED")
                        .param("userId", userId.toString())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Implement filters"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].completedAt").exists())
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskService).list(eq(COMPLETED), eq(userId), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);

        verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturnBadRequestWhenTaskStatusFilterIsInvalid() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("status", "DONE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request."));

        verifyNoInteractions(taskService);
    }

    @Test
    void shouldUpdateTaskStatus() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Task task = taskWithIds(taskId, "Ship status update", null, COMPLETED, userId);

        when(taskService.updateStatus(taskId, COMPLETED)).thenReturn(task);

        String payload = """
                {
                  "status": "COMPLETED"
                }
                """;

        mockMvc.perform(patch("/tasks/{id}/status", taskId)
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Ship status update"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists())
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(taskService).updateStatus(taskId, COMPLETED);
        verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturnBadRequestWhenUpdateTaskStatusIsMissing() throws Exception {
        mockMvc.perform(patch("/tasks/{id}/status", UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields[*].field").value(hasItem("status")));

        verifyNoInteractions(taskService);
    }

    @Test
    void shouldReturnUnprocessableContentWhenTaskHasUnfinishedSubtasks() throws Exception {
        UUID taskId = UUID.randomUUID();

        when(taskService.updateStatus(taskId, COMPLETED))
                .thenThrow(new BusinessException("Task cannot be completed because it has unfinished subtasks."));

        String payload = """
                {
                  "status": "COMPLETED"
                }
                """;

        mockMvc.perform(patch("/tasks/{id}/status", taskId)
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message").value("Task cannot be completed because it has unfinished subtasks."));

        verify(taskService).updateStatus(taskId, COMPLETED);
        verifyNoMoreInteractions(taskService);
    }

    private static Task taskWithIds(
            UUID taskId,
            String title,
            String description,
            TaskStatus status,
            UUID userId
    ) {
        AppUser user = AppUser.create("John Doe", "john.doe@email.com");
        ReflectionTestUtils.setField(user, "id", userId);

        Task task = Task.create(title, description, status, user);
        ReflectionTestUtils.setField(task, "id", taskId);

        return task;
    }
}
