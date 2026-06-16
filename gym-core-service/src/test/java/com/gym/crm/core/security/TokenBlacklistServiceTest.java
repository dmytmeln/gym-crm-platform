package com.gym.crm.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    private static final String JTI = "test-jti";
    private static final String KEY = "blacklist:token:" + JTI;
    private static final Instant INSTANT = Instant.parse("2024-01-01T00:00:00Z");

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(INSTANT, UTC);
        blacklistService = new TokenBlacklistService(fixedClock, redisTemplate);
    }

    @Test
    void shouldBlacklistTokenWhenTtlIsPositive() {
        Instant expiration = INSTANT.plusSeconds(10);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        blacklistService.blacklistToken(JTI, expiration);

        verify(valueOperations).set(eq(KEY), eq("true"), any(Duration.class));
    }

    @Test
    void shouldNotBlacklistTokenWhenTtlIsNegative() {
        Instant expiration = INSTANT.minusSeconds(10);

        blacklistService.blacklistToken(JTI, expiration);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldNotBlacklistTokenWhenTtlIsZero() {
        Instant expiration = INSTANT.plusSeconds(0);

        blacklistService.blacklistToken(JTI, expiration);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldReturnTrueWhenTokenIsBlacklisted() {
        when(redisTemplate.hasKey(KEY)).thenReturn(true);

        boolean actual = blacklistService.isBlacklisted(JTI);

        assertTrue(actual);
        verify(redisTemplate).hasKey(KEY);
    }

    @Test
    void shouldReturnFalseWhenTokenIsNotBlacklisted() {
        when(redisTemplate.hasKey(KEY)).thenReturn(false);

        boolean actual = blacklistService.isBlacklisted(JTI);

        assertFalse(actual);
        verify(redisTemplate).hasKey(KEY);
    }

}
