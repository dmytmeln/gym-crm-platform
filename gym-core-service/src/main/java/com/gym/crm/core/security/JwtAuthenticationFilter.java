package com.gym.crm.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        getJwtAuth(request)
                .map(authenticationManager::authenticate)
                .ifPresent(this::setAuthenticationInSecurityContext);

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationInSecurityContext(Authentication authenticated) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);

        SecurityContextHolder.setContext(context);
    }

    private Optional<Authentication> getJwtAuth(HttpServletRequest request) {
        return getJwtToken(request).map(JwtTokenAuthentication::unauthenticated);
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