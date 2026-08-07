package com.fashionpin.common.security;

public final class SecurityConstants {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String ROLES_CLAIM = "roles";
    public static final String USER_ID_CLAIM = "userId";

    private SecurityConstants() {
    }
}

