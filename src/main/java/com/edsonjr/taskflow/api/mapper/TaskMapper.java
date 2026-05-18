package com.edsonjr.taskflow.api.mapper;

import com.edsonjr.taskflow.api.dto.task.TaskResponse;
import com.edsonjr.taskflow.domain.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getCompletedAt(),
                task.getUser().getId()
        );
    }
}