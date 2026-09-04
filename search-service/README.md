# search-service

Microservice for product and fashion post search indexing, facet aggregations, and search query execution. Owns database `search_db`.

## Features (Part 2 & Part 3)

- JPA Domain Models: `ProductSearchIndex` and `FashionPostSearchIndex`
- Repositories: `ProductSearchIndexRepository` and `FashionPostSearchIndexRepository` with `JpaSpecificationExecutor`
- Real-time Index Synchronization: `ProductIndexSyncService` and `FashionPostIndexSyncService` with out-of-order Kafka message filtering
- Kafka Listeners: `ProductSearchEventListener` and `FashionPostSearchEventListener`
- Dynamic Query Specifications: `ProductSearchSpecifications` and `FashionPostSearchSpecifications`
- Faceted Aggregations & Autocomplete Service: `SearchCatalogService`
- REST API Controller under `/api/search`:
  - `GET /api/search/products`: Filter by text, brand, category, subcategory, color, size, price range, stock, sort, page
  - `GET /api/search/posts`: Filter by query, style, occasion, tag, author
  - `GET /api/search/suggestions`: Autocomplete titles, categories, and tags
  - `GET /api/search/facets`: Dynamic facet calculation across brands, categories, colors, sizes, and price bounds

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
