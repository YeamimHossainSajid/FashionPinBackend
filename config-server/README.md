# config-server

Centralized Spring Cloud Config Server.

## Port

`8888`

## Health

- Actuator: `http://localhost:8888/actuator/health`
- API health: `http://localhost:8888/api/v1/health`

## Local run

```bash
mvn -pl config-server spring-boot:run
```

## Docker

```bash
docker compose up config-server
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
