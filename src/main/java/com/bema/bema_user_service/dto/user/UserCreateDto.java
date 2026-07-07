package com.bema.bema_user_service.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
        @NotBlank String identityId,
        @NotBlank @Size(min = 2, max = 100) String name
) {}
