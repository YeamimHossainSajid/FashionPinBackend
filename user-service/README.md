# user-service

Foundation module for user-service. Owns database `user_db` and exposes health endpoints only.

## Port

`8082`

## Health

- Actuator: `http://localhost:8082/actuator/health`
- API health: `http://localhost:8082/api/v1/health`

## Local run

```bash
mvn -pl user-service spring-boot:run
```

## Docker

```bash
docker compose up user-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
