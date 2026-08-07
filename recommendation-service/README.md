# recommendation-service

Foundation module for recommendation-service. Owns database `recommendation_db` and exposes health endpoints only.

## Port

`8087`

## Health

- Actuator: `http://localhost:8087/actuator/health`
- API health: `http://localhost:8087/api/v1/health`

## Local run

```bash
mvn -pl recommendation-service spring-boot:run
```

## Docker

```bash
docker compose up recommendation-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
