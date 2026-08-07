# Troubleshooting

Failures we already hit during foundation bring-up — and how to fix them fast.

## Build fails with Lombok / `TypeTag :: UNKNOWN`

**Cause:** Wrong JDK (e.g. Java 26). Lombok in this stack needs Java 21.

**Fix:**

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn clean package -DskipTests
```

## `BeanDefinitionOverrideException` for `eurekaServerConfig`

**Cause:** A custom `@Configuration` class named `EurekaServerConfig` collides with Spring Cloud Netflix auto-config.

**Fix:** Rename to something like `DiscoveryServerSettings`. Never reuse Spring Cloud bean class names.

## `BeanDefinitionOverrideException` for `correlationIdInterceptor`

**Cause:** Feign `@Bean correlationIdInterceptor()` clashes with MVC component `CorrelationIdInterceptor`.

**Fix:** Name the Feign bean method `feignCorrelationIdInterceptor`.

## Service starts but Eureka shows nothing / gateway `503`

Checklist:

1. Is discovery up? `curl http://localhost:8761/actuator/health`
2. Did the service log `Registered instance ...` on discovery, or registration errors on the client?
3. Wait for `registry-fetch-interval-seconds` (set to **5**). Immediate calls after gateway boot often 503.
4. Confirm service id case: routes use `lb://user-service` (lowercase). Eureka UI shows `USER-SERVICE` — that is normal.

Gateway log line that means empty LB list:

```text
No servers available for service: user-service
```

## Gateway discovery locator returns `404`

Registry was empty when locator built routes, or locator disabled.

- Ensure `spring.cloud.gateway.discovery.locator.enabled=true`
- Ensure load balancer dependency is on gateway classpath
- Retry after Eureka fetch interval
- Prefer explicit rewrite routes for clients

## Gateway actuator `/actuator/gateway/routes` is `404`

Ensure:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  endpoint:
    gateway:
      enabled: true
```

## Redis / Kafka connection errors on local `dev`

`dev` is supposed to exclude Redis autoconfig and gate Kafka beans.

If you still see broker connection attempts:

- Confirm `SPRING_PROFILES_ACTIVE=dev` (or default)
- Confirm you did not enable `docker`/`prod` by accident
- For gateway, redis health is disabled; rate limiter is not attached to routes yet

## Zipkin spam / dropped spans

Zipkin not running. Harmless for foundation, noisy in logs.

- Start Zipkin: `docker compose up -d zipkin`
- Or disable tracing in the active profile (`management.tracing.enabled=false` — already done for gateway `dev`)

## Config client fails at startup

`CONFIG_ENABLED=true` but Config Server down, or bad basic auth.

Foundation default is `CONFIG_ENABLED=false`. Keep it false for local smoke unless Config Server is up.

Credentials default: `config` / `config`.

## Postgres databases missing in Compose

`init-databases.sh` runs only on **first** volume initialization.

If you added a new DB after the volume existed:

```bash
docker compose exec postgres psql -U fashionpin -c 'CREATE DATABASE new_db;'
# or reset volume (destructive):
docker compose down -v
```

## Port already in use

Foundation ports: gateway `8080`, Eureka `8761`, Config `8888`, services `8081–8101`, gRPC `90xx`.

```bash
lsof -iTCP:8080 -sTCP:LISTEN
kill <pid>
```

## Docker build fails on `./mvnw`

Dockerfiles expect a Maven wrapper or system Maven strategy from the repo root build context. Prefer building jars on host with Java 21, or ensure wrapper/Maven is available inside the build image path you use.

For local iteration, host `mvn -pl <service> package` then run the jar is fastest.

## Regenerated files wiped my edits

You ran `scripts/generate_foundation.py` on an already customized tree.

See [CODE_GENERATION.md](./CODE_GENERATION.md). Prefer copying an existing service module for new work.
