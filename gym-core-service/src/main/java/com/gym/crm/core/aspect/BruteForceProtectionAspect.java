package com.gym.crm.core.aspect;

import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.exception.IpBlockedException;
import com.gym.crm.core.security.LoginAttemptService;
import jakarta.servlet.ServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.springframework.web.context.request.RequestAttributes.REFERENCE_REQUEST;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class BruteForceProtectionAspect {

    private final LoginAttemptService loginAttemptService;

    @Pointcut("execution(* com.gym.crm.core.service.AuthenticationService.login(..))")
    private void authenticationLogin() {
    }

    @Around("authenticationLogin()")
    public Object protectAgainstBruteForce(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = getClientIp();

        if (loginAttemptService.isBlocked(ip)) {
            throw new IpBlockedException("IP is temporarily blocked due to too many failed login attempts");
        }

        try {
            Object result = joinPoint.proceed();
            loginAttemptService.loginSucceeded(ip);

            return result;
        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(ip);
            throw e;
        }
    }

    private String getClientIp() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("No request attributes bound to the thread");
        }

        if (!(requestAttributes.resolveReference(REFERENCE_REQUEST) instanceof ServletRequest request)) {
            throw new IllegalStateException("Request attributes do not contain a ServletRequest");
        }

        return request.getRemoteAddr();
    }

}
