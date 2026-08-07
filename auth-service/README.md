# auth-service

Foundation module for auth-service. Owns database `auth_db` and exposes health endpoints only.

## Port

`8081`

## Health

- Actuator: `http://localhost:8081/actuator/health`
- API health: `http://localhost:8081/api/v1/health`

## Local run

```bash
mvn -pl auth-service spring-boot:run
```

## Docker

```bash
docker compose up auth-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
