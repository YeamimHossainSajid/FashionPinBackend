# moodboard-service

Foundation module for moodboard-service. Owns database `moodboard_db` and exposes health endpoints only.

## Port

`8093`

## Health

- Actuator: `http://localhost:8093/actuator/health`
- API health: `http://localhost:8093/api/v1/health`

## Local run

```bash
mvn -pl moodboard-service spring-boot:run
```

## Docker

```bash
docker compose up moodboard-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
