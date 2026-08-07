# Environment Variables & Profiles

## Profiles

Set with `SPRING_PROFILES_ACTIVE` (default `dev` for apps).

### `dev`

Purpose: boot a service with almost no infrastructure.

- Datasource: H2 in-memory (`MODE=PostgreSQL`)
- JPA: `ddl-auto=update`
- Kafka `@Configuration` classes for producers/topics are `@Profile("!dev")` — absent
- Redis autoconfiguration excluded (gateway + services)
- Zipkin tracing disabled on gateway in `dev`
- Config client: `CONFIG_ENABLED` defaults to `false`

Use this for foundation compile/run and health checks.

### `test`

Similar to `dev` (H2, Kafka/Redis autoconfig excluded). Used by `@ActiveProfiles("test")` smoke tests.

### `docker`

Purpose: Compose / container runtime.

- Postgres via `DB_URL`
- Kafka + Redis expected to be available
- JPA `ddl-auto=update` (foundation convenience; tighten for real prod migrations later)
- Config client often enabled in Compose (`CONFIG_ENABLED=true`)

### `prod`

- Postgres
- JPA `ddl-auto=validate` (expects migrations / schema ownership outside Hibernate create)
- Kafka listeners auto-startup enabled

## Environment variables

Copy `.env.example` as a reference. Compose sets many of these per service.

| Variable | Default / example | Used by | Notes |
|----------|-------------------|---------|-------|
| `SPRING_PROFILES_ACTIVE` | `dev` | all | `dev` / `test` / `docker` / `prod` |
| `EUREKA_SERVER_URL` | `http://localhost:8761/eureka/` | clients + gateway | Must end with `/eureka/` |
| `CONFIG_SERVER_URL` | `http://localhost:8888` | bootstrap.yml | |
| `CONFIG_SERVER_USERNAME` | `config` | config client + server | HTTP basic |
| `CONFIG_SERVER_PASSWORD` | `config` | config client + server | **change in real deploys** |
| `CONFIG_ENABLED` | `false` | services | Set `true` only when Config Server is up |
| `DB_URL` | `jdbc:postgresql://localhost:5432/<service_db>` | domain services | Ignored in `dev` H2 profile block |
| `DB_USERNAME` | `fashionpin` | domain services | |
| `DB_PASSWORD` | `fashionpin` | domain services | |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | gateway, services | Required when enabling rate limiter |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | domain services | |
| `ZIPKIN_URL` | `http://localhost:9411/api/v2/spans` | tracing | Noisy if Zipkin down and tracing enabled |
| `GRPC_PORT` | HTTP port + 1000 | domain services | |
| `CONFIG_GIT_URI` | empty | config-server | Optional git backend; native is default |

## Database ownership

One Postgres database per domain service. Names:

| Service | Database |
|---------|----------|
| auth-service | auth_db |
| user-service | user_db |
| profile-service | profile_db |
| fashion-discovery-service | fashion_discovery_db |
| product-service | product_db |
| search-service | search_db |
| recommendation-service | recommendation_db |
| outfit-detection-service | outfit_detection_db |
| image-processing-service | image_processing_db |
| visual-search-service | visual_search_db |
| ai-stylist-service | ai_stylist_db |
| virtual-tryon-service | virtual_tryon_db |
| moodboard-service | moodboard_db |
| shopping-service | shopping_db |
| order-service | order_db |
| payment-service | payment_db |
| inventory-service | inventory_db |
| brand-integration-service | brand_integration_db |
| notification-service | notification_db |
| analytics-service | analytics_db |
| media-service | media_db |

Created by `docker/postgres/init-databases.sh` on first Postgres volume init.

**Rule:** never query another service’s DB. Use Feign/gRPC/Kafka.

## Config Server

- Default profile on server: `native`
- Search locations: `file:./config-repo` and `classpath:/config-repo`
- Source of truth for checked-in defaults: repo-root `config-repo/`
- Classpath mirror: `config-server/src/main/resources/config-repo/` — keep in sync when editing
- Secured with HTTP basic (`config`/`config` by default)
- Health endpoints are permit-all

Enable client usage only when server is reachable:

```bash
export CONFIG_ENABLED=true
export CONFIG_SERVER_URL=http://localhost:8888
```

## Eureka timing

Clients use `registry-fetch-interval-seconds: 5`.

After starting gateway + a service, wait a few seconds before testing `lb://` routes. Immediate 503 `No servers available for service: ...` usually means the gateway cache has not refreshed yet — not necessarily a bad route.

## Ports (HTTP)

| Service | Port |
|---------|------|
| api-gateway | 8080 |
| discovery-service | 8761 |
| config-server | 8888 |
| auth-service … media-service | 8081–8101 |

gRPC for domain services: `90xx` where xx mirrors the HTTP offset (`8082` → `9082`).
