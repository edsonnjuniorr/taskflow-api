package com.edsonjr.taskflow.api.controller;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.support.PostgreSQLIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubtaskControllerTest extends PostgreSQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    private Task task;

    @BeforeEach
    void setUp() {
        subtaskRepository.deleteAll();
        taskRepository.deleteAll();
        appUserRepository.deleteAll();

        AppUser user = appUserRepository.save(
                AppUser.create("Edson Junior", "edson.junior@email.com")
        );

        task = taskRepository.save(
                Task.create("Main task", "Main task description", TaskStatus.PENDING, user)
        );
    }

    @Test
    void shouldCreateSubtaskSuccessfully() throws Exception {
        String requestBody = """
                {
                    "title": "Create unit tests",
                    "description": "Cover the subtask creation flow"
                }
                """;

        mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Create unit tests"))
                .andExpect(jsonPath("$.description").value("Cover the subtask creation flow"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.completedAt").value(nullValue()))
                .andExpect(jsonPath("$.taskId").value(task.getId().toString()));
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        UUID nonExistingTaskId = UUID.randomUUID();

        String requestBody = """
                {
                    "title": "Create unit tests"
                }
                """;

        mockMvc.perform(post("/tasks/{id}/subtasks", nonExistingTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsInvalid() throws Exception {
        String requestBody = """
                {
                    "title": "   ",
                    "description": "Invalid title"
                }
                """;

        mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnprocessableContentWhenCreatingUnfinishedSubtaskForCompletedTask() throws Exception {
        task.updateStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        String requestBody = """
                {
                    "title": "Create unit tests"
                }
                """;

        mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Cannot create unfinished subtask for a completed task."));
    }

    @Test
    void shouldListSubtasksFromTask() throws Exception {
        mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "First subtask"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Second subtask"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks/{id}/subtasks", task.getId())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("First subtask"))
                .andExpect(jsonPath("$.content[0].taskId").value(task.getId().toString()))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    void shouldLimitSubtaskPageSize() throws Exception {
        mockMvc.perform(get("/tasks/{id}/subtasks", task.getId())
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void shouldReturnNotFoundWhenListingSubtasksFromMissingTask() throws Exception {
        mockMvc.perform(get("/tasks/{id}/subtasks", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found."));
    }

    @Test
    void shouldNotListSubtasksFromAnotherTask() throws Exception {
        AppUser user = appUserRepository.save(
                AppUser.create("Another User", "another.user@email.com")
        );

        Task anotherTask = taskRepository.save(
                Task.create("Another task", "Another task description", TaskStatus.PENDING, user)
        );

        mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Visible subtask"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/tasks/{id}/subtasks", anotherTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Hidden subtask"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks/{id}/subtasks", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(content().string(containsString("Visible subtask")))
                .andExpect(content().string(not(containsString("Hidden subtask"))));
    }

    @Test
    void shouldUpdateSubtaskStatusSuccessfully() throws Exception {
        String response = mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Review implementation"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String subtaskId = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(patch("/subtasks/{id}/status", UUID.fromString(subtaskId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void shouldReturnUnprocessableContentWhenUpdatingSubtaskToUnfinishedStatusForCompletedTask() throws Exception {
        task.updateStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        String response = mockMvc.perform(post("/tasks/{id}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Review implementation",
                                    "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String subtaskId = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(patch("/subtasks/{id}/status", UUID.fromString(subtaskId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "PENDING"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message")
                        .value("Cannot update subtask to unfinished status when task is completed."));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateSubtaskStatusIsMissing() throws Exception {
        mockMvc.perform(patch("/subtasks/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields", hasSize(1)))
                .andExpect(jsonPath("$.fields[0].field").value("status"));
    }
}
