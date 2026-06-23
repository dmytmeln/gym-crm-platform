package com.gym.crm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_CACHE_SIZE = 10_000;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_CACHE_SIZE);

        long start = System.currentTimeMillis();
        log.info("Request started: method={}, uri={}, query={}",
                wrappedRequest.getMethod(), wrappedRequest.getRequestURI(), wrappedRequest.getQueryString());

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            log.info("Request completed: method={}, uri={}, status={}, durationMs={}",
                    wrappedRequest.getMethod(), wrappedRequest.getRequestURI(), response.getStatus(), duration);

            String contentType = wrappedRequest.getContentType();
            if (isJson(contentType)) {
                String requestBody = new String(wrappedRequest.getContentAsByteArray(), getCharset(wrappedRequest));
                String sanitizedBody = JsonBodySanitizer.sanitize(requestBody);
                log.debug("Request body: {}", sanitizedBody);
            }
        }
    }

    private boolean isJson(String contentType) {
        return JsonBodySanitizer.supports(contentType);
    }

    private Charset getCharset(HttpServletRequest req) {
        String encoding = req.getCharacterEncoding();
        if (encoding == null) {
            return StandardCharsets.UTF_8;
        }

        try {
            return Charset.forName(encoding);
        } catch (IllegalArgumentException e) {
            return StandardCharsets.UTF_8;
        }
    }

}
