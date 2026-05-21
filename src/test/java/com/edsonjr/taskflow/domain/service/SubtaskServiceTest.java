package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubtaskServiceTest {

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SubtaskService subtaskService;

    @Test
    void shouldCreateSubtaskWithPendingStatus() {
        UUID taskId = UUID.randomUUID();
        Task task = createTask();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subtask subtask = subtaskService.create(
                taskId,
                "Create unit tests",
                "Cover service behavior",
                TaskStatus.PENDING
        );

        assertThat(subtask.getTitle()).isEqualTo("Create unit tests");
        assertThat(subtask.getDescription()).isEqualTo("Cover service behavior");
        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(subtask.getCreatedAt()).isNotNull();
        assertThat(subtask.getCompletedAt()).isNull();
        assertThat(subtask.getTask()).isEqualTo(task);

        verify(taskRepository).findById(taskId);
        verify(subtaskRepository).save(any(Subtask.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingSubtaskWithoutStatus() {
        UUID taskId = UUID.randomUUID();

        assertThatThrownBy(() -> subtaskService.create(
                taskId,
                "Create tests",
                "Description",
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status is required");

        verifyNoInteractions(taskRepository, subtaskRepository);
    }

    @Test
    void shouldCreateSubtaskWithProvidedStatus() {
        UUID taskId = UUID.randomUUID();
        Task task = createTask();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subtask subtask = subtaskService.create(
                taskId,
                "Review implementation",
                "Review service code",
                TaskStatus.IN_PROGRESS
        );

        assertThat(subtask.getTitle()).isEqualTo("Review implementation");
        assertThat(subtask.getDescription()).isEqualTo("Review service code");
        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(subtask.getCompletedAt()).isNull();
        assertThat(subtask.getTask()).isEqualTo(task);

        verify(taskRepository).findById(taskId);
        verify(subtaskRepository).save(any(Subtask.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTaskDoesNotExistWhileCreatingSubtask() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subtaskService.create(
                taskId,
                "Create tests",
                "Description",
                TaskStatus.PENDING
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found.");

        verify(taskRepository).findById(taskId);
        verifyNoInteractions(subtaskRepository);
    }

    @Test
    void shouldThrowBusinessExceptionWhenCreatingPendingSubtaskForCompletedTask() {
        UUID taskId = UUID.randomUUID();
        Task task = createCompletedTask();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> subtaskService.create(
                taskId,
                "Create tests",
                "Description",
                TaskStatus.PENDING
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot create unfinished subtask for a completed task.");

        verify(taskRepository).findById(taskId);
        verify(subtaskRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessExceptionWhenCreatingInProgressSubtaskForCompletedTask() {
        UUID taskId = UUID.randomUUID();
        Task task = createCompletedTask();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> subtaskService.create(
                taskId,
                "Create tests",
                "Description",
                TaskStatus.IN_PROGRESS
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot create unfinished subtask for a completed task.");

        verify(taskRepository).findById(taskId);
        verify(subtaskRepository, never()).save(any());
    }

    @Test
    void shouldCreateCompletedSubtaskForCompletedTask() {
        UUID taskId = UUID.randomUUID();
        Task task = createCompletedTask();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subtask subtask = subtaskService.create(
                taskId,
                "Review implementation",
                "Review service code",
                TaskStatus.COMPLETED
        );

        assertThat(subtask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(subtask.getCompletedAt()).isNotNull();

        verify(taskRepository).findById(taskId);
        verify(subtaskRepository).save(any(Subtask.class));
    }

    @Test
    void shouldListSubtasksByTask() {
        UUID taskId = UUID.randomUUID();
        Task task = createTask();

        Subtask firstSubtask = Subtask.create(task, "First subtask", null, TaskStatus.PENDING);
        Subtask secondSubtask = Subtask.create(task, "Second subtask", null, TaskStatus.PENDING);
        PageRequest pageable = PageRequest.of(0, 20);

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(subtaskRepository.findByTask_Id(taskId, pageable))
                .thenReturn(new PageImpl<>(List.of(firstSubtask, secondSubtask), pageable, 2));

        Page<Subtask> subtasks = subtaskService.listByTask(taskId, pageable);

        assertThat(subtasks.getContent())
                .hasSize(2)
                .containsExactly(firstSubtask, secondSubtask);
        assertThat(subtasks.getTotalElements()).isEqualTo(2);

        verify(taskRepository).existsById(taskId);
        verify(subtaskRepository).findByTask_Id(taskId, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTaskDoesNotExistWhileListingSubtasks() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThatThrownBy(() -> subtaskService.listByTask(taskId, PageRequest.of(0, 20)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found.");

        verify(taskRepository).existsById(taskId);
        verify(subtaskRepository, never()).findByTask_Id(any(), any());
    }

    @Test
    void shouldUpdateSubtaskStatus() {
        UUID subtaskId = UUID.randomUUID();
        Task task = createTask();
        Subtask subtask = Subtask.create(task, "Create tests", null, TaskStatus.PENDING);

        when(subtaskRepository.findByIdWithTask(subtaskId)).thenReturn(Optional.of(subtask));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subtask updatedSubtask = subtaskService.updateStatus(subtaskId, TaskStatus.COMPLETED);

        assertThat(updatedSubtask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(updatedSubtask.getCompletedAt()).isNotNull();

        ArgumentCaptor<Subtask> subtaskCaptor = ArgumentCaptor.forClass(Subtask.class);

        verify(subtaskRepository).findByIdWithTask(subtaskId);
        verify(subtaskRepository).save(subtaskCaptor.capture());

        assertThat(subtaskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingSubtaskWithoutStatus() {
        UUID subtaskId = UUID.randomUUID();

        assertThatThrownBy(() -> subtaskService.updateStatus(subtaskId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status is required");

        verifyNoInteractions(subtaskRepository, taskRepository);
    }

    @Test
    void shouldThrowBusinessExceptionWhenUpdatingSubtaskToUnfinishedStatusForCompletedTask() {
        UUID subtaskId = UUID.randomUUID();
        Task task = createCompletedTask();
        Subtask subtask = Subtask.create(task, "Create tests", null, TaskStatus.COMPLETED);

        when(subtaskRepository.findByIdWithTask(subtaskId)).thenReturn(Optional.of(subtask));

        assertThatThrownBy(() -> subtaskService.updateStatus(subtaskId, TaskStatus.PENDING))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot update subtask to unfinished status when task is completed.");

        verify(subtaskRepository).findByIdWithTask(subtaskId);
        verify(subtaskRepository, never()).save(any());
    }

    @Test
    void shouldUpdateSubtaskToCompletedWhenTaskIsCompleted() {
        UUID subtaskId = UUID.randomUUID();
        Task task = createCompletedTask();
        Subtask subtask = Subtask.create(task, "Create tests", null, TaskStatus.COMPLETED);

        when(subtaskRepository.findByIdWithTask(subtaskId)).thenReturn(Optional.of(subtask));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subtask updatedSubtask = subtaskService.updateStatus(subtaskId, TaskStatus.COMPLETED);

        assertThat(updatedSubtask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(updatedSubtask.getCompletedAt()).isNotNull();

        verify(subtaskRepository).findByIdWithTask(subtaskId);
        verify(subtaskRepository).save(subtask);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSubtaskDoesNotExistWhileUpdatingStatus() {
        UUID subtaskId = UUID.randomUUID();

        when(subtaskRepository.findByIdWithTask(subtaskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subtaskService.updateStatus(subtaskId, TaskStatus.COMPLETED))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Subtask not found.");

        verify(subtaskRepository).findByIdWithTask(subtaskId);
        verify(subtaskRepository, never()).save(any());
    }

    private static Task createTask() {
        AppUser user = AppUser.create(
                "Edson Junior",
                "edson.junior@email.com"
        );

        return Task.create(
                "Main task",
                "Main task description",
                TaskStatus.PENDING,
                user
        );
    }

    private static Task createCompletedTask() {
        AppUser user = AppUser.create(
                "Edson Junior",
                "edson.junior@email.com"
        );

        return Task.create(
                "Main task",
                "Main task description",
                TaskStatus.COMPLETED,
                user
        );
    }
}
