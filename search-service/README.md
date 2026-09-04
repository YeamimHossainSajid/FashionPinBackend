# search-service

Microservice for product and fashion post search indexing, facet aggregations, and search query execution. Owns database `search_db`.

## Features (Part 2)

- JPA Domain Models: `ProductSearchIndex` and `FashionPostSearchIndex`
- Repositories: `ProductSearchIndexRepository` and `FashionPostSearchIndexRepository` with `JpaSpecificationExecutor`
- Real-time Index Synchronization: `ProductIndexSyncService` and `FashionPostIndexSyncService` with out-of-order Kafka message filtering
- Kafka Listeners: `ProductSearchEventListener` and `FashionPostSearchEventListener`

## Port

`8088` (gRPC: `9088`)

## Health

- Actuator: `http://localhost:8088/actuator/health`
- API health: `http://localhost:8088/api/v1/health`

## Local run

```bash
mvn -pl search-service spring-boot:run
```

## Docker

```bash
docker compose up search-service
```
