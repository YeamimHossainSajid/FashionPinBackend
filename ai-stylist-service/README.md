# ai-stylist-service

Foundation module for ai-stylist-service. Owns database `ai_stylist_db` and exposes health endpoints only.

## Port

`8091`

## Health

- Actuator: `http://localhost:8091/actuator/health`
- API health: `http://localhost:8091/api/v1/health`

## Local run

```bash
mvn -pl ai-stylist-service spring-boot:run
```

## Docker

```bash
docker compose up ai-stylist-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
