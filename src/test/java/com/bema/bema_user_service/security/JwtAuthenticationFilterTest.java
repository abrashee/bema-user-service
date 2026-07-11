package com.bema.bema_user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "a".repeat(32);

    private final AccessTokenRevocationService revocationService =
            mock(AccessTokenRevocationService.class);

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(
                    new JwtTokenService(SECRET),
                    revocationService,
                    new ObjectMapper().findAndRegisterModules()
            );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mapsUserRoleToSpringAuthority() throws Exception {
        authenticate(createToken(Map.of("role", "USER")));

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("user-123", authentication.getName());
        assertEquals(
                "ROLE_USER",
                authentication.getAuthorities().iterator().next().getAuthority()
        );
    }

    @Test
    void mapsAdminRoleToSpringAuthority() throws Exception {
        authenticate(createToken(Map.of("role", "ADMIN")));

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(
                "ROLE_ADMIN",
                authentication.getAuthorities().iterator().next().getAuthority()
        );
    }

    @Test
    void mapsInternalScopeWithoutRequiringUserRole() throws Exception {
        authenticate(createToken(Map.of("scope", "internal")));

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(
                "SCOPE_INTERNAL",
                authentication.getAuthorities().iterator().next().getAuthority()
        );
    }

    @Test
    void rejectsUserTokenWithoutRole() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/users/identity/user-123");
        request.addHeader(
                "Authorization",
                "Bearer " + createToken(Map.of())
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertEquals(401, response.getStatus());
    }

    private void authenticate(String token) throws Exception {
        String tokenId = Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getId();

        when(revocationService.isRevoked(tokenId)).thenReturn(false);

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/users/identity/user-123");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertEquals(200, response.getStatus());
    }

    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject("user-123")
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 60_000L)
                )
                .signWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        ),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }
}
