package com.edsonjr.taskflow.repository;

import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByUser_Id(UUID userId);

    List<Task> findByStatusAndUserId(TaskStatus status, UUID userId);
}