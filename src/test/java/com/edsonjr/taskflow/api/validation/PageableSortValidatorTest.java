package com.edsonjr.taskflow.api.validation;

import com.edsonjr.taskflow.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageableSortValidatorTest {

    private static final Set<String> ALLOWED_PROPERTIES = Set.of("createdAt", "title");

    @Test
    void shouldAcceptNullPageable() {
        assertThatCode(() -> PageableSortValidator.validate(null, ALLOWED_PROPERTIES))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptUnsortedPageable() {
        Pageable pageable = PageRequest.of(0, 20, Sort.unsorted());

        assertThatCode(() -> PageableSortValidator.validate(pageable, ALLOWED_PROPERTIES))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptAllowedSortProperties() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("createdAt").descending().and(Sort.by("title").ascending())
        );

        assertThatCode(() -> PageableSortValidator.validate(pageable, ALLOWED_PROPERTIES))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUnknownSortProperty() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("unknown").ascending());

        assertThatThrownBy(() -> PageableSortValidator.validate(pageable, ALLOWED_PROPERTIES))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Invalid sort property: unknown");
    }
}
