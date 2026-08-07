# search-service

Foundation module for search-service. Owns database `search_db` and exposes health endpoints only.

## Port

`8086`

## Health

- Actuator: `http://localhost:8086/actuator/health`
- API health: `http://localhost:8086/api/v1/health`

## Local run

```bash
mvn -pl search-service spring-boot:run
```

## Docker

```bash
docker compose up search-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
