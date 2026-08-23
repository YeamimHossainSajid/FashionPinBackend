package com.fashionpin.authservice.service;

import com.fashionpin.authservice.entity.RefreshToken;
import com.fashionpin.authservice.repository.RefreshTokenRepository;
import com.fashionpin.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            @Value("${jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisTemplateProvider = redisTemplateProvider;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public String createRefreshToken(String userAccountId) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        Instant expiryDate = Instant.now().plusMillis(refreshTokenExpirationMs);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .tokenHash(tokenHash)
                .userAccountId(userAccountId)
                .expiryDate(expiryDate)
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        // Store session in Redis for fast validation / rate limiting foundation
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("refresh_token:" + tokenHash, userAccountId, java.time.Duration.ofMillis(refreshTokenExpirationMs));
            } catch (Exception e) {
                // Redis error gracefully handled, DB fallback remains active
            }
        }

        return rawToken;
    }

    @Transactional
    public RefreshToken verifyAndRotate(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Invalid refresh token"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token is expired or revoked");
        }

        // Revoke current token as part of rotation
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                redisTemplate.delete("refresh_token:" + tokenHash);
            } catch (Exception ignored) {
            }
        }

        return token;
    }

    @Transactional
    public void revokeToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });

        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                redisTemplate.delete("refresh_token:" + tokenHash);
            } catch (Exception ignored) {
            }
        }
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
