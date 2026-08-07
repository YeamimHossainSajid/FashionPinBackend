package com.fashionpin.aistylistservice.security;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public boolean validateToken(String token) {
        // Placeholder for JWT signature and expiry validation.
        return token != null && !token.isBlank();
    }

    public String extractUserId(String token) {
        // Placeholder for claim extraction.
        return "placeholder-user";
    }
}

