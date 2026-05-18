package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.domain.specification.TaskSpecifications;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TaskService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
    private static final String TASK_NOT_FOUND_MESSAGE = "Task not found.";
    private static final String TASK_HAS_UNFINISHED_SUBTASKS_MESSAGE =
            "Task cannot be completed because it has unfinished subtasks.";

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;
    private final SubtaskRepository subtaskRepository;

    public TaskService(
            TaskRepository taskRepository,
            AppUserRepository appUserRepository,
            SubtaskRepository subtaskRepository
    ) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
        this.subtaskRepository = subtaskRepository;
    }

    @Transactional
    public Task create(String title, String description, UUID userId, TaskStatus status) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));

        Task task = Task.create(
                title,
                description,
                status,
                user
        );

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Page<Task> list(TaskStatus status, UUID userId, Pageable pageable) {
        Specification<Task> specification = TaskSpecifications.hasStatus(status)
                .and(TaskSpecifications.belongsToUser(userId));

        return taskRepository.findAll(specification, pageable);
    }

    @Transactional
    public Task updateStatus(UUID taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND_MESSAGE));

        if (status == TaskStatus.COMPLETED && hasUnfinishedSubtasks(taskId)) {
            throw new IllegalStateException(TASK_HAS_UNFINISHED_SUBTASKS_MESSAGE);
        }

        task.updateStatus(status);

        return task;
    }

    private boolean hasUnfinishedSubtasks(UUID taskId) {
        return subtaskRepository.existsByTask_IdAndStatusNot(taskId, TaskStatus.COMPLETED);
    }
}