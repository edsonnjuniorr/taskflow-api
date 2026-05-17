package com.edsonjr.taskflow_api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubtaskTest {

    @Test
    void shouldCreateSubtaskWithRequiredFields() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtask = Subtask.create("Create entity", "Implement Subtask entity", task);

        assertThat(subtask.getId()).isNull();
        assertThat(subtask.getTitle()).isEqualTo("Create entity");
        assertThat(subtask.getDescription()).isEqualTo("Implement Subtask entity");
        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(subtask.getCreatedAt()).isNotNull();
        assertThat(subtask.getCompletedAt()).isNull();
        assertThat(subtask.getTask()).isEqualTo(task);
    }

    @Test
    void shouldTrimTitleAndDescription() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtask = Subtask.create("  Create entity  ", "  Implement Subtask entity  ", task);

        assertThat(subtask.getTitle()).isEqualTo("Create entity");
        assertThat(subtask.getDescription()).isEqualTo("Implement Subtask entity");
    }

    @Test
    void shouldCreateSubtaskWithNullDescription() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtask = Subtask.create("Create entity", null, task);

        assertThat(subtask.getDescription()).isNull();
    }

    @Test
    void shouldCreateSubtaskWithBlankDescriptionAsNull() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        Task task = Task.create("Create API", "Build domain model", user);

        Subtask subtask = Subtask.create("Create entity", "   ", task);

        assertThat(subtask.getDescription()).isNull();
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