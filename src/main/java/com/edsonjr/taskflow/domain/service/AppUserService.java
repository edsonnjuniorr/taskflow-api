package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.exception.EmailAlreadyExistsException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser create(String name, String email) {
        AppUser appUser = AppUser.create(name, email);

        if (appUserRepository.existsByEmailIgnoreCase(appUser.getEmail())) {
            throw new EmailAlreadyExistsException("Unable to create user with provided data.");
        }

        try {
            return appUserRepository.saveAndFlush(appUser);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException("Unable to create user with provided data.");
        }
    }

    @Transactional(readOnly = true)
    public AppUser findById(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

}