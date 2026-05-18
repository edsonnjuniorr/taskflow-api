package com.edsonjr.taskflow.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    @Test
    void shouldCreateTaskWithRequiredFields() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        Task task = Task.create("Create API", "Build domain model", user);

        assertThat(task.getId()).isNull();
        assertThat(task.getTitle()).isEqualTo("Create API");
        assertThat(task.getDescription()).isEqualTo("Build domain model");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(task.getCompletedAt()).isNull();
        assertThat(task.getUser()).isEqualTo(user);
    }

    @Test
    void shouldTrimTitleAndDescription() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        Task task = Task.create("  Create API  ", "  Build domain model  ", user);

        assertThat(task.getTitle()).isEqualTo("Create API");
        assertThat(task.getDescription()).isEqualTo("Build domain model");
    }

    @Test
    void shouldCreateTaskWithNullDescription() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        Task task = Task.create("Create API", null, user);

        assertThat(task.getDescription()).isNull();
    }

    @Test
    void shouldCreateTaskWithBlankDescriptionAsNull() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        Task task = Task.create("Create API", "   ", user);

        assertThat(task.getDescription()).isNull();
    }

    @Test
    void shouldFailWhenTitleIsNull() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        assertThatThrownBy(() -> Task.create(null, "Description", user))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("title is required");
    }

    @Test
    void shouldFailWhenTitleIsBlank() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");

        assertThatThrownBy(() -> Task.create("   ", "Description", user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title must not be blank");
    }

    @Test
    void shouldFailWhenUserIsNull() {
        assertThatThrownBy(() -> Task.create("Create API", "Description", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("user is required");
    }

    @Test
    void shouldCreateTaskWithPendingStatusWhenStatusIsNull() {
        AppUser user = AppUser.create("Edson", "edson@example.com");

        Task task = Task.create("Implement task module", null, null, user);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getCompletedAt()).isNull();
    }

    @Test
    void shouldSetCompletionDateWhenStatusIsUpdatedToCompleted() {
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Implement task module", null, user);

        task.updateStatus(TaskStatus.COMPLETED);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldClearCompletionDateWhenStatusIsUpdatedFromCompletedToPending() {
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Implement task module", null, TaskStatus.COMPLETED, user);

        task.updateStatus(TaskStatus.PENDING);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getCompletedAt()).isNull();
    }

    @Test
    void shouldKeepCompletionDateWhenCompletedStatusIsAppliedAgain() {
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Implement task module", null, TaskStatus.COMPLETED, user);

        var completedAt = task.getCompletedAt();

        task.updateStatus(TaskStatus.COMPLETED);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void shouldFailWhenUpdatedStatusIsNull() {
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Implement task module", null, user);

        assertThatThrownBy(() -> task.updateStatus(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status is required");
    }
}
