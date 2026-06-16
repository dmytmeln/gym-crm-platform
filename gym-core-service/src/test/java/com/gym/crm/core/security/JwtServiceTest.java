package com.gym.crm.core.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET_KEY = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
    private static final long EXPIRATION_MS = 3600000;
    private static final String USERNAME = "liam.miller";

    private final JwtService service = new JwtService(SECRET_KEY, EXPIRATION_MS);

    @Test
    void shouldGenerateAccessToken() {
        String actual = service.generateAccessToken(USERNAME);

        assertNotNull(actual);
        assertFalse(actual.isBlank());
    }

    @Test
    void shouldGenerateAccessTokenAndGetPayload() {
        String token = service.generateAccessToken(USERNAME);

        JwtPayload payload = service.getPayload(token);

        assertEquals(USERNAME, payload.username());
        assertNotNull(payload.jti());
        assertNotNull(payload.expiration());
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        String token = service.generateAccessToken(USERNAME);

        boolean actual = service.isTokenValid(token);

        assertTrue(actual);
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() {
        JwtService expiredService = new JwtService(SECRET_KEY, -1000);
        String token = expiredService.generateAccessToken(USERNAME);

        boolean actual = service.isTokenValid(token);

        assertFalse(actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid.jwt.token", "another.bad.token", "  "})
    void shouldReturnFalseWhenTokenIsInvalid(String invalidToken) {
        boolean actual = service.isTokenValid(invalidToken);

        assertFalse(actual);
    }

}
