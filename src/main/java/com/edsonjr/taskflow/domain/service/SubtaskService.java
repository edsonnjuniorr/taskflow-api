package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SubtaskService {

    Subtask create(UUID taskId, String title, String description, TaskStatus status);

    Page<Subtask> listByTask(UUID taskId, Pageable pageable);

    Subtask updateStatus(UUID subtaskId, TaskStatus status);
}
