package com.bema.bema_user_service.dto.common;

import java.time.LocalDateTime;

public record ApiResponse<T> (
    LocalDateTime timestamp,
    String message,
    T data
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(LocalDateTime.now(), message, null);
    }

}
