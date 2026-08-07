# API Gateway Routes

Gateway module: `api-gateway`  
Listen port: `8080`  
Discovery: Eureka (`lb://<service-name>`)

## Two ways to call a service

### 1. Explicit rewrite routes (preferred public API shape)

Pattern in `api-gateway/src/main/resources/application.yml`:

```yaml
- id: user-service
  uri: lb://user-service
  predicates:
    - Path=/api/v1/user/**
  filters:
    - RewritePath=/api/v1/user(?<segment>/?.*), /api/v1${segment}
```

| Public URL | Downstream |
|------------|------------|
| `GET /api/v1/user/health` | `user-service` → `/api/v1/health` |
| `GET /api/v1/user/anything` | `user-service` → `/api/v1/anything` |

### 2. Discovery locator (ops / debugging)

```yaml
spring.cloud.gateway.discovery.locator.enabled: true
spring.cloud.gateway.discovery.locator.lower-case-service-id: true
```

| Public URL | Downstream |
|------------|------------|
| `GET /user-service/api/v1/health` | `user-service` → `/api/v1/health` |

Use locator for quick checks. Prefer explicit `/api/v1/<prefix>/...` routes for clients.

## Route map (foundation)

Prefix is derived from service name without `-service`.

| Gateway path prefix | Eureka service id | Example health |
|---------------------|-------------------|----------------|
| `/api/v1/auth/**` | auth-service | `/api/v1/auth/health` |
| `/api/v1/user/**` | user-service | `/api/v1/user/health` |
| `/api/v1/profile/**` | profile-service | `/api/v1/profile/health` |
| `/api/v1/fashion-discovery/**` | fashion-discovery-service | `/api/v1/fashion-discovery/health` |
| `/api/v1/product/**` | product-service | `/api/v1/product/health` |
| `/api/v1/search/**` | search-service | `/api/v1/search/health` |
| `/api/v1/recommendation/**` | recommendation-service | `/api/v1/recommendation/health` |
| `/api/v1/outfit-detection/**` | outfit-detection-service | `/api/v1/outfit-detection/health` |
| `/api/v1/image-processing/**` | image-processing-service | `/api/v1/image-processing/health` |
| `/api/v1/visual-search/**` | visual-search-service | `/api/v1/visual-search/health` |
| `/api/v1/ai-stylist/**` | ai-stylist-service | `/api/v1/ai-stylist/health` |
| `/api/v1/virtual-tryon/**` | virtual-tryon-service | `/api/v1/virtual-tryon/health` |
| `/api/v1/moodboard/**` | moodboard-service | `/api/v1/moodboard/health` |
| `/api/v1/shopping/**` | shopping-service | `/api/v1/shopping/health` |
| `/api/v1/order/**` | order-service | `/api/v1/order/health` |
| `/api/v1/payment/**` | payment-service | `/api/v1/payment/health` |
| `/api/v1/inventory/**` | inventory-service | `/api/v1/inventory/health` |
| `/api/v1/brand-integration/**` | brand-integration-service | `/api/v1/brand-integration/health` |
| `/api/v1/notification/**` | notification-service | `/api/v1/notification/health` |
| `/api/v1/analytics/**` | analytics-service | `/api/v1/analytics/health` |
| `/api/v1/media/**` | media-service | `/api/v1/media/health` |

Gateway itself:

- `GET /api/v1/health`
- `GET /actuator/health`
- `GET /actuator/gateway/routes` (when actuator gateway endpoint enabled)

## Filters (current state)

| Filter / component | Status | Notes |
|--------------------|--------|-------|
| `GlobalLoggingFilter` | Active | Adds/propagates `X-Correlation-Id`, request logging |
| `JwtAuthenticationFilter` | Placeholder | Observes Bearer token; does not validate |
| `AuthenticationFilter` | Placeholder | No enforcement yet |
| CORS (`CorsConfig`) | Active | Allowed origin patterns configurable |
| `RequestRateLimiter` | Placeholder only | Config bean `ipKeyResolver` exists; not attached to routes yet (needs Redis) |

## Load balancing dependency

Gateway POM must include `spring-cloud-starter-loadbalancer` (in addition to Eureka client). Without it, `lb://` routes fail with empty server lists.

## Common gateway failures

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `503` + `No servers available for service: user-service` | Eureka fetch lag or service not registered | Wait ≥ fetch interval; check Eureka UI |
| `404` on `/user-service/...` | Locator disabled or registry empty at first fetch | Enable locator; wait for registry refresh |
| `404` on `/api/v1/user/health` | Missing rewrite route or wrong prefix | Check `application.yml` route id/path |
| Redis connection errors on startup | Rate limiter / redis autoconfig in non-dev | Use `dev` exclusions or start Redis |

## Changing routes safely

1. Edit `api-gateway/.../application.yml`
2. Update this file
3. Restart gateway (route changes are not hot-reloaded unless you add that later)
4. Smoke: direct service health, then gateway rewrite path, then locator path
