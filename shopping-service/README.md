# shopping-service

Foundation module for shopping-service. Owns database `shopping_db` and exposes health endpoints only.

## Port

`8094`

## Health

- Actuator: `http://localhost:8094/actuator/health`
- API health: `http://localhost:8094/api/v1/health`

## Local run

```bash
mvn -pl shopping-service spring-boot:run
```

## Docker

```bash
docker compose up shopping-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
