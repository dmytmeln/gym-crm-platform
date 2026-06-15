package com.gym.crm.core.aspect;

import com.gym.crm.core.actuator.metrics.LoginCounter;
import com.gym.crm.core.actuator.metrics.TrainingCreatedCounter;
import com.gym.crm.core.actuator.metrics.UserRegistrationCounter;
import com.gym.crm.core.dto.LoginRequestDto;
import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.exception.IpBlockedException;
import com.gym.crm.core.exception.UserDeactivatedException;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.security.JwtService;
import com.gym.crm.core.security.LoginAttemptService;
import com.gym.crm.core.security.TokenBlacklistService;
import com.gym.crm.core.service.AuthenticationService;
import com.gym.crm.core.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.context.request.RequestAttributes.REFERENCE_REQUEST;

@SpringJUnitConfig(classes = {
        AuthenticationServiceImpl.class,
        BruteForceProtectionAspect.class,
        SecurityExceptionTranslationAspect.class,
        ServiceTelemetryAspect.class
})
@EnableAspectJAutoProxy
class AuthenticationServiceAspectIntegrationTest {

    private static final String TEST_IP_ADDRESS = "192.168.1.100";

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private LoginAttemptService loginAttemptService;

    @MockitoBean
    private UserRegistrationCounter registrationCounter;

    @MockitoBean
    private LoginCounter loginCounter;

    @MockitoBean
    private TrainingCreatedCounter trainingCounter;

    @Autowired
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(TEST_IP_ADDRESS);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldProceedWhenIpNotBlockedAndLoginSucceeds() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("username", "password");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("username", "password");
        String expectedJwtToken = "jwt-token";

        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(false);
        when(authenticationManager.authenticate(authenticationToken)).thenReturn(null);
        when(jwtService.generateAccessToken("username")).thenReturn(expectedJwtToken);

        String result = service.login(loginRequestDto);

        assertEquals(expectedJwtToken, result);
        verify(loginCounter).increment(true);
        verify(loginAttemptService).loginSucceeded(TEST_IP_ADDRESS);
    }

    @Test
    void shouldBlockRequestViaBruteForceAspectBeforeReachingAuthenticationManager() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("username", "password");

        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(true);

        IpBlockedException exception = assertThrows(IpBlockedException.class, () -> service.login(loginRequestDto));

        assertEquals("IP is temporarily blocked due to too many failed login attempts", exception.getMessage());
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void shouldTranslateBadCredentialsExceptionToAuthenticationExceptionAndTrackFailedAttempts() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("username", "password");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("username", "password");

        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(false);
        when(authenticationManager.authenticate(authenticationToken)).thenThrow(new BadCredentialsException("Bad credentials"));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> service.login(loginRequestDto));

        assertEquals("Invalid username or password", exception.getMessage());
        verify(loginCounter).increment(false);
        verify(loginAttemptService).loginFailed(TEST_IP_ADDRESS);
    }

    @Test
    void shouldTranslateDisabledExceptionToUserDeactivatedExceptionAndTrackFailedAttempts() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("username", "password");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("username", "password");

        when(loginAttemptService.isBlocked(TEST_IP_ADDRESS)).thenReturn(false);
        when(authenticationManager.authenticate(authenticationToken)).thenThrow(new DisabledException("Disabled"));

        UserDeactivatedException exception = assertThrows(UserDeactivatedException.class, () -> service.login(loginRequestDto));

        assertEquals("User account is deactivated", exception.getMessage());
        verify(loginCounter).increment(false);
        verify(loginAttemptService).loginFailed(TEST_IP_ADDRESS);
    }

    @Test
    void shouldThrowIllegalStateWhenNoRequestAttributesIsBound() {
        RequestContextHolder.resetRequestAttributes();
        LoginRequestDto loginRequestDto = new LoginRequestDto("username", "password");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.login(loginRequestDto));

        assertEquals("No request attributes bound to the thread", exception.getMessage());
        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(loginAttemptService);
        verifyNoInteractions(registrationCounter);
        verifyNoInteractions(loginCounter);
        verifyNoInteractions(trainingCounter);
    }

    @Test
    void shouldThrowIllegalStateWhenRequestAttributesIsNotServletRequestAttributes() {
        RequestAttributes requestAttributes = mock(RequestAttributes.class);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        LoginRequestDto loginRequestDto = new LoginRequestDto("username", "password");

        when(requestAttributes.resolveReference(REFERENCE_REQUEST)).thenReturn(new Object());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.login(loginRequestDto));

        assertEquals("Request attributes do not contain a ServletRequest", exception.getMessage());
        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(loginAttemptService);
        verifyNoInteractions(registrationCounter);
        verifyNoInteractions(loginCounter);
        verifyNoInteractions(trainingCounter);
    }

}
