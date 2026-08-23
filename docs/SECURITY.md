# Security Model (Phase 1 Implementation)

This document describes the security architecture implemented in Phase 1 for **Fashion Pin**.

## Implementation Status

| Area | Status |
|------|--------|
| User Registration & Hashing | **Implemented** (`BCryptPasswordEncoder` in `auth-service`) |
| Token Issuance & Refresh | **Implemented** (JWT Access Tokens & SHA-256 Hashed Refresh Tokens in DB/Redis) |
| Token Revocation & Rotation | **Implemented** (`POST /api/auth/refresh`, `POST /api/auth/logout`) |
| Gateway JWT Authentication | **Implemented** (`JwtAuthenticationFilter` in `api-gateway`) |
| Service JWT Validation | **Implemented** (`JwtAuthenticationFilter` in `auth-service`, `user-service`, `profile-service`) |
| Role-based Access Control | **Implemented** (`ROLE_USER`, `ROLE_ADMIN` foundations) |
| Actuator / Health / Swagger | `permitAll` for health check probes and OpenAPI docs |

## Gateway Security

- `api-gateway` executes `JwtAuthenticationFilter` (order `-100`).
- Validates JWT signature using HMAC SHA-256 secret.
- Extracts `userId` and `roles` claims.
- Enriches downstream request headers with `X-User-Id` and `X-User-Roles` while preserving original Bearer authorization headers.

## Downstream Microservices Security

- `SecurityConfig` in `auth-service`, `user-service`, `profile-service`:
  - CSRF disabled
  - Stateless session management (`SessionCreationPolicy.STATELESS`)
  - Public permits:
    - `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`
    - `/actuator/**`, `/v3/api-docs/**`, `/swagger-ui/**`
  - Authenticated:
    - `/api/auth/logout`
    - `/api/users/me`, `/api/users/{id}`, `PATCH /api/users/me`, `DELETE /api/users/me`
    - `/api/profiles/me`, `/api/profiles/{userId}`, `PATCH /api/profiles/me`

## Shared Security Constants

`common-lib` → `SecurityConstants`:
- `Authorization` / `Bearer ` prefix
- `X-Correlation-Id`
- Claim names: `roles`, `userId`

## Next Implementation Phases (Remaining TODOs)

1. **Phase 2 (Fashion & Product Domain)**:
   - Product catalog APIs & category taxonomy in `product-service`.
   - Fashion discovery & trending feeds in `fashion-discovery-service`.
2. **Phase 3 (Social & Style Domain)**:
   - User social graph (followers/following) in `profile-service`.
   - Moodboards & curation pins in `moodboard-service`.
3. **Phase 4 (AI & Computer Vision Domain)**:
   - AI stylist recommendation ports in `ai-stylist-service`.
   - Visual search & image processing pipeline in `visual-search-service` and `image-processing-service`.
   - Virtual try-on stubs in `virtual-tryon-service`.
4. **Phase 5 (Commerce & Shopping Domain)**:
   - Shopping cart & bag in `shopping-service`.
   - Order management in `order-service`.
   - Payment gateway integration in `payment-service`.
