# Fashion Pin Backend

Production-grade Spring Boot microservices foundation for **Fashion Pin**, a visual fashion discovery platform.

This repository contains infrastructure, configuration, communication, observability, and service skeletons only.
Business APIs, AI models, and domain features are intentionally not implemented.

## Documentation (start here for maintenance)

Full maintainer docs live under [`docs/`](./docs/README.md):

| Need | Doc |
|------|-----|
| Local setup / daily workflow | [docs/DEVELOPER_GUIDE.md](./docs/DEVELOPER_GUIDE.md) |
| Add a new microservice | [docs/ADDING_A_SERVICE.md](./docs/ADDING_A_SERVICE.md) |
| Profiles, env vars, DB names | [docs/ENVIRONMENT_AND_PROFILES.md](./docs/ENVIRONMENT_AND_PROFILES.md) |
| Gateway URL map | [docs/GATEWAY_ROUTES.md](./docs/GATEWAY_ROUTES.md) |
| Kafka topics / events | [docs/KAFKA_AND_EVENTS.md](./docs/KAFKA_AND_EVENTS.md) |
| What security actually does | [docs/SECURITY.md](./docs/SECURITY.md) |
| When something breaks | [docs/TROUBLESHOOTING.md](./docs/TROUBLESHOOTING.md) |
| Generator script dangers | [docs/CODE_GENERATION.md](./docs/CODE_GENERATION.md) |
| Why decisions were made | [docs/adr/](./docs/adr/) |

## Architecture Overview

- Microservices with independent Maven modules
- Domain-oriented service boundaries
- Event-driven communication via Apache Kafka
- Synchronous communication via OpenFeign and gRPC placeholders
- Service discovery with Netflix Eureka
- Edge routing with Spring Cloud Gateway
- Centralized configuration with Spring Cloud Config
- Database-per-service with PostgreSQL
- Observability with Actuator, Micrometer, Prometheus, Grafana, and Zipkin

```text
Clients
   |
   v
API Gateway (JWT/Auth/Rate-limit placeholders, CORS, logging)
   |
   +--> Eureka Discovery
   |
   +--> Config Server
   |
   +--> Domain Services (auth, user, product, search, AI, commerce, ...)
          |                |
          | Kafka events   | Feign / gRPC
          v                v
     Brokers/Topics   Peer services
```

## Tech Stack

Java 21, Spring Boot 3.3.x, Spring Cloud 2023.0.x, Spring Security, Gateway, Eureka, Config Server, Actuator, JPA, PostgreSQL, Redis, Kafka, Docker, gRPC, OpenFeign, MapStruct, Lombok, Validation, OpenAPI, Micrometer, Prometheus, Grafana, Zipkin, JUnit 5, Testcontainers, Maven.

## Modules

| Service | Port | Database |
|---------|------|----------|
| `discovery-service` | 8761 | n/a |
| `config-server` | 8888 | n/a |
| `api-gateway` | 8080 | n/a |
| `auth-service` | 8081 | auth_db |
| `user-service` | 8082 | user_db |
| `profile-service` | 8083 | profile_db |
| `fashion-discovery-service` | 8084 | fashion_discovery_db |
| `product-service` | 8085 | product_db |
| `search-service` | 8086 | search_db |
| `recommendation-service` | 8087 | recommendation_db |
| `outfit-detection-service` | 8088 | outfit_detection_db |
| `image-processing-service` | 8089 | image_processing_db |
| `visual-search-service` | 8090 | visual_search_db |
| `ai-stylist-service` | 8091 | ai_stylist_db |
| `virtual-tryon-service` | 8092 | virtual_tryon_db |
| `moodboard-service` | 8093 | moodboard_db |
| `shopping-service` | 8094 | shopping_db |
| `order-service` | 8095 | order_db |
| `payment-service` | 8096 | payment_db |
| `inventory-service` | 8097 | inventory_db |
| `brand-integration-service` | 8098 | brand_integration_db |
| `notification-service` | 8099 | notification_db |
| `analytics-service` | 8100 | analytics_db |
| `media-service` | 8101 | media_db |
| `common-lib` | n/a | shared DTOs/errors/events |

## Folder Structure

Each business service follows:

```text
src/main/java/com/fashionpin/<service>/
  config/
  controller/
  service/
  repository/
  entity/
  dto/
  mapper/
  security/
  exception/
  client/
  event/
  kafka/
  grpc/
  util/
  validation/
  health/
```

## How Services Communicate

1. **Client -> Gateway**: all external traffic enters through `api-gateway:8080`
2. **Gateway -> Services**: Eureka-backed `lb://service-name` routes
3. **Service -> Service (sync)**: OpenFeign clients and gRPC placeholders
4. **Service -> Service (async)**: Kafka topics under `fashionpin.*.events`
5. **Config**: optional Config Server (`CONFIG_ENABLED=true`) with `dev` / `test` / `prod` profiles

## Prerequisites

- **Java 21** (required; Lombok does not support newer JDKs for this foundation)
- Maven 3.9+
- Docker & Docker Compose

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

## Build

```bash
./mvnw clean package -DskipTests
```

## Run locally (foundation profile)

Start infrastructure first (recommended):

```bash
docker compose up -d postgres redis zookeeper kafka zipkin discovery-service config-server
```

Then run a service:

```bash
./mvnw -pl discovery-service spring-boot:run
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl user-service spring-boot:run
```

Dev profile uses in-memory H2 so services can boot without PostgreSQL for foundation smoke checks.
Kafka/Redis autoconfig is disabled in `dev`/`test` to keep local startup reliable.

## Docker instructions

```bash
# Build and start the full stack
docker compose up --build

# Start only platform dependencies
docker compose up -d postgres redis zookeeper kafka zipkin prometheus grafana discovery-service config-server api-gateway
```

Useful URLs:

- Eureka: http://localhost:8761
- Gateway health: http://localhost:8080/api/v1/health
- Config Server: http://localhost:8888
- Zipkin: http://localhost:9411
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## Health endpoints

Every service exposes:

- `/actuator/health`
- `/api/v1/health`
- `/actuator/prometheus`

Gateway example route:

```text
GET /api/v1/user/health  ->  user-service /api/v1/health
```

Because gateway discovery locator is enabled, services are also reachable as:

```text
GET /user-service/api/v1/health
```

## Security placeholders

- JWT filter placeholders in gateway and services
- Role-based authorization placeholder (`ROLE_ADMIN`, `ROLE_USER`)
- No production auth implementation yet

## Coding standards

- Constructor injection only
- No wildcard imports
- SOLID / clean architecture packaging
- Shared error model in `common-lib`
- Service-owned databases only

## Implementation Phases & Roadmap

- [x] **Phase 1: Identity & User Domain (Completed)**
  - Auth token issuance, password hashing, refresh token rotation & revocation (`auth-service`).
  - Event-driven user creation & profile initialization (`user-service`, `profile-service`).
  - Gateway routes & JWT security foundation (`api-gateway`).
  - Transactional Outbox pattern & consumer idempotency.

- [x] **Phase 2: Product Catalog & Visual Media Domain (Completed)**
  - Hexagonal object storage abstraction & media metadata lifecycle (`media-service`).
  - Brand catalog integration & provider abstractions (`brand-integration-service`).
  - Product catalog aggregates with fashion attributes, OpenFeign validation, & Redis caching (`product-service`).
  - Gateway routes & outbox events (`MediaCreated`, `BrandCreated`, `ProductCreated`, etc.).

- [ ] **Phase 3: Moodboards & Social Graph Domain (Remaining TODO)**
  - User follow/following graph & social interactions (`profile-service`).
  - Curation moodboards, saved pins, and collections (`moodboard-service`).

- [ ] **Phase 4: AI & Computer Vision Domain (Remaining TODO)**
  - Visual search & image processing pipeline (`visual-search-service`, `image-processing-service`).
  - AI stylist recommendation engine (`ai-stylist-service`).
  - Virtual try-on engine (`virtual-tryon-service`).

- [ ] **Phase 5: Commerce, Orders & Payments (Remaining TODO)**
  - Shopping cart & bag (`shopping-service`).
  - Order checkout & processing (`order-service`).
  - Payment gateway integration (`payment-service`).

## License

Proprietary - Fashion Pin startup foundation.
