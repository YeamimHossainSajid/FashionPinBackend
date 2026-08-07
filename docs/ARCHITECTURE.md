# Fashion Pin Architecture

See also: [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) · [GATEWAY_ROUTES.md](./GATEWAY_ROUTES.md) · [adr/](./adr/)

## Style

- Microservices
- Event-driven integration
- DDD-oriented module boundaries
- Clean / hexagonal packaging inside services (`controller`, `service`, `repository`, `client`, `kafka`, `grpc`)

## Bounded contexts (initial)

- Identity & Access: `auth-service`, `user-service`, `profile-service`
- Discovery & Content: `fashion-discovery-service`, `moodboard-service`, `media-service`
- Catalog & Search: `product-service`, `search-service`, `visual-search-service`
- Intelligence: `recommendation-service`, `ai-stylist-service`, `outfit-detection-service`, `image-processing-service`, `virtual-tryon-service`
- Commerce: `shopping-service`, `order-service`, `payment-service`, `inventory-service`, `brand-integration-service`
- Platform: `notification-service`, `analytics-service`
- Edge/Platform infra: `api-gateway`, `discovery-service`, `config-server`

## Data ownership

Each service owns a dedicated PostgreSQL database. Cross-service data access must happen through APIs or events, never shared tables.

## Observability

- Correlation ID propagation (`X-Correlation-Id`)
- Structured console logging with MDC
- Micrometer metrics + Prometheus scrape endpoints
- Zipkin tracing bridge

## Resilience placeholders

- Gateway rate limiter (Redis RequestRateLimiter)
- Eureka registration/heartbeat
- Feign + load balancer ready for retries/circuit breakers in later phases
