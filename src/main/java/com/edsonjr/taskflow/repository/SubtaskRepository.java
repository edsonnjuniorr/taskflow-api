package com.edsonjr.taskflow.repository;

import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {

    List<Subtask> findByTask_Id(UUID taskId);

    boolean existsByTask_IdAndStatusNot(UUID taskId, TaskStatus status);
}
