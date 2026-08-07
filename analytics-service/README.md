# analytics-service

Foundation module for analytics-service. Owns database `analytics_db` and exposes health endpoints only.

## Port

`8100`

## Health

- Actuator: `http://localhost:8100/actuator/health`
- API health: `http://localhost:8100/api/v1/health`

## Local run

```bash
mvn -pl analytics-service spring-boot:run
```

## Docker

```bash
docker compose up analytics-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
