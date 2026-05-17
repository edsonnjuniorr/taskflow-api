package com.edsonjr.taskflow_api.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubtaskTest {

    @Test
    void shouldCreateSubtaskWithRequiredFields() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtaskTest = Subtask.create("Create entity", "Implement Subtask entity", task);

        Assertions.assertThat(subtaskTest.getId()).isNull();
        Assertions.assertThat(subtaskTest.getTitle()).isEqualTo("Create entity");
        Assertions.assertThat(subtaskTest.getDescription()).isEqualTo("Implement Subtask entity");
        Assertions.assertThat(subtaskTest.getStatus()).isEqualTo(TaskStatus.PENDING);
        Assertions.assertThat(subtaskTest.getCreatedAt()).isNotNull();
        Assertions.assertThat(subtaskTest.getCompletedAt()).isNull();
        Assertions.assertThat(subtaskTest.getTask()).isEqualTo(task);
    }

    @Test
    void shouldTrimTitleAndDescription() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtaskTest = Subtask.create("  Create entity  ", "  Implement Subtask entity  ", task);

        Assertions.assertThat(subtaskTest.getTitle()).isEqualTo("Create entity");
        Assertions.assertThat(subtaskTest.getDescription()).isEqualTo("Implement Subtask entity");
    }

    @Test
    void shouldCreateSubtaskWithNullDescription() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtaskTest = Subtask.create("Create entity", null, task);

        Assertions.assertThat(subtaskTest.getDescription()).isNull();
    }

    @Test
    void shouldCreateSubtaskWithBlankDescriptionAsNull() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtaskTest = Subtask.create("Create entity", "   ", task);

        Assertions.assertThat(subtaskTest.getDescription()).isNull();
    }

    @Test
    void shouldFailWhenTitleIsNull() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        assertThatThrownBy(() -> Subtask.create(null, "Description", task))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("title is required");
    }

    @Test
    void shouldFailWhenTitleIsBlank() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        assertThatThrownBy(() -> Subtask.create("   ", "Description", task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title must not be blank");
    }

    @Test
    void shouldFailWhenTaskIsNull() {
        assertThatThrownBy(() -> Subtask.create("Create entity", "Description", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("task is required");
    }
}