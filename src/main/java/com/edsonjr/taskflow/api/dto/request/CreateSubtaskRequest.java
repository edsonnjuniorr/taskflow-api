package com.edsonjr.taskflow.api.dto.request;

import com.edsonjr.taskflow.domain.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubtaskRequest(

        @NotBlank(message = "Title is required.")
        @Size(max = 160, message = "Title must have at most 160 characters.")
        String title,

        @Size(max = 1000, message = "Description must have at most 1000 characters.")
        String description,

        TaskStatus status
) {
}
