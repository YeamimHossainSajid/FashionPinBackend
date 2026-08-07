# ADR 0002: Database Per Service

## Status

Accepted

## Context

Shared databases across microservices create hidden coupling, unsafe joins, and blocked independent deploys.

## Decision

Each domain service owns one PostgreSQL database. No cross-service SQL. Integration only via APIs or events.

Compose bootstraps databases in `docker/postgres/init-databases.sh`.

## Consequences

- Independent schema evolution per service
- Need eventual consistency patterns later (outbox, sagas) for multi-service workflows
- More databases to operate and back up
