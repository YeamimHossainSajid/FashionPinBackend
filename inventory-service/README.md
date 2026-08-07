# inventory-service

Foundation module for inventory-service. Owns database `inventory_db` and exposes health endpoints only.

## Port

`8097`

## Health

- Actuator: `http://localhost:8097/actuator/health`
- API health: `http://localhost:8097/api/v1/health`

## Local run

```bash
mvn -pl inventory-service spring-boot:run
```

## Docker

```bash
docker compose up inventory-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
