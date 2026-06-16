package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.LoginChangeDto;
import com.gym.crm.core.dto.LoginRequestDto;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.security.JwtService;
import com.gym.crm.core.security.TokenBlacklistService;
import com.gym.crm.core.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(AuthenticationServiceImpl.class)
@EnableMethodSecurity
class AuthenticationServiceSecurityTest {

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

    @Autowired
    private AuthenticationService service;

    @Test
    void shouldAllowLoginWhenUnauthenticated() {
        String username = "liam.miller";
        String password = "password123";
        LoginRequestDto loginRequestDto = new LoginRequestDto(username, password);
        Authentication authentication = mock(Authentication.class);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        String expectedJwtToken = "mocked-jwt-token";

        when(authenticationManager.authenticate(token)).thenReturn(authentication);
        when(jwtService.generateAccessToken(username)).thenReturn(expectedJwtToken);

        String result = service.login(loginRequestDto);

        assertThat(result).isEqualTo(expectedJwtToken);
    }

    @Test
    @WithMockUser(username = "liam.miller")
    void shouldAllowChangePasswordWhenIsSelf() {
        String username = "liam.miller";
        String oldPassword = "password123";
        String newPassword = "newPassword123";
        LoginChangeDto loginChangeDto = new LoginChangeDto(username, oldPassword, newPassword);
        String hashedPassword = "hashedPassword";
        User user = User.builder()
                .username(username)
                .password(hashedPassword)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, hashedPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashedPassword");

        assertThatNoException().isThrownBy(() -> service.changePassword(loginChangeDto));
    }

    @Test
    @WithMockUser(username = "other.user")
    void shouldDenyChangePasswordWhenIsOtherUser() {
        LoginChangeDto loginChangeDto = new LoginChangeDto("liam.miller", "password123", "newPassword123");

        assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> service.changePassword(loginChangeDto));
    }

}
