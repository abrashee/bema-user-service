package com.bema.bema_user_service.dto.user;

public record UserDto(
        Long id,
        String identityId,
        String name
) {}