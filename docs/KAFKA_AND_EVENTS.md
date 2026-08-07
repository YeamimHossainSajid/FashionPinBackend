# Kafka & Events

## Status in foundation

- Infrastructure wiring exists (producer factory, topic beans, sample event types).
- **No business consumers** are implemented yet.
- In `dev` / `test`, Kafka config/topic beans are profile-gated off (`@Profile("!dev")` / excluded autoconfig) so services start without a broker.

## Shared topic constants

Source of truth: `common-lib` → `com.fashionpin.common.kafka.KafkaTopics`

| Constant | Topic name |
|----------|------------|
| `USER_EVENTS` | `fashionpin.user.events` |
| `ORDER_EVENTS` | `fashionpin.order.events` |
| `PRODUCT_EVENTS` | `fashionpin.product.events` |
| `NOTIFICATION_EVENTS` | `fashionpin.notification.events` |
| `ANALYTICS_EVENTS` | `fashionpin.analytics.events` |
| `MEDIA_EVENTS` | `fashionpin.media.events` |
| `PAYMENT_EVENTS` | `fashionpin.payment.events` |
| `INVENTORY_EVENTS` | `fashionpin.inventory.events` |

Each service also declares a local topic bean pattern:

```text
fashionpin.<packagename>.events
```

Example: `user-service` → `fashionpin.userservice.events`

When you add a real integration topic:

1. Add constant to `KafkaTopics`
2. Document it here
3. Prefer Avro/JSON schema evolution strategy before wide production use
4. Keep payload ownership with the producing bounded context

## Event base types

In `common-lib`:

- `BaseEvent` — `eventId`, `eventType`, `occurredAt`, `correlationId`, `source`
- `DomainEvent` — adds `aggregateId`, `aggregateType`, `payload`

Service-local example: `SampleDomainEvent` (placeholder only).

## Producer usage

`KafkaProducerService.publish(topic, key, payload)`:

- Uses `ObjectProvider<KafkaTemplate<...>>` so missing Kafka beans in `dev` do not crash injection
- Logs and no-ops if template unavailable

## Consumer rules (for future implementers)

- Consumer group id defaults to the service name
- Trust packages: `com.fashionpin.*`
- Always propagate / log `correlationId`
- Prefer idempotent handlers
- Do not block request threads on publish; use transactional outbox when consistency matters (not implemented yet)

## Local broker

```bash
docker compose up -d zookeeper kafka
export SPRING_PROFILES_ACTIVE=docker
# or run with prod/docker profile so Kafka configs activate
```

Compose advertises `kafka:9092` inside the network and `localhost:9092` for host clients (`PLAINTEXT_HOST`).
