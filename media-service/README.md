# media-service

Foundation module for media-service. Owns database `media_db` and exposes health endpoints only.

## Port

`8101`

## Health

- Actuator: `http://localhost:8101/actuator/health`
- API health: `http://localhost:8101/api/v1/health`

## Local run

```bash
mvn -pl media-service spring-boot:run
```

## Docker

```bash
docker compose up media-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
