package com.edsonjr.taskflow.api.controller;

import com.edsonjr.taskflow.api.dto.request.CreateSubtaskRequest;
import com.edsonjr.taskflow.api.dto.request.UpdateSubtaskStatusRequest;
import com.edsonjr.taskflow.api.dto.response.SubtaskResponse;
import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.service.SubtaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Tag(name = "Subtasks", description = "Create, list and update subtasks linked to tasks.")
@RestController
public class SubtaskController {

    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    @PostMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<SubtaskResponse> create(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateSubtaskRequest request
    ) {
        Subtask subtask = subtaskService.create(
                taskId,
                request.title(),
                request.description(),
                request.status()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(SubtaskResponse.from(subtask, taskId));
    }

    @GetMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<PagedModel<SubtaskResponse>> listByTask(
            @PathVariable UUID taskId,
            @PageableDefault(size = 20, sort = "createdAt", direction = ASC) Pageable pageable
    ) {
        Page<SubtaskResponse> response = subtaskService.listByTask(taskId, pageable)
                .map(subtask -> SubtaskResponse.from(subtask, taskId));

        return ResponseEntity.ok(new PagedModel<>(response));
    }

    @PatchMapping("/subtasks/{subtaskId}/status")
    public ResponseEntity<SubtaskResponse> updateStatus(
            @PathVariable UUID subtaskId,
            @Valid @RequestBody UpdateSubtaskStatusRequest request
    ) {
        Subtask subtask = subtaskService.updateStatus(subtaskId, request.status());

        return ResponseEntity.ok(SubtaskResponse.from(subtask));
    }
}
