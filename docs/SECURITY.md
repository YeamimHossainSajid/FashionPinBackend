# Security Model (Current Foundation)

This document describes **what is actually enforced today** vs **placeholders**. Do not assume production auth exists.

## Current reality

| Area | Status |
|------|--------|
| JWT signature validation | **Not implemented** (placeholder only) |
| Token issuance / login APIs | **Not implemented** |
| Gateway auth blocking | **Not enforced** |
| Service method security | Annotation support enabled; almost nothing protected beyond path rules |
| Config Server | HTTP Basic (`config` / `config` by default) |
| Actuator / health / swagger | Generally `permitAll` on services |

## Gateway

Classes:

- `JwtAuthenticationFilter` — if `Authorization: Bearer ...` present, logs length; does not validate
- `AuthenticationFilter` — no-op placeholder for protected-route enforcement
- CORS enabled via `CorsConfig`

Treat the gateway as an open edge for foundation health checks.

## Domain services

`SecurityConfig` pattern:

- CSRF disabled
- Stateless sessions
- Permit:
  - `/actuator/**`
  - `/api/v1/health`
  - `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
- `/api/v1/admin/**` → `hasRole("ADMIN")` (placeholder path)
- Everything else → `authenticated()`

`JwtAuthenticationFilter` (servlet):

- If Bearer header exists, installs a fake `ROLE_USER` authentication named `placeholder-user`
- Does **not** check signature, expiry, issuer, or roles from token

Implication: unauthenticated calls to non-public endpoints get `401`. Sending any non-empty Bearer token currently becomes an authenticated `ROLE_USER`.

## Shared constants

`common-lib` → `SecurityConstants`:

- `Authorization` / `Bearer ` prefix
- `X-Correlation-Id`
- claim name placeholders: `roles`, `userId`

## What to implement next (recommended order)

1. Real JWT issue/validate in `auth-service`
2. Shared JWT parser utility in `common-lib` or a dedicated security starter
3. Gateway validates JWT and forwards `X-User-Id` / roles headers
4. Services trust gateway headers **only** on private networks, or re-validate JWT
5. Replace placeholder filter behavior; remove “any Bearer = ROLE_USER”
6. Lock down actuator in prod (or put behind admin auth / network policy)
7. Rotate Config Server credentials; do not ship defaults

## Secrets hygiene

- Never commit real secrets; use `.env` (gitignored) from `.env.example`
- Change `CONFIG_SERVER_PASSWORD`, DB passwords, JWT secrets before any shared environment
- `config-repo/application.yml` contains a placeholder JWT secret (`change-me-in-production`)
