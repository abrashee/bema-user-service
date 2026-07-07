// dto/auth/AuthResponseDto.java
package com.bema.bema_user_service.dto.auth;

import com.bema.bema_user_service.dto.user.UserDto;

public record AuthResponseDto(
        String token,
        UserDto user
) {}