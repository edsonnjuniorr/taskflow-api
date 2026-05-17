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

    private Subtask(String title, String description, Task task) {
        this.title = requireNonBlank(title, "title");
        this.description = normalizeOptionalText(description);
        this.status = TaskStatus.PENDING;
        this.createdAt = Instant.now();
        this.task = Objects.requireNonNull(task, "task is required");
    }

    public static Subtask create(String title, String description, Task task) {
        return new Subtask(title, description, task);
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

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");

        String normalizedValue = value.trim();

        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return normalizedValue;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}