# brand-integration-service

Foundation module for brand-integration-service. Owns database `brand_integration_db` and exposes health endpoints only.

## Port

`8098`

## Health

- Actuator: `http://localhost:8098/actuator/health`
- API health: `http://localhost:8098/api/v1/health`

## Local run

```bash
mvn -pl brand-integration-service spring-boot:run
```

## Docker

```bash
docker compose up brand-integration-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
