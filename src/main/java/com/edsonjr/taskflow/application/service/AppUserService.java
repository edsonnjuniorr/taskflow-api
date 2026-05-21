package com.edsonjr.taskflow.application.service;

import com.edsonjr.taskflow.application.usecase.AppUserUseCase;
import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.exception.EmailAlreadyExistsException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AppUserService implements AppUserUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserService.class);

    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Unable to create user with provided data.";

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    @Override
    public AppUser create(String name, String email) {
        AppUser appUser = AppUser.create(name, email);

        if (appUserRepository.existsByEmailIgnoreCase(appUser.getEmail())) {
            LOGGER.warn("App user creation rejected because email already exists.");

            throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS_MESSAGE);
        }

        try {
            AppUser savedAppUser = appUserRepository.saveAndFlush(appUser);

            LOGGER.info("App user created successfully. userId={}", savedAppUser.getId());

            return savedAppUser;
        } catch (DataIntegrityViolationException exception) {
            LOGGER.warn(
                    "App user creation rejected by database unique constraint."
            );

            throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS_MESSAGE, exception);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public AppUser findById(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.debug("App user not found. userId={}", id);

                    return new NotFoundException("User not found.");
                });
    }
}
