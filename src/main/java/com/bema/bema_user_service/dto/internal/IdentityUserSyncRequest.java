package com.bema.bema_user_service.dto.internal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdentityUserSyncRequest(
        @NotBlank String identityId,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 100) String name
) {}
