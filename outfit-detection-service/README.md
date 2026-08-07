# outfit-detection-service

Foundation module for outfit-detection-service. Owns database `outfit_detection_db` and exposes health endpoints only.

## Port

`8088`

## Health

- Actuator: `http://localhost:8088/actuator/health`
- API health: `http://localhost:8088/api/v1/health`

## Local run

```bash
mvn -pl outfit-detection-service spring-boot:run
```

## Docker

```bash
docker compose up outfit-detection-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
