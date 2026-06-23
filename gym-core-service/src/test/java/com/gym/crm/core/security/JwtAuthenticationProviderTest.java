package com.gym.crm.core.security;

import com.gym.crm.security.JwtPayload;
import com.gym.crm.security.JwtService;
import com.gym.crm.core.security.UserDetailsServiceImpl.SimpleUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {

    private static final String USERNAME = "liam.miller";
    private static final String TOKEN = "validToken";
    private static final String JTI = "test-jti";
    private static final JwtPayload PAYLOAD = new JwtPayload(USERNAME, JTI,
            Date.from(LocalDate.of(2024, JANUARY, 1).atStartOfDay(UTC).toInstant()));

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtAuthenticationProvider tokenProvider;

    @Test
    void shouldAuthenticateSuccessfullyWhenTokenIsValid() {
        JwtTokenAuthentication authentication = JwtTokenAuthentication.unauthenticated(TOKEN);
        SimpleUserDetails userDetails = new SimpleUserDetails(USERNAME, true, List.of());

        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(jwtService.getPayload(TOKEN)).thenReturn(PAYLOAD);
        when(tokenBlacklistService.isBlacklisted(JTI)).thenReturn(false);
        when(userDetailsService.loadSimpleUserByUsername(USERNAME)).thenReturn(userDetails);

        Authentication result = tokenProvider.authenticate(authentication);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        UserDetails principal = (UserDetails) result.getPrincipal();
        assertEquals(USERNAME, principal.getUsername());
        assertTrue(principal.isEnabled());
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenTokenIsInvalid() {
        JwtTokenAuthentication authentication = JwtTokenAuthentication.unauthenticated(TOKEN);

        when(jwtService.isTokenValid(TOKEN)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> tokenProvider.authenticate(authentication));
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenTokenIsBlacklisted() {
        JwtTokenAuthentication authentication = JwtTokenAuthentication.unauthenticated(TOKEN);

        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(jwtService.getPayload(TOKEN)).thenReturn(PAYLOAD);
        when(tokenBlacklistService.isBlacklisted(JTI)).thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> tokenProvider.authenticate(authentication));
    }

    @Test
    void shouldThrowDisabledExceptionWhenUserIsDeactivated() {
        JwtTokenAuthentication authentication = JwtTokenAuthentication.unauthenticated(TOKEN);
        SimpleUserDetails userDetails = new SimpleUserDetails(USERNAME, false, List.of());

        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(jwtService.getPayload(TOKEN)).thenReturn(PAYLOAD);
        when(tokenBlacklistService.isBlacklisted(JTI)).thenReturn(false);
        when(userDetailsService.loadSimpleUserByUsername(USERNAME)).thenReturn(userDetails);

        assertThrows(DisabledException.class, () -> tokenProvider.authenticate(authentication));
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
        JwtTokenAuthentication authentication = JwtTokenAuthentication.unauthenticated(TOKEN);

        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(jwtService.getPayload(TOKEN)).thenReturn(PAYLOAD);
        when(tokenBlacklistService.isBlacklisted(JTI)).thenReturn(false);
        when(userDetailsService.loadSimpleUserByUsername(USERNAME)).thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () -> tokenProvider.authenticate(authentication));
    }

    @Test
    void shouldSupportJwtTokenAuthentication() {
        boolean result = tokenProvider.supports(JwtTokenAuthentication.class);

        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(classes = {Authentication.class, UsernamePasswordAuthenticationToken.class, AnonymousAuthenticationToken.class})
    void shouldNotSupportOtherAuthenticationTypes(Class<? extends Authentication> authentication) {
        boolean result = tokenProvider.supports(authentication);

        assertFalse(result);
    }

}
