package com.bema.bema_user_service.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenRevocationService {

    private static final String KEY_PREFIX = "auth:revoked-access-token:";

    private final StringRedisTemplate redisTemplate;

    public AccessTokenRevocationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRevoked(String tokenId) {
        return tokenId != null
                && !tokenId.isBlank()
                && Boolean.TRUE.equals(
                        redisTemplate.hasKey(KEY_PREFIX + tokenId)
                );
    }
}
