package com.gym.crm.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private final Clock clock;
    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String jti, Instant expiration) {
        Instant now = Instant.now(clock);
        Duration tokenTtl = Duration.between(now, expiration);

        if (tokenTtl.isPositive()) {
            String key = BLACKLIST_PREFIX + jti;
            redisTemplate.opsForValue().set(key, "true", tokenTtl);
        }
    }

    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
    }

}
