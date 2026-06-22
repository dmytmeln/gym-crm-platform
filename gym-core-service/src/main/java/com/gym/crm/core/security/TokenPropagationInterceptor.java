package com.gym.crm.core.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class TokenPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request,
                                        byte @NonNull [] body,
                                        @NonNull ClientHttpRequestExecution execution) throws IOException {
        getAuthHeaderValue().ifPresent(authHeaderValue -> request.getHeaders().add(AUTHORIZATION, authHeaderValue));

        return execution.execute(request, body);
    }

    private Optional<String> getAuthHeaderValue() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            log.debug("No ServletRequestAttributes bound to the current thread");
            return Optional.empty();
        }

        HttpServletRequest httpRequest = servletAttributes.getRequest();
        String authHeader = httpRequest.getHeader(AUTHORIZATION);

        if (authHeader == null) {
            log.debug("No Authorization header found in current request context");
            return Optional.empty();
        }

        return Optional.of(authHeader);
    }

}
