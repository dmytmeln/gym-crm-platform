package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.LoginChangeDto;
import com.gym.crm.core.dto.LoginRequestDto;
import com.gym.crm.core.entity.User;
import com.gym.crm.core.exception.AuthenticationException;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.security.TokenBlacklistService;
import com.gym.crm.core.service.AuthenticationService;
import com.gym.crm.security.JwtPayload;
import com.gym.crm.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public String login(LoginRequestDto loginRequestDto) {
        Objects.requireNonNull(loginRequestDto, "AuthenticateRequestDto cannot be null");
        log.info("Login attempt for username: {}", loginRequestDto.username());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.username(), loginRequestDto.password());

        authenticationManager.authenticate(authenticationToken);
        log.info("Login successful for username: {}", loginRequestDto.username());

        return jwtService.generateAccessToken(loginRequestDto.username());
    }

    @Override
    @Transactional
    @PreAuthorize("#loginChangeDto.username == authentication.name")
    public void changePassword(LoginChangeDto loginChangeDto) {
        Objects.requireNonNull(loginChangeDto, "LoginChangeDto cannot be null");
        log.info("Change password attempt for username: {}", loginChangeDto.username());

        User user = userRepository.findByUsername(loginChangeDto.username())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(loginChangeDto.oldPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        User updatedUser = user.toBuilder()
                .password(passwordEncoder.encode(loginChangeDto.newPassword()))
                .build();

        userRepository.save(updatedUser);
        log.info("Password changed successfully for username: {}", loginChangeDto.username());
    }

    @Override
    public void logout(String token) {
        Objects.requireNonNull(token, "Token cannot be null");

        JwtPayload payload = jwtService.getPayload(token);
        tokenBlacklistService.blacklistToken(payload.jti(), payload.expiration().toInstant());
    }

}
