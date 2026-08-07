# ADR 0001: Foundation Stack & Module Layout

## Status

Accepted (foundation phase)

## Context

Fashion Pin needs a scalable multi-service backend for discovery, commerce, media, and AI features, but business logic is not ready yet. We need a compile-and-run skeleton teams can extend without re-litigating infrastructure.

## Decision

- Java 21 + Spring Boot 3.3.x + Spring Cloud 2023.0.x
- One Maven parent + independent modules per service
- Shared `common-lib` for errors/events/security constants only (not a dumping ground for domain models)
- Netflix Eureka for discovery, Spring Cloud Gateway at the edge, Config Server for centralized config
- Kafka for async, OpenFeign + gRPC placeholders for sync
- Standard hexagonal-ish package layout inside each service

## Consequences

- Clear service boundaries and ports from day one
- Higher ops complexity than a monolith (accepted)
- Must keep docs/runbooks current when adding services
- Lombok pins us to Java 21 until toolchain upgrades are validated
