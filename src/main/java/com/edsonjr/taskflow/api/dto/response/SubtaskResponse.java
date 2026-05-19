package com.edsonjr.taskflow.api.dto.response;

import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record SubtaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        Instant createdAt,
        Instant completedAt,
        UUID taskId
) {

    public static SubtaskResponse from(Subtask subtask) {
        return from(subtask, subtask.getTask().getId());
    }

    public static SubtaskResponse from(Subtask subtask, UUID taskId) {
        return new SubtaskResponse(
                subtask.getId(),
                subtask.getTitle(),
                subtask.getDescription(),
                subtask.getStatus(),
                subtask.getCreatedAt(),
                subtask.getCompletedAt(),
                taskId
        );
    }
}
