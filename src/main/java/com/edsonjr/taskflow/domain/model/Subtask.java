package com.edsonjr.taskflow.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "subtasks",
        indexes = {
                @Index(name = "idx_subtasks_task_id", columnList = "task_id")
        }
)
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "task_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_subtasks_task_id")
    )
    private Task task;

    protected Subtask() {
    }

    private Subtask(Task task, String title, String description, TaskStatus status) {
        this.task = Objects.requireNonNull(task, "task is required");
        this.title = requireNonBlank(title);
        this.description = normalizeDescription(description);
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Instant.now();
        this.completedAt = resolveCompletedAt(this.status, null);
    }

    public static Subtask create(Task task, String title, String description, TaskStatus status) {
        return new Subtask(task, title, description, status);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Task getTask() {
        return task;
    }

    public void updateStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
        this.completedAt = resolveCompletedAt(this.status, this.completedAt);
    }

    private static String normalizeDescription(String value) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();

        return normalizedValue.isBlank() ? null : normalizedValue;
    }

    private static Instant resolveCompletedAt(TaskStatus status, Instant currentCompletedAt) {
        if (TaskStatus.COMPLETED.equals(status)) {
            return currentCompletedAt != null ? currentCompletedAt : Instant.now();
        }

        return null;
    }

    private static String requireNonBlank(String value) {
        Objects.requireNonNull(value, "title is required");

        String normalizedValue = value.trim();

        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        return normalizedValue;
    }

}
