package com.edsonjr.taskflow.integration;

import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskFlowEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        subtaskRepository.deleteAll();
        taskRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void shouldReturnConflictWhenCreatingUserWithDuplicatedEmail() throws Exception {
        createUser("John Doe", "duplicated@email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane Doe",
                                  "email": "duplicated@email.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Unable to create user with provided data."));
    }

    @Test
    void shouldListTasksFilteredByStatusAndUserId() throws Exception {
        String firstUserId = createUser("First User", "first@email.com");
        String secondUserId = createUser("Second User", "second@email.com");

        createTask(firstUserId, "Expected task", "PENDING");
        createTask(firstUserId, "Completed task", "COMPLETED");
        createTask(secondUserId, "Other user task", "PENDING");

        mockMvc.perform(get("/tasks")
                        .param("status", "PENDING")
                        .param("userId", firstUserId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Expected task"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].userId").value(firstUserId));
    }

    @Test
    void shouldListTasksFilteredOnlyByStatus() throws Exception {
        String firstUserId = createUser("First Status User", "first.status@email.com");
        String secondUserId = createUser("Second Status User", "second.status@email.com");

        createTask(firstUserId, "First pending task", "PENDING");
        createTask(firstUserId, "Completed task", "COMPLETED");
        createTask(secondUserId, "Second pending task", "PENDING");

        mockMvc.perform(get("/tasks")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("First pending task"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[1].title").value("Second pending task"))
                .andExpect(jsonPath("$.content[1].status").value("PENDING"));
    }

    @Test
    void shouldReturnBadRequestWhenTaskSortParameterIsMalformed() throws Exception {
        String userId = createUser("Sort User", "sort.user@email.com");

        createTask(userId, "Sortable task", "PENDING");

        mockMvc.perform(get("/tasks")
                        .param("status", "PENDING")
                        .param("userId", userId)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "[\"asc\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request."))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void shouldCreateUserTaskSubtaskAndOnlyCompleteTaskAfterAllSubtasksAreCompleted() throws Exception {
        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane Doe",
                                  "email": "jane.doe@email.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = read(userResponse);

        String taskResponse = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Deliver challenge",
                                  "description": "Finish backend implementation",
                                  "userId": "%s",
                                  "status": "PENDING"
                                }
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.completedAt").value(nullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String taskId = read(taskResponse);

        String subtaskResponse = mockMvc.perform(post("/tasks/{taskId}/subtasks", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write integration tests",
                                  "status": "PENDING"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String subtaskId = read(subtaskResponse);

        mockMvc.perform(patch("/tasks/{taskId}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message")
                        .value("Task cannot be completed because it has unfinished subtasks."));

        mockMvc.perform(patch("/subtasks/{subtaskId}/status", subtaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());

        mockMvc.perform(patch("/tasks/{taskId}/status", taskId)
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

    private String read(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asText();
    }

    private String createUser(String name, String email) throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s"
                                }
                                """.formatted(name, email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return read(response);
    }

    private void createTask(String userId, String title, String status) throws Exception {
        String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "userId": "%s",
                                  "status": "%s"
                                }
                                """.formatted(title, userId, status)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        read(response);
    }
}
