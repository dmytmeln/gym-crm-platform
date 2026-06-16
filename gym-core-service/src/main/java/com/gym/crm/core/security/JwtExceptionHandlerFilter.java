package com.gym.crm.core.security;

import com.gym.crm.core.exception.InvalidTokenException;
import com.gym.crm.core.exception.UserDeactivatedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (DisabledException ex) {
            handlerExceptionResolver.resolveException(request, response, null, new UserDeactivatedException("User account is deactivated", ex));
        } catch (AuthenticationException ex) {
            handlerExceptionResolver.resolveException(request, response, null, new InvalidTokenException("Invalid token", ex));
        }
    }

}
