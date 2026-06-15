package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.LoginChangeDto;
import com.gym.crm.core.dto.LoginRequestDto;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.security.JwtPayload;
import com.gym.crm.core.security.JwtService;
import com.gym.crm.core.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final String USERNAME = "liam.miller";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPassword123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void shouldLoginSuccessfullyWhenCredentialsMatch() {
        LoginRequestDto loginRequestDto = buildLoginRequestDto();
        Authentication authentication = mock(Authentication.class);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
        String expectedToken = "mocked-jwt-token";

        when(authenticationManager.authenticate(token)).thenReturn(authentication);
        when(jwtService.generateAccessToken(USERNAME)).thenReturn(expectedToken);

        String actual = service.login(loginRequestDto);

        assertThat(actual).isEqualTo(expectedToken);
        verify(authenticationManager).authenticate(token);
        verify(jwtService).generateAccessToken(USERNAME);
    }

    @Test
    void shouldThrowExceptionWhenLoginCredentialsAreInvalid() {
        LoginRequestDto loginRequestDto = buildLoginRequestDto();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);

        when(authenticationManager.authenticate(token)).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> service.login(loginRequestDto));
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        LoginChangeDto loginChangeDto = buildLoginChangeDto();
        String oldPassword = "hashedPassword";
        User user = User.builder()
                .username(USERNAME)
                .password(oldPassword)
                .build();
        String newPassword = "newHashedPassword";
        User expectedUpdatedUser = user.toBuilder()
                .password(newPassword)
                .build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, oldPassword)).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(newPassword);

        service.changePassword(loginChangeDto);

        verify(userRepository).save(expectedUpdatedUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundOnChangePassword() {
        LoginChangeDto loginChangeDto = buildLoginChangeDto();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> service.changePassword(loginChangeDto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenOldPasswordMismatchOnChangePassword() {
        LoginChangeDto loginChangeDto = buildLoginChangeDto();
        User user = User.builder()
                .username(USERNAME)
                .password("hashedPassword")
                .build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "hashedPassword")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> service.changePassword(loginChangeDto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLogoutSuccessfully() {
        String token = "valid-token";
        String jti = "test-jti";
        Date expiration = Date.from(LocalDate.of(2024, 1, 1).atStartOfDay(UTC).toInstant());
        JwtPayload payload = new JwtPayload(USERNAME, jti, expiration);

        when(jwtService.getPayload(token)).thenReturn(payload);

        service.logout(token);

        verify(tokenBlacklistService).blacklistToken(jti, expiration.toInstant());
    }

    private LoginRequestDto buildLoginRequestDto() {
        return new LoginRequestDto(USERNAME, PASSWORD);
    }

    private LoginChangeDto buildLoginChangeDto() {
        return new LoginChangeDto(USERNAME, PASSWORD, NEW_PASSWORD);
    }

}
