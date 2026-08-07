# Adding a New Microservice

Follow this checklist so the new service behaves like the existing foundation modules.

## 1. Choose identity

Pick:

- `service-name` (kebab-case), e.g. `wishlist-service`
- HTTP port (next free after `8101` unless reclaiming)
- gRPC port = HTTP port + 1000 (convention used today)
- Dedicated Postgres DB name, e.g. `wishlist_db`
- Java package: `com.fashionpin.wishlistservice`
- Spring application name: exactly `wishlist-service` (Eureka / Feign / Gateway depend on this)

## 2. Create Maven module

1. Copy an existing domain service (e.g. `user-service`) as the template — **preferred over re-running the generator**.
2. Add the module to root `pom.xml` `<modules>`.
3. Rename:
   - `artifactId`
   - main application class
   - base package directories
   - `spring.application.name`
   - ports / DB names in `application.yml`
   - README

Do **not** blindly re-run `scripts/generate_foundation.py` on a mature tree; it overwrites files. See [CODE_GENERATION.md](./CODE_GENERATION.md).

## 3. Keep the standard package layout

```text
src/main/java/com/fashionpin/<pkg>/
  config/ controller/ service/ repository/ entity/ dto/ mapper/
  security/ exception/ client/ event/ kafka/ grpc/
  util/ validation/ health/
src/main/proto/health.proto
src/main/resources/application.yml
src/main/resources/bootstrap.yml
src/main/resources/logback-spring.xml
Dockerfile
README.md
```

Minimum runtime surface for foundation:

- `GET /api/v1/health`
- Actuator health + prometheus
- Security permitting health/docs/actuator
- Eureka client registration
- Global exception handler using `common-lib` types

## 4. Wire Config Server files

Add under `config-repo/`:

- `wishlist-service.yml`
- `wishlist-service-dev.yml`
- `wishlist-service-test.yml`
- `wishlist-service-prod.yml`

If you keep classpath native search active, also copy into:

`config-server/src/main/resources/config-repo/`

## 5. Wire API Gateway

In `api-gateway/src/main/resources/application.yml` add:

```yaml
- id: wishlist-service
  uri: lb://wishlist-service
  predicates:
    - Path=/api/v1/wishlist/**
  filters:
    - RewritePath=/api/v1/wishlist(?<segment>/?.*), /api/v1${segment}
```

Resulting public health path:

```text
GET /api/v1/wishlist/health  ->  wishlist-service /api/v1/health
```

Discovery locator also exposes:

```text
GET /wishlist-service/api/v1/health
```

Document the route in [GATEWAY_ROUTES.md](./GATEWAY_ROUTES.md).

## 6. Wire Docker

1. Ensure module `Dockerfile` exists (copy from another service; update module name/port).
2. Add service block to `docker-compose.yml` (env: Eureka, DB, Redis, Kafka, Zipkin, `GRPC_PORT`).
3. Add `CREATE DATABASE wishlist_db;` to `docker/postgres/init-databases.sh`.
4. Add Prometheus scrape job in `observability/prometheus/prometheus.yml`.

## 7. Wire shared constants (if needed)

- Kafka topic constants → `common-lib` `KafkaTopics` + [KAFKA_AND_EVENTS.md](./KAFKA_AND_EVENTS.md)
- Shared error codes → `common-lib` exception types
- Feign client stubs in peer services only when a real call path exists

## 8. Avoid known bean-name landmines

| Bad name | Why | Safer name |
|----------|-----|------------|
| Class/bean `EurekaServerConfig` | Collides with Spring Cloud Eureka auto-config | `DiscoveryServerSettings` |
| `@Bean correlationIdInterceptor` (Feign) | Collides with MVC `CorrelationIdInterceptor` component | `feignCorrelationIdInterceptor` |

Spring Boot disables bean definition overriding by default — collisions fail startup.

## 9. Verify

```bash
mvn -pl wishlist-service -am package -DskipTests
mvn -pl discovery-service spring-boot:run   # if not already up
mvn -pl wishlist-service spring-boot:run
# confirm Eureka registration, then:
curl http://localhost:<port>/api/v1/health
curl http://localhost:8080/api/v1/wishlist/health
```

## 10. Docs to update (mandatory)

- Root `README.md` modules table
- [GATEWAY_ROUTES.md](./GATEWAY_ROUTES.md)
- [ENVIRONMENT_AND_PROFILES.md](./ENVIRONMENT_AND_PROFILES.md) port/DB tables if present
- [ARCHITECTURE.md](./ARCHITECTURE.md) bounded context list
- New service `README.md`

If you skip docs updates, the next person (including future you) will assume the service does not exist.
