package com.fashionpin.authservice.controller;

import com.fashionpin.authservice.dto.AuthResponse;
import com.fashionpin.authservice.dto.LoginRequest;
import com.fashionpin.authservice.dto.RefreshTokenRequest;
import com.fashionpin.authservice.dto.RegisterRequest;
import com.fashionpin.authservice.service.AuthService;
import com.fashionpin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/api/auth/register", "/api/v1/auth/register"})
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User registered successfully", response));
    }

    @PostMapping({"/api/auth/login", "/api/v1/auth/login"})
    @Operation(summary = "Authenticate user and generate access/refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping({"/api/auth/refresh", "/api/v1/auth/refresh"})
    @Operation(summary = "Refresh access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @PostMapping({"/api/auth/logout", "/api/v1/auth/logout"})
    @Operation(summary = "Revoke refresh token and logout user")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", null));
    }
}
