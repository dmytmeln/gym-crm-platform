package com.gym.crm.core.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    private static final String IP = "192.168.1.100";
    private static final String ATTEMPTS_KEY = "login:attempts:" + IP;
    private static final String BLOCKED_KEY = "login:blocked:" + IP;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LoginAttemptService service;

    @Test
    void shouldReturnTrueWhenBlockedKeyExists() {
        when(redisTemplate.hasKey(BLOCKED_KEY)).thenReturn(true);

        boolean result = service.isBlocked(IP);

        assertTrue(result);
        verify(redisTemplate).hasKey(BLOCKED_KEY);
    }

    @Test
    void shouldReturnFalseWhenBlockedKeyDoesNotExist() {
        when(redisTemplate.hasKey(BLOCKED_KEY)).thenReturn(false);

        boolean result = service.isBlocked(IP);

        assertFalse(result);
        verify(redisTemplate).hasKey(BLOCKED_KEY);
    }

    @Test
    void shouldIncrementAttemptsAndSetExpiryOnFirstFailure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(1L);

        service.loginFailed(IP);

        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(redisTemplate).expire(eq(ATTEMPTS_KEY), any(Duration.class));
        verify(valueOperations, never()).set(any(String.class), any(String.class), any(Duration.class));
    }

    @Test
    void shouldIncrementWithoutSettingExpiryOnSubsequentFailures() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(2L);

        service.loginFailed(IP);

        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(redisTemplate, never()).expire(any(String.class), any(Duration.class));
        verify(valueOperations, never()).set(any(String.class), any(String.class), any(Duration.class));
    }

    @Test
    void shouldBlockIpAndClearAttemptsWhenThresholdReached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(3L);

        service.loginFailed(IP);

        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(redisTemplate, never()).expire(any(String.class), any(Duration.class));
        verify(valueOperations).set(eq(BLOCKED_KEY), eq("true"), any(Duration.class));
        verify(redisTemplate).delete(ATTEMPTS_KEY);
    }

    @Test
    void shouldThrowNullPointerWhenAttemptsIsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(null);

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.loginFailed(IP));

        assertEquals("Unexpected null from Redis increment(). This code must not run inside Redis pipeline/transaction", exception.getMessage());
        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(redisTemplate, never()).expire(any(String.class), any(Duration.class));
        verify(valueOperations, never()).set(any(String.class), any(String.class), any(Duration.class));
        verify(redisTemplate, never()).delete(any(String.class));
    }

    @Test
    void shouldClearAttemptsOnLoginSuccess() {
        service.loginSucceeded(IP);

        verify(redisTemplate).delete(ATTEMPTS_KEY);
    }

}
