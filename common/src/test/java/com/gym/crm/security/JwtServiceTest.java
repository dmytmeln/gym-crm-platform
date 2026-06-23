package com.gym.crm.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET_KEY = "aGVsbG9oZWxsb2hlbGxvaGVsbG9oZWxsb2hlbGxvaGVsbG8=";
    private static final long EXPIRATION_MS = 60_000;
    private static final String USERNAME = "test-user";

    private final JwtService service = new JwtService(SECRET_KEY, EXPIRATION_MS);

    @Test
    void shouldGenerateValidTokenForUser() {
        String actual = service.generateAccessToken(USERNAME);

        assertThat(actual).isNotBlank();
        assertThat(service.isTokenValid(actual)).isTrue();
    }

    @Test
    void shouldReturnPayloadForGeneratedToken() {
        String token = service.generateAccessToken(USERNAME);

        JwtPayload actual = service.getPayload(token);

        assertThat(actual.username()).isEqualTo(USERNAME);
        assertThat(actual.jti()).isNotBlank();
        assertThat(actual.expiration()).isNotNull();
    }

    @Test
    void shouldRejectMalformedToken() {
        boolean actual = service.isTokenValid("invalid-token");

        assertThat(actual).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService expiredService = new JwtService(SECRET_KEY, -1000);
        String token = expiredService.generateAccessToken(USERNAME);

        boolean actual = expiredService.isTokenValid(token);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenGettingPayloadForMalformedToken() {
        String token = "invalid-token";

        assertThrows(JwtException.class, () -> service.getPayload(token));
    }

    @Test
    void shouldThrowExceptionWhenGettingPayloadForExpiredToken() {
        JwtService expiredService = new JwtService(SECRET_KEY, -1000);
        String token = expiredService.generateAccessToken(USERNAME);

        assertThrows(JwtException.class, () -> expiredService.getPayload(token));
    }

    @Test
    void shouldReturnFalseForNullToken() {
        boolean actual = service.isTokenValid(null);

        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalseForEmptyToken() {
        boolean actual = service.isTokenValid("");

        assertThat(actual).isFalse();
    }

}
