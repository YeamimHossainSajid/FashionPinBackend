package com.fashionpin.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;

    public JwtService(
            @Value("${jwt.secret:fashionpin_super_secret_jwt_key_that_is_at_least_32_bytes_long_for_security!}") String secret,
            @Value("${jwt.access-token-expiration-ms:86400000}") long accessTokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateAccessToken(String userId, String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("roles", roles);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuer("fashionpin-auth")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        return claims.get("roles", List.class);
    }

    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims.get("email", String.class);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }
}
