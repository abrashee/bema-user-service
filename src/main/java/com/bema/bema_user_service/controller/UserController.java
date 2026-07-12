package com.bema.bema_user_service.controller;

import com.bema.bema_user_service.audit.UserAuditLogger;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bema.bema_user_service.dto.common.ApiResponse;
import com.bema.bema_user_service.dto.internal.IdentityUserSyncRequest;
import com.bema.bema_user_service.dto.user.UserCreateDto;
import com.bema.bema_user_service.dto.user.UserDto;
import com.bema.bema_user_service.dto.user.UserUpdateDto;
import com.bema.bema_user_service.service.serviceInterface.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserAuditLogger auditLogger;

    public UserController(
            UserService userService,
            UserAuditLogger auditLogger
    ) {
        this.userService = userService;
        this.auditLogger = auditLogger;
    }

    // ─────────────────────────────────────────────
    // ADMIN: list all users
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("Fetched all users", userService.getAllUsers())
        );
    }

    // ─────────────────────────────────────────────
    // OWNER OR INTERNAL: get user by ID
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            Authentication authentication,
            @PathVariable @Positive Long id
    ) {
        UserDto user = userService.getUserById(id);
        requireOwnerOrInternal(authentication, user.identityId());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fetched user with id: " + id,
                        user
                )
        );
    }

    // ─────────────────────────────────────────────
    // OWNER OR INTERNAL: get user by identity ID
    // ─────────────────────────────────────────────
    @GetMapping("/identity/{identityId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByIdentityId(
            Authentication authentication,
            @PathVariable String identityId
    ) {
        requireOwnerOrInternal(authentication, identityId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fetched user with identityId: " + identityId,
                        userService.getUserByIdentityId(identityId)
                )
        );
    }

    // ─────────────────────────────────────────────
    // INTERNAL: create user profile (called by identity-service)
    // ─────────────────────────────────────────────
        @PostMapping("/internal")
        public ResponseEntity<ApiResponse<UserDto>> createInternalUser(
                Authentication authentication,
                @Valid @RequestBody IdentityUserSyncRequest request
        ) {
        requireInternal(authentication);
        UserDto user = userService.createUser(
                new UserCreateDto(
                        request.identityId(),
                        request.name()
                )
        );

        auditLogger.success(
                "USER_PROFILE_CREATE",
                actorId(authentication),
                user.id(),
                user.identityId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Internal user created", user)
        );
        }

    // ─────────────────────────────────────────────
    // OWNER OR INTERNAL: update profile
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            Authentication authentication,
            @PathVariable @Positive Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto
    ) {
        UserDto existing = userService.getUserById(id);
        requireOwnerOrInternal(authentication, existing.identityId());

        UserDto updated = userService.updateUser(id, userUpdateDto);

        auditLogger.success(
                "USER_PROFILE_UPDATE",
                actorId(authentication),
                updated.id(),
                updated.identityId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("User updated", updated)
        );
    }

    // ─────────────────────────────────────────────
    // OWNER OR INTERNAL: delete profile
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(Authentication authentication, @PathVariable @Positive Long id) {
        UserDto existing = userService.getUserById(id);
        requireOwnerOrInternal(authentication, existing.identityId());
        userService.deleteUser(id);

        auditLogger.success(
                "USER_PROFILE_DELETE",
                actorId(authentication),
                id,
                existing.identityId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }

    private void requireInternal(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "SCOPE_INTERNAL".equals(authority.getAuthority()))) {
            auditLogger.denied(
                    "USER_INTERNAL_ACCESS",
                    actorId(authentication),
                    null,
                    "MISSING_INTERNAL_SCOPE"
            );
            throw new AccessDeniedException("Forbidden");
        }
    }

    private void requireOwnerOrInternal(Authentication authentication, String identityId) {
        boolean internal = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "SCOPE_INTERNAL".equals(authority.getAuthority()));
        boolean owner = authentication != null && identityId.equals(authentication.getName());

        if (!internal && !owner) {
            auditLogger.denied(
                    "USER_RESOURCE_ACCESS",
                    actorId(authentication),
                    identityId,
                    "NOT_OWNER_OR_INTERNAL"
            );
            throw new AccessDeniedException("Forbidden");
        }
    }

    private String actorId(Authentication authentication) {
        return authentication == null
                ? null
                : authentication.getName();
    }
}
