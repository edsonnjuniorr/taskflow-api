package com.edsonjr.taskflow.api.mapper;

import com.edsonjr.taskflow.api.dto.response.AppUserResponse;
import com.edsonjr.taskflow.domain.model.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AppUserMapper {

    public AppUserResponse toResponse(AppUser appUser) {
        return new AppUserResponse(
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail()
        );
    }
}