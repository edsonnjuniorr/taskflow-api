package com.edsonjr.taskflow.integration;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Subtask;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.domain.repository.TaskRepository;
import com.edsonjr.taskflow.domain.specification.TaskSpecifications;
import com.edsonjr.taskflow.support.PostgreSQLIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import static com.edsonjr.taskflow.domain.model.TaskStatus.COMPLETED;
import static com.edsonjr.taskflow.domain.model.TaskStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("postgres")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Transactional
class PostgreSQLPersistenceIntegrationTest extends PostgreSQLIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldApplyFlywayMigrationsAndPersistDomainModelInPostgreSQL() {
        AppUser user = AppUser.create("John Doe", "john.doe.postgres@example.com");
        entityManager.persist(user);

        Task task = Task.create("Create API", "Validate PostgreSQL mapping", user);
        entityManager.persist(task);

        Subtask subtask = Subtask.create(task, "Create entities", "Validate JPA entities");
        entityManager.persist(subtask);

        entityManager.flush();

        assertThat(user.getId()).isNotNull();
        assertThat(task.getId()).isNotNull();
        assertThat(subtask.getId()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(PENDING);
        assertThat(subtask.getStatus()).isEqualTo(PENDING);
    }

    @Test
    void shouldEnforceUniqueEmailConstraintInPostgreSQL() {
        AppUser firstUser = AppUser.create("John Doe", "duplicated.postgres@example.com");
        AppUser secondUser = AppUser.create("Jane Doe", "duplicated.postgres@example.com");

        entityManager.persist(firstUser);
        entityManager.flush();

        assertThatThrownBy(() -> {
            entityManager.persist(secondUser);
            entityManager.flush();
        })
                .isInstanceOfAny(
                        PersistenceException.class,
                        DataIntegrityViolationException.class,
                        ConstraintViolationException.class
                );
    }

    @Test
    void shouldFindTasksByStatusAndUserIdUsingJpaSpecificationInPostgreSQL() {
        AppUser targetUser = appUserRepository.save(AppUser.create("Target User", "target.postgres@example.com"));
        AppUser otherUser = appUserRepository.save(AppUser.create("Other User", "other.postgres@example.com"));

        Task expectedTask = taskRepository.save(Task.create("Target pending task", null, PENDING, targetUser));

        taskRepository.save(Task.create("Target completed task", null, COMPLETED, targetUser));
        taskRepository.save(Task.create("Other pending task", null, PENDING, otherUser));

        Specification<Task> specification = TaskSpecifications.hasStatus(PENDING)
                .and(TaskSpecifications.belongsToUser(targetUser.getId()));

        Page<Task> result = taskRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(expectedTask);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(targetUser.getId());
    }
}
