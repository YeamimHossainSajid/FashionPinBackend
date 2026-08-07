# fashion-discovery-service

Foundation module for fashion-discovery-service. Owns database `fashion_discovery_db` and exposes health endpoints only.

## Port

`8084`

## Health

- Actuator: `http://localhost:8084/actuator/health`
- API health: `http://localhost:8084/api/v1/health`

## Local run

```bash
mvn -pl fashion-discovery-service spring-boot:run
```

## Docker

```bash
docker compose up fashion-discovery-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
