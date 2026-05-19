package com.edsonjr.taskflow.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubtaskTest {

    @Test
    void shouldCreateSubtaskWithDefaultStatusWhenStatusIsNotProvided() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create unit tests",
                "Cover subtask creation flow"
        );

        assertThat(subtask.getId()).isNull();
        assertThat(subtask.getTitle()).isEqualTo("Create unit tests");
        assertThat(subtask.getDescription()).isEqualTo("Cover subtask creation flow");
        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(subtask.getCreatedAt()).isNotNull();
        assertThat(subtask.getCompletedAt()).isNull();
        assertThat(subtask.getTask()).isEqualTo(task);
    }

    @Test
    void shouldCreateSubtaskWithProvidedStatus() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create integration tests",
                "Cover API behavior",
                TaskStatus.IN_PROGRESS
        );

        assertThat(subtask.getTitle()).isEqualTo("Create integration tests");
        assertThat(subtask.getDescription()).isEqualTo("Cover API behavior");
        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(subtask.getCreatedAt()).isNotNull();
        assertThat(subtask.getCompletedAt()).isNull();
        assertThat(subtask.getTask()).isEqualTo(task);
    }

    @Test
    void shouldSetCompletedAtWhenSubtaskIsCreatedAsCompleted() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Review implementation",
                null,
                TaskStatus.COMPLETED
        );

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(subtask.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldTrimTitleWhenCreatingSubtask() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "   Create tests   ",
                "Description"
        );

        assertThat(subtask.getTitle()).isEqualTo("Create tests");
    }

    @Test
    void shouldAllowNullDescription() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                null
        );

        assertThat(subtask.getDescription()).isNull();
    }

    @Test
    void shouldNormalizeBlankDescriptionToNull() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "   "
        );

        assertThat(subtask.getDescription()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenTitleIsNull() {
        Task task = createTask();

        assertThatThrownBy(() -> Subtask.create(
                task,
                null,
                "Description"
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("title is required");
    }

    @Test
    void shouldThrowExceptionWhenTitleIsBlank() {
        Task task = createTask();

        assertThatThrownBy(() -> Subtask.create(
                task,
                "   ",
                "Description"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("title must not be blank");
    }

    @Test
    void shouldThrowExceptionWhenTaskIsNull() {
        assertThatThrownBy(() -> Subtask.create(
                null,
                "Create tests",
                "Description"
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("task is required");
    }

    @Test
    void shouldUpdateStatusToCompletedAndSetCompletedAt() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "Description"
        );

        subtask.updateStatus(TaskStatus.COMPLETED);

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(subtask.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldKeepCompletionDateWhenCompletedStatusIsAppliedAgain() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "Description",
                TaskStatus.COMPLETED
        );

        var completedAt = subtask.getCompletedAt();

        subtask.updateStatus(TaskStatus.COMPLETED);

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(subtask.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void shouldUpdateStatusToPendingAndClearCompletedAt() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "Description",
                TaskStatus.COMPLETED
        );

        subtask.updateStatus(TaskStatus.PENDING);

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(subtask.getCompletedAt()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusToNull() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "Description"
        );

        assertThatThrownBy(() -> subtask.updateStatus(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status is required");
    }

    @Test
    void shouldNotSetCompletedAtWhenSubtaskIsCreatedAsPending() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "Description",
                TaskStatus.PENDING
        );

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(subtask.getCompletedAt()).isNull();
    }

    @Test
    void shouldNotSetCompletedAtWhenSubtaskIsCreatedAsInProgress() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "Description",
                TaskStatus.IN_PROGRESS
        );

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(subtask.getCompletedAt()).isNull();
    }

    @Test
    void shouldUpdateStatusToInProgressAndClearCompletedAt() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "Description",
                TaskStatus.COMPLETED
        );

        assertThat(subtask.getCompletedAt()).isNotNull();

        subtask.updateStatus(TaskStatus.IN_PROGRESS);

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(subtask.getCompletedAt()).isNull();
    }

    @Test
    void shouldTrimDescriptionWhenCreatingSubtask() {
        Task task = createTask();

        Subtask subtask = Subtask.create(
                task,
                "Create tests",
                "   Description with spaces   "
        );

        assertThat(subtask.getDescription()).isEqualTo("Description with spaces");
    }

    @Test
    void shouldCreateSubtaskUsingJpaConstructor() {
        Subtask subtask = new Subtask();

        assertThat(subtask).isNotNull();
    }

    private static Task createTask() {
        AppUser user = AppUser.create(
                "Edson Junior",
                "edson.junior@email.com"
        );

        return Task.create(
                "Main task",
                "Main task description",
                TaskStatus.PENDING,
                user
        );
    }
}
