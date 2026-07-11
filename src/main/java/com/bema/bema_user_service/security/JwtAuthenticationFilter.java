package com.bema.bema_user_service.security;

import com.bema.bema_user_service.dto.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AccessTokenRevocationService accessTokenRevocationService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            AccessTokenRevocationService accessTokenRevocationService,
            ObjectMapper objectMapper
    ) {
        this.jwtTokenService = jwtTokenService;
        this.accessTokenRevocationService = accessTokenRevocationService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var claims = jwtTokenService.parse(token);
                String tokenId = claims.getId();

                if (tokenId == null
                        || tokenId.isBlank()
                        || accessTokenRevocationService.isRevoked(tokenId)) {
                    throw new IllegalArgumentException("Revoked or non-revocable token");
                }

                String subject = claims.getSubject();
                boolean internalToken = "internal".equalsIgnoreCase(
                        claims.get("scope", String.class)
                );

                List<SimpleGrantedAuthority> authorities;

                if (internalToken) {
                    authorities = List.of(
                            new SimpleGrantedAuthority("SCOPE_INTERNAL")
                    );
                } else {
                    String role = claims.get("role", String.class);

                    if (!"USER".equals(role) && !"ADMIN".equals(role)) {
                        throw new IllegalArgumentException(
                                "Missing or unsupported role"
                        );
                    }

                    authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );
                }

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                subject,
                                null,
                                authorities
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), ApiResponse.error("Invalid or expired token"));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
