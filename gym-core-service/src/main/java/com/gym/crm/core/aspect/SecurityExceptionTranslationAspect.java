package com.gym.crm.core.aspect;

import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.exception.UserDeactivatedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityExceptionTranslationAspect {

    @Pointcut("execution(* com.gym.crm.core.service.AuthenticationService.login(..))")
    private void loginMethod() {
    }

    @Around("loginMethod()")
    public Object translateLoginExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DisabledException e) {
            throw new UserDeactivatedException("User account is deactivated", e);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException("Invalid username or password", e);
        }
    }

}
