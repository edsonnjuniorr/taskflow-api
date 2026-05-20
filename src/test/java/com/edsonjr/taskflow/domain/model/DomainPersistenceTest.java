package com.edsonjr.taskflow.domain.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@ActiveProfiles("test")
@Transactional
class DomainPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAppUserTaskAndSubtask() {
        AppUser user = AppUser.create("John Doe", "john.doe@example.com");
        entityManager.persist(user);

        Task task = Task.create("Create API", "Build domain model", user);
        entityManager.persist(task);

        Subtask subtaskTest = Subtask.create(task, "Create entities", "Implement JPA entities");
        entityManager.persist(subtaskTest);

        entityManager.flush();

        assertThat(user.getId()).isNotNull();
        assertThat(task.getId()).isNotNull();
        assertThat(subtaskTest.getId()).isNotNull();

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(subtaskTest.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(subtaskTest.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFailWhenEmailIsDuplicated() {
        AppUser firstUser = AppUser.create("John Doe", "duplicated@example.com");
        AppUser secondUser = AppUser.create("Jane Doe", "duplicated@example.com");

        entityManager.persist(firstUser);
        entityManager.flush();

        assertPersistenceFailure(() -> {
            entityManager.persist(secondUser);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenTaskHasNoUser() {
        AppUser user = AppUser.create("John Doe", "john.task@example.com");
        entityManager.persist(user);
        entityManager.flush();

        Task task = Task.create("Task without user", null, user);
        ReflectionTestUtils.setField(task, "user", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(task);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenSubtaskHasNoTask() {
        AppUser user = AppUser.create("John Doe", "john.subtask@example.com");
        entityManager.persist(user);

        Task task = Task.create("Parent task", null, user);
        entityManager.persist(task);
        entityManager.flush();

        Subtask subtaskTest = Subtask.create(task, "Subtask without task", null);
        ReflectionTestUtils.setField(subtaskTest, "task", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(subtaskTest);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenAppUserNameIsNull() {
        AppUser user = AppUser.create("John Doe", "john.null.name@example.com");
        ReflectionTestUtils.setField(user, "name", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(user);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenAppUserEmailIsNull() {
        AppUser user = AppUser.create("John Doe", "john.null.email@example.com");
        ReflectionTestUtils.setField(user, "email", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(user);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenTaskTitleIsNull() {
        AppUser user = AppUser.create("John Doe", "john.task.title@example.com");
        entityManager.persist(user);
        entityManager.flush();

        Task task = Task.create("Task title", null, user);
        ReflectionTestUtils.setField(task, "title", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(task);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenTaskStatusIsNull() {
        AppUser user = AppUser.create("John Doe", "john.task.status@example.com");
        entityManager.persist(user);
        entityManager.flush();

        Task task = Task.create("Task status", null, user);
        ReflectionTestUtils.setField(task, "status", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(task);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenTaskCreatedAtIsNull() {
        AppUser user = AppUser.create("John Doe", "john.task.created@example.com");
        entityManager.persist(user);
        entityManager.flush();

        Task task = Task.create("Task created at", null, user);
        ReflectionTestUtils.setField(task, "createdAt", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(task);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenSubtaskTitleIsNull() {
        AppUser user = AppUser.create("John Doe", "john.subtask.title@example.com");
        entityManager.persist(user);

        Task task = Task.create("Parent task", null, user);
        entityManager.persist(task);
        entityManager.flush();

        Subtask subtaskTest = Subtask.create(task, "Subtask title", null);
        ReflectionTestUtils.setField(subtaskTest, "title", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(subtaskTest);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenSubtaskStatusIsNull() {
        AppUser user = AppUser.create("John Doe", "john.subtask.status@example.com");
        entityManager.persist(user);

        Task task = Task.create("Parent task", null, user);
        entityManager.persist(task);
        entityManager.flush();

        Subtask subtaskTest = Subtask.create(task, "Subtask status", null);
        ReflectionTestUtils.setField(subtaskTest, "status", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(subtaskTest);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenSubtaskCreatedAtIsNull() {
        AppUser user = AppUser.create("John Doe", "john.subtask.created@example.com");
        entityManager.persist(user);

        Task task = Task.create("Parent task", null, user);
        entityManager.persist(task);
        entityManager.flush();

        Subtask subtaskTest = Subtask.create(task, "Subtask created at", null);
        ReflectionTestUtils.setField(subtaskTest, "createdAt", null);

        assertPersistenceFailure(() -> {
            entityManager.persist(subtaskTest);
            entityManager.flush();
        });
    }

    @Test
    void shouldFailWhenAppUserEmailFormatIsInvalid() {
        AppUser user = AppUser.create("John Doe", "invalid-email");

        assertPersistenceFailure(() -> {
            entityManager.persist(user);
            entityManager.flush();
        });
    }

    private static void assertPersistenceFailure(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfAny(
                        PersistenceException.class,
                        DataIntegrityViolationException.class,
                        ConstraintViolationException.class
                );
    }
}
