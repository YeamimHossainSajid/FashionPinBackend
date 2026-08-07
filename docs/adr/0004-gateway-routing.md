# ADR 0004: Gateway Rewrite Routes + Eureka Locator

## Status

Accepted

## Context

Clients need a stable public URL shape. Operators also need a quick way to hit services by Eureka id.

## Decision

1. Explicit routes: `/api/v1/<prefix>/**` rewritten to `/api/v1/**` on the target service via `RewritePath`
2. Discovery locator enabled with lower-case service ids: `/<service-name>/**`
3. Eureka registry fetch interval set to 5s to reduce empty-LB races after startup
4. Rate limiting wired as placeholder only (Redis `KeyResolver` present; filter not attached yet)

## Consequences

- Public API prefixes must stay documented in `GATEWAY_ROUTES.md`
- Adding a service requires a gateway YAML change, not only Eureka registration
- Locator paths are convenient but should not be the only public contract
