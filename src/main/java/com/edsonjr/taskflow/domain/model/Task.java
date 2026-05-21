package com.edsonjr.taskflow.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_user_id", columnList = "user_id"),
                @Index(name = "idx_tasks_status", columnList = "status"),
                @Index(name = "idx_tasks_user_id_status", columnList = "user_id, status")
        }
)
public class Task {

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
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tasks_user_id")
    )
    private AppUser user;

    protected Task() {
    }

    private Task(String title, String description, TaskStatus status, AppUser user) {
        this.title = requireNonBlank(title);
        this.description = normalizeOptionalText(description);
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Instant.now();
        this.completedAt = resolveCompletedAt(this.status, null);
        this.user = Objects.requireNonNull(user, "user is required");
    }

    public static Task create(String title, String description, TaskStatus status, AppUser user) {
        return new Task(title, description, status, user);
    }

    public void updateStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status is required");
        this.completedAt = resolveCompletedAt(this.status, this.completedAt);
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

    public AppUser getUser() {
        return user;
    }

    private static Instant resolveCompletedAt(TaskStatus status, Instant currentCompletedAt) {
        if (status == TaskStatus.COMPLETED) {
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

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
