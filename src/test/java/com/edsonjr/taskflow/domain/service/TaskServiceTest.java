package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.domain.repository.SubtaskRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.exception.BusinessException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.edsonjr.taskflow.domain.model.TaskStatus.COMPLETED;
import static com.edsonjr.taskflow.domain.model.TaskStatus.IN_PROGRESS;
import static com.edsonjr.taskflow.domain.model.TaskStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private AppUserRepository appUserRepository;
    private TaskService taskService;
    private SubtaskRepository subtaskRepository;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        appUserRepository = mock(AppUserRepository.class);
        subtaskRepository = mock(SubtaskRepository.class);

        taskService = new TaskService(
                taskRepository,
                appUserRepository,
                subtaskRepository
        );
    }

    @Test
    void shouldCreateTaskWithPendingStatusWhenStatusIsNotProvided() {
        AppUser user = AppUser.create("Edson", "edson@example.com");

        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task createdTask = taskService.create(
                "Implement task creation",
                "Create POST /tarefas endpoint",
                user.getId(),
                null
        );

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertThat(createdTask).isSameAs(savedTask);
        assertThat(savedTask.getTitle()).isEqualTo("Implement task creation");
        assertThat(savedTask.getDescription()).isEqualTo("Create POST /tarefas endpoint");
        assertThat(savedTask.getStatus()).isEqualTo(PENDING);
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getCompletedAt()).isNull();
        assertThat(savedTask.getUser()).isEqualTo(user);
    }

    @Test
    void shouldCreateCompletedTaskWithCompletionDate() {
        AppUser user = AppUser.create("Edson", "edson@example.com");

        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.create(
                "Finish task module",
                null,
                user.getId(),
                COMPLETED
        );

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertThat(savedTask.getStatus()).isEqualTo(COMPLETED);
        assertThat(savedTask.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create("Implement task creation", null, userId, PENDING))
                .isInstanceOf(NotFoundException.class);

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldListTasksUsingFiltersAndPagination() {
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Implement filters", null, PENDING, user);
        PageRequest pageable = PageRequest.of(0, 10);

        when(taskRepository.findAll(anyTaskSpecification(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(task), pageable, 1));

        var tasks = taskService.list(PENDING, user.getId(), pageable);

        assertThat(tasks.getContent()).containsExactly(task);

        verify(taskRepository).findAll(anyTaskSpecification(), eq(pageable));
    }

    @Test
    void shouldUpdateTaskStatusWhenTaskExistsAndHasNoUnfinishedSubtasks() {
        UUID taskId = UUID.randomUUID();
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Finish task module", null, PENDING, user);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(subtaskRepository.existsByTask_IdAndStatusNot(taskId, COMPLETED)).thenReturn(false);

        Task updatedTask = taskService.updateStatus(taskId, COMPLETED);

        assertThat(updatedTask).isSameAs(task);
        assertThat(task.getStatus()).isEqualTo(COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();

        verify(taskRepository).findById(taskId);
        verify(subtaskRepository).existsByTask_IdAndStatusNot(taskId, COMPLETED);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldUpdateTaskStatusWithoutCheckingSubtasksWhenStatusIsNotCompleted() {
        UUID taskId = UUID.randomUUID();
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Start task module", null, PENDING, user);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Task updatedTask = taskService.updateStatus(taskId, IN_PROGRESS);

        assertThat(updatedTask).isSameAs(task);
        assertThat(task.getStatus()).isEqualTo(IN_PROGRESS);
        assertThat(task.getCompletedAt()).isNull();

        verify(taskRepository).findById(taskId);
        verifyNoInteractions(subtaskRepository);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTaskDoesNotExist() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(taskId, COMPLETED))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found.");

        verify(taskRepository).findById(taskId);
        verifyNoInteractions(subtaskRepository);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenTaskHasUnfinishedSubtasks() {
        UUID taskId = UUID.randomUUID();
        AppUser user = AppUser.create("Edson", "edson@example.com");
        Task task = Task.create("Finish task module", null, PENDING, user);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(subtaskRepository.existsByTask_IdAndStatusNot(taskId, COMPLETED)).thenReturn(true);

        assertThatThrownBy(() -> taskService.updateStatus(taskId, COMPLETED))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Task cannot be completed because it has unfinished subtasks.");

        assertThat(task.getStatus()).isEqualTo(PENDING);
        assertThat(task.getCompletedAt()).isNull();

        verify(taskRepository).findById(taskId);
        verify(subtaskRepository).existsByTask_IdAndStatusNot(taskId, COMPLETED);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @SuppressWarnings("unchecked")
    private static Specification<Task> anyTaskSpecification() {
        return any(Specification.class);
    }
}
