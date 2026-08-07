# notification-service

Foundation module for notification-service. Owns database `notification_db` and exposes health endpoints only.

## Port

`8099`

## Health

- Actuator: `http://localhost:8099/actuator/health`
- API health: `http://localhost:8099/api/v1/health`

## Local run

```bash
mvn -pl notification-service spring-boot:run
```

## Docker

```bash
docker compose up notification-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
