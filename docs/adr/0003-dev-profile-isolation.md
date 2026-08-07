# ADR 0003: Dev Profile Isolates Kafka / Redis / Postgres

## Status

Accepted

## Context

Requiring full infra for every local health-check makes onboarding and CI smoke painful.

## Decision

Default `dev` profile:

- H2 in-memory (PostgreSQL mode) instead of Postgres
- Exclude Redis autoconfiguration
- Do not activate Kafka producer/topic `@Configuration` beans (`@Profile("!dev")`)
- Keep Eureka optional-but-expected for gateway path tests; services still start if Eureka is down (with registration warnings)

`docker` / `prod` profiles use real Postgres/Kafka/Redis.

## Consequences

- Fast local boot for foundation work
- Dev behavior ≠ prod behavior — integration testing must use `docker`/Testcontainers before release
- Developers can think Kafka “works in dev” when it is intentionally off; docs must say so
