package com.edsonjr.taskflow.api.dto.task;

import com.edsonjr.taskflow.domain.model.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        Instant createdAt,
        Instant completedAt,
        UUID userId
) {
}
