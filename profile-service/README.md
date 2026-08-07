# profile-service

Foundation module for profile-service. Owns database `profile_db` and exposes health endpoints only.

## Port

`8083`

## Health

- Actuator: `http://localhost:8083/actuator/health`
- API health: `http://localhost:8083/api/v1/health`

## Local run

```bash
mvn -pl profile-service spring-boot:run
```

## Docker

```bash
docker compose up profile-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
