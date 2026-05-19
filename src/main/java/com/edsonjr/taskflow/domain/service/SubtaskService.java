package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubtaskService {

    private static final String TASK_NOT_FOUND_MESSAGE = "Task not found.";
    private static final String SUBTASK_NOT_FOUND_MESSAGE = "Subtask not found.";
    private static final String COMPLETED_TASK_UNFINISHED_SUBTASK_MESSAGE =
            "Cannot create unfinished subtask for a completed task.";
    private static final String COMPLETED_TASK_UNFINISHED_SUBTASK_UPDATE_MESSAGE =
            "Cannot update subtask to unfinished status when task is completed.";

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;

    public SubtaskService(
            SubtaskRepository subtaskRepository,
            TaskRepository taskRepository
    ) {
        this.subtaskRepository = subtaskRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Subtask create(UUID taskId, String title, String description, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND_MESSAGE));

        if (TaskStatus.COMPLETED.equals(task.getStatus()) && isUnfinished(status)) {
            throw new BusinessException(COMPLETED_TASK_UNFINISHED_SUBTASK_MESSAGE);
        }

        Subtask subtask = Subtask.create(task, title, description, status);

        return subtaskRepository.save(subtask);
    }

    @Transactional(readOnly = true)
    public Page<Subtask> listByTask(UUID taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException(TASK_NOT_FOUND_MESSAGE);
        }

        return subtaskRepository.findByTask_Id(taskId, pageable);
    }

    @Transactional
    public Subtask updateStatus(UUID subtaskId, TaskStatus status) {
        Subtask subtask = subtaskRepository.findByIdWithTask(subtaskId)
                .orElseThrow(() -> new NotFoundException(SUBTASK_NOT_FOUND_MESSAGE));

        if (TaskStatus.COMPLETED.equals(subtask.getTask().getStatus()) && isUnfinished(status)) {
            throw new BusinessException(COMPLETED_TASK_UNFINISHED_SUBTASK_UPDATE_MESSAGE);
        }

        subtask.updateStatus(status);

        return subtaskRepository.save(subtask);
    }

    private static boolean isUnfinished(TaskStatus status) {
        return status == null || status != TaskStatus.COMPLETED;
    }
}
