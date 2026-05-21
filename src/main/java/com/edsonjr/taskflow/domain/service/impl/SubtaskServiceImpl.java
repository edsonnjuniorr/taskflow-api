package com.edsonjr.taskflow.domain.service.impl;

import com.edsonjr.taskflow.domain.service.SubtaskService;
import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class SubtaskServiceImpl implements SubtaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubtaskServiceImpl.class);

    private static final String TASK_NOT_FOUND_MESSAGE = "Task not found.";
    private static final String SUBTASK_NOT_FOUND_MESSAGE = "Subtask not found.";
    private static final String COMPLETED_TASK_UNFINISHED_SUBTASK_MESSAGE =
            "Cannot create unfinished subtask for a completed task.";
    private static final String COMPLETED_TASK_UNFINISHED_SUBTASK_UPDATE_MESSAGE =
            "Cannot update subtask to unfinished status when task is completed.";

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;

    public SubtaskServiceImpl(
            SubtaskRepository subtaskRepository,
            TaskRepository taskRepository
    ) {
        this.subtaskRepository = subtaskRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    @Override
    public Subtask create(UUID taskId, String title, String description, TaskStatus status) {
        Objects.requireNonNull(status, "status is required");

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    LOGGER.debug("Subtask creation rejected because task was not found. taskId={}", taskId);

                    return new NotFoundException(TASK_NOT_FOUND_MESSAGE);
                });

        if (TaskStatus.COMPLETED.equals(task.getStatus()) && isUnfinished(status)) {
            LOGGER.warn(
                    "Subtask creation rejected because task is completed. taskId={}, requestedStatus={}",
                    taskId,
                    status
            );

            throw new BusinessException(COMPLETED_TASK_UNFINISHED_SUBTASK_MESSAGE);
        }

        Subtask subtask = Subtask.create(task, title, description, status);

        Subtask savedSubtask = subtaskRepository.save(subtask);

        LOGGER.info(
                "Subtask created successfully. subtaskId={}, taskId={}, status={}",
                savedSubtask.getId(),
                taskId,
                savedSubtask.getStatus()
        );

        return savedSubtask;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Subtask> listByTask(UUID taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            LOGGER.debug("Subtask listing rejected because task was not found. taskId={}", taskId);

            throw new NotFoundException(TASK_NOT_FOUND_MESSAGE);
        }

        return subtaskRepository.findByTask_Id(taskId, pageable);
    }

    @Transactional
    @Override
    public Subtask updateStatus(UUID subtaskId, TaskStatus status) {
        Objects.requireNonNull(status, "status is required");

        Subtask subtask = subtaskRepository.findByIdWithTask(subtaskId)
                .orElseThrow(() -> {
                    LOGGER.debug(
                            "Subtask status update rejected because subtask was not found. subtaskId={}",
                            subtaskId
                    );

                    return new NotFoundException(SUBTASK_NOT_FOUND_MESSAGE);
                });

        if (TaskStatus.COMPLETED.equals(subtask.getTask().getStatus()) && isUnfinished(status)) {
            LOGGER.warn(
                    "Subtask status update rejected because task is completed. "
                            + "subtaskId={}, taskId={}, requestedStatus={}",
                    subtaskId,
                    subtask.getTask().getId(),
                    status
            );

            throw new BusinessException(COMPLETED_TASK_UNFINISHED_SUBTASK_UPDATE_MESSAGE);
        }

        TaskStatus previousStatus = subtask.getStatus();

        subtask.updateStatus(status);

        Subtask savedSubtask = subtaskRepository.save(subtask);

        LOGGER.info(
                "Subtask status updated successfully. subtaskId={}, taskId={}, previousStatus={}, currentStatus={}",
                savedSubtask.getId(),
                savedSubtask.getTask().getId(),
                previousStatus,
                savedSubtask.getStatus()
        );

        return savedSubtask;
    }

    private static boolean isUnfinished(TaskStatus status) {
        return status != TaskStatus.COMPLETED;
    }
}
