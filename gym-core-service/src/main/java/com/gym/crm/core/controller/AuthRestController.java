package com.gym.crm.core.controller;

import com.gia.openapi.model.ErrorResponse;
import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gym.crm.core.exception.ValidationException;
import com.gym.crm.core.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("${app.api.base-path}/auth")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Login and account management")
public class AuthRestController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final GymFacade facade;

    @PutMapping("/password")
    @Operation(summary = "Change login password", description = "Changes the password for a given user", responses = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden access",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invalid user credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> changePassword(@Valid @RequestBody LoginChangeRequest loginChangeRequest) {
        facade.changePassword(loginChangeRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password", description = "Authenticates user by username and password", responses = {
            @ApiResponse(responseCode = "200", description = "Successful login",
                    headers = @Header(name = AUTHORIZATION, description = "JWT Token", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invalid user credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Too many failed login attempts",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = facade.login(loginRequest);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, BEARER_PREFIX + token)
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out user", description = "Invalidates the active JWT by blacklisting it", responses = {
            @ApiResponse(responseCode = "200", description = "Successful logout"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden access",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> logout(@RequestHeader(AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new ValidationException("Invalid authorization header format");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        facade.logout(token);

        return ResponseEntity.ok().build();
    }

}

