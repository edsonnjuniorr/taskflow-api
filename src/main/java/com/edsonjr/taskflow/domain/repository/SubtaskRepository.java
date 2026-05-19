package com.edsonjr.taskflow.domain.repository;

import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {

    Page<Subtask> findByTask_Id(UUID taskId, Pageable pageable);

    @Query("select subtask from Subtask subtask join fetch subtask.task where subtask.id = :id")
    Optional<Subtask> findByIdWithTask(@Param("id") UUID id);

    boolean existsByTask_IdAndStatusNot(UUID taskId, TaskStatus status);
}
