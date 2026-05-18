package com.edsonjr.taskflow.domain.specification;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.model.Task;
import com.edsonjr.taskflow.domain.model.TaskStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

import static com.edsonjr.taskflow.domain.model.TaskStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskSpecificationsTest {

    @Test
    void shouldReturnConjunctionWhenStatusIsNull() {
        Root<Task> root = mockRoot();
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Task> specification = TaskSpecifications.hasStatus(null);

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(predicate).isEqualTo(conjunction);

        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void shouldReturnPredicateWhenStatusIsProvided() {
        Root<Task> root = mockRoot();
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<TaskStatus> statusPath = mockPath();
        Predicate expectedPredicate = mock(Predicate.class);

        when(root.<TaskStatus>get("status")).thenReturn(statusPath);
        when(criteriaBuilder.equal(statusPath, PENDING)).thenReturn(expectedPredicate);

        Specification<Task> specification = TaskSpecifications.hasStatus(PENDING);

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(predicate).isEqualTo(expectedPredicate);

        verify(root).get("status");
        verify(criteriaBuilder).equal(statusPath, PENDING);
        verify(criteriaBuilder, never()).conjunction();
    }

    @Test
    void shouldReturnConjunctionWhenUserIdIsNull() {
        Root<Task> root = mockRoot();
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);

        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Task> specification = TaskSpecifications.belongsToUser(null);

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(predicate).isEqualTo(conjunction);

        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void shouldReturnPredicateWhenUserIdIsProvided() {
        UUID userId = UUID.randomUUID();

        Root<Task> root = mockRoot();
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<AppUser> userPath = mockPath();
        Path<UUID> userIdPath = mockPath();
        Predicate expectedPredicate = mock(Predicate.class);

        when(root.<AppUser>get("user")).thenReturn(userPath);
        when(userPath.<UUID>get("id")).thenReturn(userIdPath);
        when(criteriaBuilder.equal(userIdPath, userId)).thenReturn(expectedPredicate);

        Specification<Task> specification = TaskSpecifications.belongsToUser(userId);

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(predicate).isEqualTo(expectedPredicate);

        verify(root).get("user");
        verify(userPath).get("id");
        verify(criteriaBuilder).equal(userIdPath, userId);
        verify(criteriaBuilder, never()).conjunction();
    }

    @SuppressWarnings("unchecked")
    private static Root<Task> mockRoot() {
        return mock(Root.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> Path<T> mockPath() {
        return mock(Path.class);
    }
}