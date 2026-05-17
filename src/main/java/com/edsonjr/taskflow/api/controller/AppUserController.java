package com.edsonjr.taskflow.api.controller;

import com.edsonjr.taskflow.api.dto.request.CreateAppUserRequest;
import com.edsonjr.taskflow.api.dto.response.AppUserResponse;
import com.edsonjr.taskflow.api.mapper.AppUserMapper;
import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class AppUserController {

    private final AppUserService appUserService;
    private final AppUserMapper appUserMapper;

    public AppUserController(
            AppUserService appUserService,
            AppUserMapper appUserMapper
    ) {
        this.appUserService = appUserService;
        this.appUserMapper = appUserMapper;
    }

    @PostMapping
    public ResponseEntity<AppUserResponse> create(
            @Valid @RequestBody CreateAppUserRequest request
    ) {
        AppUser createdUser = appUserService.create(request.name(), request.email());
        AppUserResponse response = appUserMapper.toResponse(createdUser);

        URI location = URI.create("/usuarios/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponse> findById(@PathVariable UUID id) {
        AppUser appUser = appUserService.findById(id);

        return ResponseEntity.ok(appUserMapper.toResponse(appUser));
    }
}