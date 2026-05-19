package com.edsonjr.taskflow.api.dto.request;

import com.edsonjr.taskflow.domain.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubtaskRequest(

        @NotBlank(message = "title must not be blank")
        @Size(max = 160, message = "title must be at most 160 characters")
        String title,

        @Size(max = 1000, message = "description must be at most 1000 characters")
        String description,

        TaskStatus status
) {
}