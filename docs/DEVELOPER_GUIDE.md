# Developer Guide

Practical operating manual for the Fashion Pin backend foundation.

## Hard requirements

- **Java 21 only** for compile/run. Newer JDKs break Lombok annotation processing in this stack.
- Maven 3.9+
- Docker + Docker Compose for infra / full stack

```bash
# macOS Homebrew example
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # must show 21.x
```

There is a `.java-version` file set to `21` for jenv/sdkman-style tools.

## Repository mental model

```text
Parent POM (fashion-pin)
├── common-lib          shared errors/events/security constants
├── discovery-service   Eureka
├── config-server       Spring Cloud Config (native/git)
├── api-gateway         Spring Cloud Gateway edge
└── *-service           domain services (DB-per-service)
```

This phase is **foundation only**: health endpoints, wiring, placeholders. No business APIs yet.

## Build

```bash
mvn clean package -DskipTests
# or
./mvnw clean package -DskipTests
```

`./mvnw` currently delegates to system `mvn` if present. Prefer an installed Maven 3.9+ with Java 21.

Build a subset:

```bash
mvn -pl api-gateway,user-service -am package -DskipTests
```

## Recommended local bring-up

### Option A — fastest foundation smoke (no Postgres/Kafka/Redis)

1. Start Eureka:

```bash
mvn -pl discovery-service spring-boot:run
```

2. Start one domain service (uses H2 in `dev`):

```bash
mvn -pl user-service spring-boot:run
```

3. Start gateway:

```bash
mvn -pl api-gateway spring-boot:run
```

4. Verify:

```bash
curl http://localhost:8761/api/v1/health
curl http://localhost:8082/api/v1/health
curl http://localhost:8080/api/v1/health
# wait a few seconds for Eureka fetch (registry-fetch-interval-seconds=5)
curl http://localhost:8080/api/v1/user/health
curl http://localhost:8080/user-service/api/v1/health
```

### Option B — infra in Docker, apps on host

```bash
docker compose up -d postgres redis zookeeper kafka zipkin prometheus grafana
export SPRING_PROFILES_ACTIVE=docker
# or set DB_URL / KAFKA / REDIS env vars and use docker profile per service
```

See [ENVIRONMENT_AND_PROFILES.md](./ENVIRONMENT_AND_PROFILES.md).

### Option C — full stack Compose

```bash
docker compose up --build
```

Useful URLs:

| System | URL |
|--------|-----|
| Eureka | http://localhost:8761 |
| Gateway | http://localhost:8080 |
| Config Server | http://localhost:8888 |
| Zipkin | http://localhost:9411 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (`admin` / `admin`) |

## Profiles cheat sheet

| Profile | DB | Kafka beans | Redis | Typical use |
|---------|----|-------------|-------|-------------|
| `dev` (default) | H2 in-memory | Kafka config/topics disabled (`!dev`) | autoconfig excluded | Local foundation smoke |
| `test` | H2 | disabled similarly | excluded | Unit/context tests |
| `docker` | Postgres | enabled | enabled | Compose / integration |
| `prod` | Postgres (`ddl-auto=validate`) | enabled | enabled | Production-like |

Details: [ENVIRONMENT_AND_PROFILES.md](./ENVIRONMENT_AND_PROFILES.md).

## Where to change what

| Need | Location |
|------|----------|
| Shared error/event types | `common-lib` |
| Gateway routes / filters | `api-gateway/src/main/resources/application.yml` + `.../filter/` |
| Centralized config defaults | `config-repo/` (+ classpath copy under `config-server`) |
| Per-service port/DB | that service’s `application.yml` + `docker-compose.yml` + `docker/postgres/init-databases.sh` |
| Prometheus scrape targets | `observability/prometheus/prometheus.yml` |
| Bootstrap secrets template | `.env.example` |

## Coding conventions (enforced by foundation)

- Constructor injection only (no `@Autowired` on fields)
- No wildcard imports
- Package layout stays as documented in [FOLDER_STRUCTURE.md](./FOLDER_STRUCTURE.md)
- Never share a database across services
- Put cross-cutting types in `common-lib`, not copy-paste across services
- Prefer placeholders over fake business logic until a real feature lands

## Smoke checklist before merging infra changes

1. `mvn clean package -DskipTests` on Java 21
2. Discovery starts on `8761`
3. At least one domain service registers in Eureka UI / `/eureka/apps`
4. Gateway returns 200 for `/api/v1/{service-prefix}/health`
5. Actuator `/actuator/health` is UP on touched services

## Related docs

- New service: [ADDING_A_SERVICE.md](./ADDING_A_SERVICE.md)
- Something broken: [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
- Generator script: [CODE_GENERATION.md](./CODE_GENERATION.md)
