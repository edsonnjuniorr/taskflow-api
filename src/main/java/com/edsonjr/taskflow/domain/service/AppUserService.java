package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.exception.EmailAlreadyExistsException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser create(String name, String email) {
        String normalizedEmail = normalizeEmail(email);

        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Unable to create user with provided data.");
        }

        AppUser appUser = AppUser.create(name.trim(), normalizedEmail);

        return appUserRepository.save(appUser);
    }

    @Transactional(readOnly = true)
    public AppUser findById(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}