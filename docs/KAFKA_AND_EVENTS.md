# Kafka & Events (Phase 1 Implementation)

## Implemented Topics & Event Schemas

Source of truth: `common-lib` → `com.fashionpin.common.kafka.KafkaTopics`

| Topic Constant | Topic Name | Producer Service | Consumer Service | Payload Class |
|----------------|------------|------------------|------------------|---------------|
| `USER_REGISTERED_V1` | `fashionpin.user.registered.v1` | `auth-service` | `user-service` | `UserRegisteredEvent` |
| `USER_CREATED_V1` | `fashionpin.user.created.v1` | `user-service` | `profile-service` | `UserCreatedEvent` |

## Transactional Outbox Pattern & Reliability

To prevent database-Kafka dual-write failures, events are published using the **Transactional Outbox Pattern**:

1. **DB Transaction**: Service writes entity state and event payload to `outbox_events` table within a single PostgreSQL transaction.
2. **Outbox Relay**: Scheduled background worker (`OutboxPublisherScheduler`) polls pending outbox events every 2 seconds, publishes to Kafka via `KafkaProducerService`, and marks status as `PROCESSED`.

## Consumer Idempotency Strategy

Consumers enforce strict idempotency to handle potential duplicate Kafka deliveries:

1. **Idempotency Check**: Consumers check `processed_events` table by `eventId` before processing.
2. **Duplicate Handling**: If `eventId` exists, processing is skipped with an info log.
3. **Atomic Execution**: Entity creation and `processed_events` record creation occur within the consumer's DB transaction.

## Event Base Types

In `common-lib`:
- `BaseEvent` — `eventId`, `eventType`, `occurredAt`, `correlationId`, `source`
- `UserRegisteredEvent` — `eventId`, `eventType`, `timestamp`, `correlationId`, `userId`, `email`
- `UserCreatedEvent` — `eventId`, `eventType`, `timestamp`, `correlationId`, `userId`, `email`

## Remaining TODOs (Future Event Domains)

1. **Phase 2 (Product & Fashion Discovery)**:
   - `fashionpin.product.created.v1`
   - `fashionpin.fashion.pin.created.v1`
2. **Phase 3 (Social & Moodboards)**:
   - `fashionpin.user.followed.v1`
   - `fashionpin.moodboard.created.v1`
3. **Phase 4 (AI & Search Indexing)**:
   - `fashionpin.image.processed.v1`
   - `fashionpin.search.index.updated.v1`
4. **Phase 5 (Commerce & Orders)**:
   - `fashionpin.order.created.v1`
   - `fashionpin.payment.processed.v1`
