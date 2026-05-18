package com.edsonjr.taskflow.api.dto.task;

import com.edsonjr.taskflow.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Status is required.")
        TaskStatus status
) {
}