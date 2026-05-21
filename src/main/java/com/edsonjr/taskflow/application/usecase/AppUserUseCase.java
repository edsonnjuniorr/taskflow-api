package com.edsonjr.taskflow.application.usecase;

import com.edsonjr.taskflow.domain.model.AppUser;

import java.util.UUID;

public interface AppUserUseCase {

    AppUser create(String name, String email);

    AppUser findById(UUID id);
}
