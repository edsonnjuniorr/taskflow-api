package com.edsonjr.taskflow.api.dto.response;

import java.util.UUID;

public record AppUserResponse(
        UUID id,
        String name,
        String email
) {
}