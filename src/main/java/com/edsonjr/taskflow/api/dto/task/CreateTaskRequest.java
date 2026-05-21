package com.edsonjr.taskflow.api.dto.task;

import com.edsonjr.taskflow.domain.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "Title is required.")
        @Size(max = 160, message = "Title must have at most 160 characters.")
        String title,

        @Size(max = 1000, message = "Description must have at most 1000 characters.")
        String description,

        @NotNull(message = "User id is required.")
        UUID userId,

        @NotNull(message = "Status is required.")
        TaskStatus status
) {
}
