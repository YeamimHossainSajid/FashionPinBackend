# discovery-service

Netflix Eureka service discovery server.

## Port

`8761`

## Health

- Actuator: `http://localhost:8761/actuator/health`
- API health: `http://localhost:8761/api/v1/health`

## Local run

```bash
mvn -pl discovery-service spring-boot:run
```

## Docker

```bash
docker compose up discovery-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
