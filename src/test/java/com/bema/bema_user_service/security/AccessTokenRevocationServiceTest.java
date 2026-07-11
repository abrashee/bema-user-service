package com.bema.bema_user_service.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessTokenRevocationServiceTest {

    @Test
    void reportsRevokedTokenFromRedis() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AccessTokenRevocationService service =
                new AccessTokenRevocationService(redisTemplate);

        when(redisTemplate.hasKey("auth:revoked-access-token:revoked-id"))
                .thenReturn(true);

        assertTrue(service.isRevoked("revoked-id"));
    }

    @Test
    void reportsActiveAndMissingTokenIdsAsNotRevoked() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AccessTokenRevocationService service =
                new AccessTokenRevocationService(redisTemplate);

        when(redisTemplate.hasKey("auth:revoked-access-token:active-id"))
                .thenReturn(false);

        assertFalse(service.isRevoked("active-id"));
        assertFalse(service.isRevoked(null));
        assertFalse(service.isRevoked(" "));
    }
}
