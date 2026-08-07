# image-processing-service

Foundation module for image-processing-service. Owns database `image_processing_db` and exposes health endpoints only.

## Port

`8089`

## Health

- Actuator: `http://localhost:8089/actuator/health`
- API health: `http://localhost:8089/api/v1/health`

## Local run

```bash
mvn -pl image-processing-service spring-boot:run
```

## Docker

```bash
docker compose up image-processing-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
