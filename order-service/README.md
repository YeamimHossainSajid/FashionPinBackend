# order-service

Foundation module for order-service. Owns database `order_db` and exposes health endpoints only.

## Port

`8095`

## Health

- Actuator: `http://localhost:8095/actuator/health`
- API health: `http://localhost:8095/api/v1/health`

## Local run

```bash
mvn -pl order-service spring-boot:run
```

## Docker

```bash
docker compose up order-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
