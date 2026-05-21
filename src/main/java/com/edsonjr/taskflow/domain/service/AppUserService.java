package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.AppUser;

import java.util.UUID;

public interface AppUserService {

    AppUser create(String name, String email);

    AppUser findById(UUID id);
}
