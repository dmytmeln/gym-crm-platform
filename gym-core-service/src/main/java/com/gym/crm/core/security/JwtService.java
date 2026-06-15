package com.gym.crm.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.io.Decoders.BASE64;
import static java.util.Collections.emptyMap;

@Component
@Slf4j
public class JwtService {

    private final String secretKey;
    private final long accessTokenExpirationMs;

    public JwtService(@Value("${app.security.jwt.secret-key}") String secretKey,
                      @Value("${app.security.jwt.access-expiration-ms}") long accessTokenExpirationMs) {
        this.secretKey = secretKey;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateAccessToken(String username) {
        return generateToken(emptyMap(), username, accessTokenExpirationMs);
    }

    public JwtPayload getPayload(String token) {
        Claims claims = extractAllClaims(token);

        return new JwtPayload(claims.getSubject(), claims.getId(), claims.getExpiration());
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ignored) {
            return false;
        }
    }

    private String generateToken(Map<String, ?> extraClaims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey(), HS256)
                .compact();
    }

    private void parseToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parse(token);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}