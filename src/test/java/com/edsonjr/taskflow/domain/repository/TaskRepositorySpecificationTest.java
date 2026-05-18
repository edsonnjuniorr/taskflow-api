package com.edsonjr.taskflow.domain.repository;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.specification.TaskSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.edsonjr.taskflow.domain.model.TaskStatus.COMPLETED;
import static com.edsonjr.taskflow.domain.model.TaskStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Testcontainers
@Transactional
class TaskRepositorySpecificationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskflow_test")
            .withUsername("taskflow")
            .withPassword("taskflow");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TaskRepository taskRepository;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Test
    void shouldFindTasksByStatusAndUserIdUsingJpaSpecification() {
        AppUser targetUser = appUserRepository.save(AppUser.create("Target User", "target.spec@example.com"));
        AppUser otherUser = appUserRepository.save(AppUser.create("Other User", "other.spec@example.com"));

        Task expectedTask = taskRepository.save(Task.create("Target pending task", null, PENDING, targetUser));

        taskRepository.save(Task.create("Target completed task", null, COMPLETED, targetUser));
        taskRepository.save(Task.create("Other pending task", null, PENDING, otherUser));

        Specification<Task> specification = TaskSpecifications.hasStatus(PENDING)
                .and(TaskSpecifications.belongsToUser(targetUser.getId()));

        Page<Task> result = taskRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(expectedTask);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(PENDING);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(targetUser.getId());
    }
}
