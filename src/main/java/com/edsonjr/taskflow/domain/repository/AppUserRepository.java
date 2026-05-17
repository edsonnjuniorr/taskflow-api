package com.edsonjr.taskflow.domain.repository;

import com.edsonjr.taskflow.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    boolean existsByEmailIgnoreCase(String email);
}