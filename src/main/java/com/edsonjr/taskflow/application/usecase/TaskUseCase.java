package com.edsonjr.taskflow.application.usecase;

import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskUseCase {

    Task create(String title, String description, UUID userId, TaskStatus status);

    Page<Task> list(TaskStatus status, UUID userId, Pageable pageable);

    Task updateStatus(UUID taskId, TaskStatus status);
}
