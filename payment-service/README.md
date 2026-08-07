# payment-service

Foundation module for payment-service. Owns database `payment_db` and exposes health endpoints only.

## Port

`8096`

## Health

- Actuator: `http://localhost:8096/actuator/health`
- API health: `http://localhost:8096/api/v1/health`

## Local run

```bash
mvn -pl payment-service spring-boot:run
```

## Docker

```bash
docker compose up payment-service
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
