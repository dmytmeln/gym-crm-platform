package com.gym.crm.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final String ATTEMPTS_PREFIX = "login:attempts:";
    private static final String BLOCKED_PREFIX = "login:blocked:";
    private static final Duration ATTEMPT_TTL = Duration.ofHours(1);
    private static final Duration BLOCK_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public boolean isBlocked(String ip) {
        String blockedKey = BLOCKED_PREFIX + ip;
        return redisTemplate.hasKey(blockedKey);
    }

    public void loginFailed(String ip) {
        String attemptsKey = ATTEMPTS_PREFIX + ip;
        long attempts = incrementAndGet(attemptsKey);

        boolean isFirstAttempt = attempts == 1;
        if (isFirstAttempt) {
            redisTemplate.expire(attemptsKey, ATTEMPT_TTL);
            return;
        }

        boolean hasExceededMaxAttempts = attempts >= MAX_ATTEMPTS;
        if (!hasExceededMaxAttempts) {
            return;
        }

        String blockedKey = BLOCKED_PREFIX + ip;
        redisTemplate.opsForValue().set(blockedKey, "true", BLOCK_TTL);
        redisTemplate.delete(attemptsKey);
    }

    public void loginSucceeded(String ip) {
        redisTemplate.delete(ATTEMPTS_PREFIX + ip);
    }

    private long incrementAndGet(String attemptsKey) {
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        return Objects.requireNonNull(attempts, "Unexpected null from Redis increment(). This code must not run inside Redis pipeline/transaction");
    }

}
