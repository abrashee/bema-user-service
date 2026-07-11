package com.bema.bema_user_service.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {

    private static final String SECRET = "a".repeat(32);

    @Test
    void rejectsExpiredToken() {
        String token = Jwts.builder()
                .setSubject("user-123")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000L))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000L))
                .signWith(
                        Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();

        JwtTokenService jwtTokenService = new JwtTokenService(SECRET);

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtTokenService.parse(token)
        );
    }
}
