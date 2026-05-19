package com.edsonjr.taskflow.api.dto.request;

import com.edsonjr.taskflow.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSubtaskStatusRequest(

        @NotNull(message = "status is required")
        TaskStatus status
) {
}