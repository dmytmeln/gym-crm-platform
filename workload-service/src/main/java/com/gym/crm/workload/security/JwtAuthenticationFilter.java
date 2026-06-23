package com.gym.crm.workload.security;

import com.gym.crm.security.JwtPayload;
import com.gym.crm.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        getJwtToken(request)
                .filter(jwtService::isTokenValid)
                .map(jwtService::getPayload)
                .map(payload -> createAuthentication(payload, request))
                .ifPresent(this::setAuthenticationInSecurityContext);

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(JwtPayload payload, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(payload.username(),
                null,
                Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return authentication;
    }

    private void setAuthenticationInSecurityContext(UsernamePasswordAuthenticationToken authenticated) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);

        SecurityContextHolder.setContext(context);
    }

    private Optional<String> getJwtToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(this::isBearerAuthHeader)
                .map(this::extractBearerToken);
    }

    private boolean isBearerAuthHeader(String headerValue) {
        return headerValue.startsWith(BEARER_PREFIX);
    }

    private String extractBearerToken(String headerValue) {
        return headerValue.substring(BEARER_PREFIX.length());
    }

}
