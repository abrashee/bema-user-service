package com.bema.bema_user_service.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.bema.bema_user_service.dto.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void missingUserReturnsNotFound() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleNotFound(
                        new ResourceNotFoundException("User not found")
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("User not found");
        assertThat(response.getBody().data()).isNull();
    }

    @Test
    void invalidPathParameterReturnsBadRequest() {
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleTypeMismatch(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("Invalid request parameter");
        assertThat(response.getBody().data()).isNull();
    }
}
