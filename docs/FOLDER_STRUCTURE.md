# Folder Explanation

Index of all docs: [README.md](./README.md)

## Root

- `pom.xml` - parent Maven aggregator and dependency management
- `common-lib/` - shared error model, events, security constants
- `config-repo/` - centralized configuration source for Config Server
- `docker-compose.yml` - full local/prod-like runtime topology
- `observability/` - Prometheus and Grafana provisioning
- `docker/postgres/` - per-service database bootstrap
- `docs/` - maintainer docs (runbooks, troubleshooting, ADRs) — start at `docs/README.md`
- `scripts/` - bootstrap generator only; see `docs/CODE_GENERATION.md` before re-running

## Per service

- `config/` - Spring configuration beans
- `controller/` - inbound HTTP adapters
- `service/` - application services
- `repository/` - persistence ports
- `entity/` - JPA entities
- `dto/` - transport objects
- `mapper/` - MapStruct mappers
- `security/` - security filters/config placeholders
- `exception/` - local exception handling
- `client/` - OpenFeign clients
- `event/` - domain/integration events
- `kafka/` - producer/consumer/topic config
- `grpc/` - gRPC server/client placeholders
- `util/` - utilities (correlation IDs, etc.)
- `validation/` - custom validators
- `health/` - custom health indicators
