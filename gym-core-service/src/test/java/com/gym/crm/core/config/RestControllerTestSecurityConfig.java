package com.gym.crm.core.config;

import com.gym.crm.core.security.JwtAccessDeniedHandler;
import com.gym.crm.core.security.JwtAuthenticationEntryPoint;
import com.gym.crm.core.security.JwtAuthenticationFilter;
import com.gym.crm.core.security.JwtExceptionHandlerFilter;
import com.gym.crm.core.security.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;

import java.io.IOException;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class, JwtExceptionHandlerFilter.class})
public class RestControllerTestSecurityConfig {

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new FakeJwtAuthenticationFilter();
    }

    private static class FakeJwtAuthenticationFilter extends JwtAuthenticationFilter {

        public FakeJwtAuthenticationFilter() {
            super(mock(AuthenticationManager.class));
        }

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) throws ServletException, IOException {
            filterChain.doFilter(request, response);
        }
    }

}
