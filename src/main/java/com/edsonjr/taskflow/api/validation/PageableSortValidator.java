package com.edsonjr.taskflow.api.validation;

import com.edsonjr.taskflow.exception.InvalidRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageableSortValidator {

    private PageableSortValidator() {
    }

    public static void validate(Pageable pageable, Set<String> allowedProperties) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return;
        }

        for (Sort.Order order : pageable.getSort()) {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidRequestException("Invalid sort property: " + order.getProperty());
            }
        }
    }
}
