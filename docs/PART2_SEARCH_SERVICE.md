# Part 2: Search Service Domain & Ingestion Engine Architecture

## Summary

Part 2 implements the core domain persistence and real-time Kafka event ingestion engine for `search-service` (port `8088`).

## Components

1. **JPA Domain Entities**:
   - `ProductSearchIndex`: Maps to PostgreSQL table `product_search_index`. Includes factory methods `fromPayload()` and `updateFromPayload()`.
   - `FashionPostSearchIndex`: Maps to PostgreSQL table `fashion_post_search_index`. Includes factory methods `fromPayload()` and `updateFromPayload()`.
   - Attribute Converters: `StringListConverter` and `UuidListConverter` for multi-value field serialization.

2. **Spring Data JPA Repositories**:
   - `ProductSearchIndexRepository`: Extends `JpaRepository` and `JpaSpecificationExecutor`.
   - `FashionPostSearchIndexRepository`: Extends `JpaRepository` and `JpaSpecificationExecutor`.

3. **Idempotent Sync Services**:
   - `ProductIndexSyncService`: Real-time upsert and delete operations with timestamp-based out-of-order event filtering.
   - `FashionPostIndexSyncService`: Real-time post upsert and delete operations.

4. **Kafka Consumers & Event Listeners**:
   - `KafkaConsumerConfig`: Error handling via `DefaultErrorHandler` with fixed backoff.
   - `ProductSearchEventListener`: Consumes product events (`created`, `updated`, `deleted`).
   - `FashionPostSearchEventListener`: Consumes post events (`created`, `updated`, `deleted`).

5. **Verification**:
   - `ProductIndexSyncServiceTest`: Unit tests for idempotent upsert and timestamp filtering.
   - `SearchEventListenerIntegrationTest`: Integration tests for Kafka listener event routing.
