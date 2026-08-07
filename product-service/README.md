# product-service

Foundation module for product-service. Owns database `product_db` and exposes health endpoints only.

## Port

`8085`

## Health

- Actuator: `http://localhost:8085/actuator/health`
- API health: `http://localhost:8085/api/v1/health`

## Local run

```bash
mvn -pl product-service spring-boot:run
```

## Docker

```bash
docker compose up product-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
