# visual-search-service

Foundation module for visual-search-service. Owns database `visual_search_db` and exposes health endpoints only.

## Port

`8090`

## Health

- Actuator: `http://localhost:8090/actuator/health`
- API health: `http://localhost:8090/api/v1/health`

## Local run

```bash
mvn -pl visual-search-service spring-boot:run
```

## Docker

```bash
docker compose up visual-search-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
