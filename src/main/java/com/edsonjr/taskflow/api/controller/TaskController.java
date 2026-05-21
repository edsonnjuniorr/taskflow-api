package com.edsonjr.taskflow.api.controller;

import com.edsonjr.taskflow.api.dto.request.CreateTaskRequest;
import com.edsonjr.taskflow.api.dto.response.TaskResponse;
import com.edsonjr.taskflow.api.dto.request.UpdateTaskStatusRequest;
import com.edsonjr.taskflow.api.mapper.TaskMapper;
import com.edsonjr.taskflow.domain.service.TaskService;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Tag(name = "Tasks", description = "Create, filter and update tasks assigned to users.")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(
                request.title(),
                request.description(),
                request.userId(),
                request.status()
        );

        TaskResponse response = taskMapper.toResponse(task);
        URI location = URI.create("/tasks/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PagedModel<TaskResponse> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID userId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        Page<TaskResponse> tasks = taskService.list(status, userId, pageable)
                .map(taskMapper::toResponse);

        return new PagedModel<>(tasks);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        Task task = taskService.updateStatus(id, request.status());

        return taskMapper.toResponse(task);
    }
}
