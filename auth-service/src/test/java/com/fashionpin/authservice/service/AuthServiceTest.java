package com.fashionpin.authservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.authservice.dto.AuthResponse;
import com.fashionpin.authservice.dto.LoginRequest;
import com.fashionpin.authservice.dto.RefreshTokenRequest;
import com.fashionpin.authservice.dto.RegisterRequest;
import com.fashionpin.authservice.entity.RefreshToken;
import com.fashionpin.authservice.entity.UserAccount;
import com.fashionpin.authservice.repository.UserAccountRepository;
import com.fashionpin.authservice.security.JwtService;
import com.fashionpin.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthService authService;

    private UserAccount sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = UserAccount.builder()
                .id(UUID.randomUUID().toString())
                .email("test@fashionpin.com")
                .passwordHash("hashed-pass")
                .status("ACTIVE")
                .roles("ROLE_USER")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@fashionpin.com")
                .password("password123")
                .build();

        when(userAccountRepository.existsByEmail("test@fashionpin.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pass");
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(sampleAccount);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token-123");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(86400000L);
        when(refreshTokenService.createRefreshToken(any())).thenReturn("refresh-token-123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token-123", response.getAccessToken());
        assertEquals("refresh-token-123", response.getRefreshToken());
        verify(outboxService, times(1)).saveEvent(eq("UserAccount"), any(), eq("UserRegistered"), any());
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@fashionpin.com")
                .password("password123")
                .build();

        when(userAccountRepository.existsByEmail("test@fashionpin.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getCode());
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("test@fashionpin.com")
                .password("password123")
                .build();

        when(userAccountRepository.findByEmail("test@fashionpin.com")).thenReturn(Optional.of(sampleAccount));
        when(passwordEncoder.matches("password123", "hashed-pass")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token-123");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(86400000L);
        when(refreshTokenService.createRefreshToken(any())).thenReturn("refresh-token-123");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token-123", response.getAccessToken());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .email("test@fashionpin.com")
                .password("wrong-pass")
                .build();

        when(userAccountRepository.findByEmail("test@fashionpin.com")).thenReturn(Optional.of(sampleAccount));
        when(passwordEncoder.matches("wrong-pass", "hashed-pass")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals("INVALID_CREDENTIALS", ex.getCode());
    }

    @Test
    void refresh_Success() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        RefreshToken rotatedToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userAccountId(sampleAccount.getId())
                .tokenHash("hash")
                .build();

        when(refreshTokenService.verifyAndRotate("valid-refresh-token")).thenReturn(rotatedToken);
        when(userAccountRepository.findById(sampleAccount.getId())).thenReturn(Optional.of(sampleAccount));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(86400000L);
        when(refreshTokenService.createRefreshToken(any())).thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void logout_Success() {
        authService.logout("raw-token");
        verify(refreshTokenService, times(1)).revokeToken("raw-token");
    }
}
