package com.bema.bema_user_service.dto.user;

import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @Size(min = 2, max = 100) String name
) {}
