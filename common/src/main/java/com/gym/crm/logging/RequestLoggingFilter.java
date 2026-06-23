package com.gym.crm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_CACHE_SIZE = 10_000;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_CACHE_SIZE);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        log.info("Request started: method={}, uri={}, query={}",
                wrappedRequest.getMethod(), wrappedRequest.getRequestURI(), wrappedRequest.getQueryString());

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;

            log.info("Request completed: method={}, uri={}, status={}, durationMs={}",
                    wrappedRequest.getMethod(), wrappedRequest.getRequestURI(), wrappedResponse.getStatus(), duration);

            logBody("Request body", wrappedRequest.getContentType(), wrappedRequest.getContentAsByteArray(), getCharset(wrappedRequest));
            logBody("Response body", wrappedResponse.getContentType(), wrappedResponse.getContentAsByteArray(), getCharset(wrappedResponse));

            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logBody(String label, String contentType, byte[] body, Charset charset) {
        if (!JsonBodySanitizer.supports(contentType) || body.length == 0) {
            return;
        }

        String sanitizedBody = JsonBodySanitizer.sanitize(new String(body, charset));
        log.debug("{}: {}", label, sanitizedBody);
    }

    private Charset getCharset(HttpServletRequest req) {
        String encoding = req.getCharacterEncoding();
        return getCharset(encoding);
    }

    private Charset getCharset(HttpServletResponse res) {
        String encoding = res.getCharacterEncoding();
        return getCharset(encoding);
    }

    private Charset getCharset(String encoding) {
        if (encoding == null) {
            return UTF_8;
        }

        try {
            return Charset.forName(encoding);
        } catch (IllegalArgumentException e) {
            return UTF_8;
        }
    }

}
