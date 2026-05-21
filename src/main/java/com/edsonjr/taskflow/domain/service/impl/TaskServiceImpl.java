package com.edsonjr.taskflow.domain.service.impl;

import com.edsonjr.taskflow.domain.service.TaskService;
import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.domain.specification.TaskSpecifications;
import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
    private static final String TASK_NOT_FOUND_MESSAGE = "Task not found.";
    private static final String TASK_HAS_UNFINISHED_SUBTASKS_MESSAGE =
            "Task cannot be completed because it has unfinished subtasks.";

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;
    private final SubtaskRepository subtaskRepository;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            AppUserRepository appUserRepository,
            SubtaskRepository subtaskRepository
    ) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
        this.subtaskRepository = subtaskRepository;
    }

    @Transactional
    @Override
    public Task create(String title, String description, UUID userId, TaskStatus status) {
        Objects.requireNonNull(status, "status is required");

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.debug("Task creation rejected because user was not found. userId={}", userId);

                    return new NotFoundException(USER_NOT_FOUND_MESSAGE);
                });

        Task task = Task.create(
                title,
                description,
                status,
                user
        );

        Task savedTask = taskRepository.save(task);

        LOGGER.info(
                "Task created successfully. taskId={}, userId={}, status={}",
                savedTask.getId(),
                userId,
                savedTask.getStatus()
        );

        return savedTask;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Task> list(TaskStatus status, UUID userId, Pageable pageable) {
        Specification<Task> specification = TaskSpecifications.hasStatus(status)
                .and(TaskSpecifications.belongsToUser(userId));

        return taskRepository.findAll(specification, pageable);
    }

    @Transactional
    @Override
    public Task updateStatus(UUID taskId, TaskStatus status) {
        Objects.requireNonNull(status, "status is required");

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    LOGGER.debug("Task status update rejected because task was not found. taskId={}", taskId);

                    return new NotFoundException(TASK_NOT_FOUND_MESSAGE);
                });

        if (status == TaskStatus.COMPLETED && hasUnfinishedSubtasks(taskId)) {
            LOGGER.warn(
                    "Task status update rejected because task has unfinished subtasks. taskId={}, requestedStatus={}",
                    taskId,
                    status
            );

            throw new BusinessException(TASK_HAS_UNFINISHED_SUBTASKS_MESSAGE);
        }

        TaskStatus previousStatus = task.getStatus();

        task.updateStatus(status);

        LOGGER.info(
                "Task status updated successfully. taskId={}, previousStatus={}, currentStatus={}",
                taskId,
                previousStatus,
                task.getStatus()
        );

        return task;
    }

    private boolean hasUnfinishedSubtasks(UUID taskId) {
        return subtaskRepository.existsByTask_IdAndStatusNot(taskId, TaskStatus.COMPLETED);
    }
}
