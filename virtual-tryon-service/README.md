# virtual-tryon-service

Foundation module for virtual-tryon-service. Owns database `virtual_tryon_db` and exposes health endpoints only.

## Port

`8092`

## Health

- Actuator: `http://localhost:8092/actuator/health`
- API health: `http://localhost:8092/api/v1/health`

## Local run

```bash
mvn -pl virtual-tryon-service spring-boot:run
```

## Docker

```bash
docker compose up virtual-tryon-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
