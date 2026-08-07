# api-gateway

Spring Cloud Gateway edge service.

## Port

`8080`

## Health

- Actuator: `http://localhost:8080/actuator/health`
- API health: `http://localhost:8080/api/v1/health`

## Local run

```bash
mvn -pl api-gateway spring-boot:run
```

## Docker

```bash
docker compose up api-gateway
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
