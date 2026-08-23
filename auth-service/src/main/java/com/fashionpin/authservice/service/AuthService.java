package com.fashionpin.authservice.service;

import com.fashionpin.authservice.dto.AuthResponse;
import com.fashionpin.authservice.dto.LoginRequest;
import com.fashionpin.authservice.dto.RefreshTokenRequest;
import com.fashionpin.authservice.dto.RegisterRequest;
import com.fashionpin.authservice.entity.RefreshToken;
import com.fashionpin.authservice.entity.UserAccount;
import com.fashionpin.authservice.repository.UserAccountRepository;
import com.fashionpin.authservice.security.JwtService;
import com.fashionpin.common.event.UserRegisteredEvent;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.util.CorrelationIdConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final OutboxService outboxService;
    private final HttpServletRequest httpRequest;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            OutboxService outboxService,
            HttpServletRequest httpRequest) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.outboxService = outboxService;
        this.httpRequest = httpRequest;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userAccountRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        UserAccount account = UserAccount.create(email, passwordHash);
        userAccountRepository.save(account);

        String correlationId = httpRequest.getHeader(CorrelationIdConstants.CORRELATION_ID_HEADER);
        UserRegisteredEvent event = UserRegisteredEvent.create(account.getId(), account.getEmail(), correlationId);
        outboxService.saveEvent("UserAccount", account.getId(), "UserRegistered", event);

        return buildAuthResponse(account);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        return buildAuthResponse(account);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken rotatedToken = refreshTokenService.verifyAndRotate(request.getRefreshToken());
        UserAccount account = userAccountRepository.findById(rotatedToken.getUserAccountId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User account not found"));

        return buildAuthResponse(account);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    private AuthResponse buildAuthResponse(UserAccount account) {
        List<String> roles = Arrays.asList(account.getRoles().split(","));
        String accessToken = jwtService.generateAccessToken(account.getId(), account.getEmail(), roles);
        String refreshToken = refreshTokenService.createRefreshToken(account.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .userId(account.getId())
                .email(account.getEmail())
                .build();
    }
}
