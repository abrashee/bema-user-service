package com.bema.bema_user_service.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.bema.bema_user_service.audit.UserAuditLogger;
import com.bema.bema_user_service.dto.internal.IdentityUserSyncRequest;
import com.bema.bema_user_service.dto.user.UserDto;
import com.bema.bema_user_service.dto.user.UserUpdateDto;
import com.bema.bema_user_service.service.serviceInterface.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class UserControllerAuditTest {

    private UserService userService;
    private UserAuditLogger auditLogger;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        auditLogger = mock(UserAuditLogger.class);
        controller = new UserController(userService, auditLogger);
    }

    @Test
    void auditsInternalProfileCreation() {
        UserDto created = new UserDto(
                10L,
                "identity-10",
                "Test User"
        );

        when(userService.createUser(
                new com.bema.bema_user_service.dto.user.UserCreateDto(
                        "identity-10",
                        "Test User"
                )
        )).thenReturn(created);

        controller.createInternalUser(
                internalAuthentication(),
                new IdentityUserSyncRequest(
                        "identity-10",
                        "user@example.test",
                        "Test User"
                )
        );

        verify(auditLogger).success(
                "USER_PROFILE_CREATE",
                "identity-service",
                10L,
                "identity-10"
        );
    }

    @Test
    void auditsProfileUpdate() {
        UserDto existing = new UserDto(
                10L,
                "identity-10",
                "Old Name"
        );
        UserDto updated = new UserDto(
                10L,
                "identity-10",
                "New Name"
        );

        when(userService.getUserById(10L)).thenReturn(existing);
        when(userService.updateUser(
                10L,
                new UserUpdateDto("New Name")
        )).thenReturn(updated);

        controller.updateUser(
                userAuthentication("identity-10"),
                10L,
                new UserUpdateDto("New Name")
        );

        verify(auditLogger).success(
                "USER_PROFILE_UPDATE",
                "identity-10",
                10L,
                "identity-10"
        );
    }

    @Test
    void auditsProfileDeletion() {
        UserDto existing = new UserDto(
                10L,
                "identity-10",
                "Test User"
        );

        when(userService.getUserById(10L)).thenReturn(existing);

        controller.deleteUser(
                userAuthentication("identity-10"),
                10L
        );

        verify(auditLogger).success(
                "USER_PROFILE_DELETE",
                "identity-10",
                10L,
                "identity-10"
        );
    }

    @Test
    void auditsDeniedOwnerAccess() {
        UserDto existing = new UserDto(
                10L,
                "identity-10",
                "Test User"
        );

        when(userService.getUserById(10L)).thenReturn(existing);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.getUserById(
                        userAuthentication("different-user"),
                        10L
                )
        );

        verify(auditLogger).denied(
                "USER_RESOURCE_ACCESS",
                "different-user",
                "identity-10",
                "NOT_OWNER_OR_INTERNAL"
        );
    }

    @Test
    void auditsDeniedInternalEndpointAccess() {
        assertThrows(
                AccessDeniedException.class,
                () -> controller.createInternalUser(
                        userAuthentication("identity-10"),
                        new IdentityUserSyncRequest(
                                "identity-10",
                                "user@example.test",
                                "Test User"
                        )
                )
        );

        verify(auditLogger).denied(
                "USER_INTERNAL_ACCESS",
                "identity-10",
                null,
                "MISSING_INTERNAL_SCOPE"
        );
    }

    private UsernamePasswordAuthenticationToken internalAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "identity-service",
                null,
                List.of(new SimpleGrantedAuthority("SCOPE_INTERNAL"))
        );
    }

    private UsernamePasswordAuthenticationToken userAuthentication(
            String identityId
    ) {
        return new UsernamePasswordAuthenticationToken(
                identityId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
