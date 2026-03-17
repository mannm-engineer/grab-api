# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build and run all tests
mvn -B clean package

# Run tests only
mvn test

# Apply code formatting (Google Java style)
mvn spotless:apply

# Check formatting without applying
mvn spotless:check

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName
```

The CI pipeline runs `mvn clean package`, which includes the Spotless format check at the `validate` phase. Always run `mvn spotless:apply` before committing to avoid CI failures.

## Architecture

This is a Spring Boot 4.0 ride-sharing backend API (Java 25, Maven) with the following key services:

- **DriverService** — manages driver lifecycle (create with document file uploads, update location with audit)
- **RideService** — handles ride request creation
- **RideDispatchService** — async (`@Async`) service that finds the nearest available driver and sends WebSocket notifications
- **NotificationService** — sends real-time STOMP/WebSocket messages to drivers at `/topic/user/{driverId}`
- **OutboxEventPublishScheduler** — polls outbox table every 5 seconds, publishes domain events (e.g. `DRIVER_CREATED`) to Kafka using `SELECT ... FOR UPDATE SKIP LOCKED` for multi-instance safety

### Layered Architecture

```
Controller (REST/DTO) → Service (domain logic) → Store interface → DatabaseStore (Spring Data JDBC)
```

- **Controllers** define API via interface (`DriverApi`, `RideApi`) with OpenAPI annotations, implemented in `*RestController`
- **DTOs** live in `controller/dto/`, converted to/from domain models via `controller/converter/`
- **Domain models** live in `service/domain/`
- **Store pattern**: services depend on `Store` interfaces; `*DatabaseStore` classes implement these using Spring Data JDBC repositories (not JPA)
- **Entities** (`repository/entity/`) are only used at the persistence layer

### Key Patterns

- **Outbox pattern**: domain events written transactionally to `outbox_event` table, then asynchronously published to Kafka by the scheduler
- **Null-safety**: JSpecify annotations enforced by NullAway compiler plugin — annotate new code accordingly
- **Error Prone**: enabled at compile time; avoid suppressing without justification

### Infrastructure

- **Database**: PostgreSQL — schema defined in `src/main/resources/schema.sql` (Spring initializes it on startup)
- **Kafka**: used for domain events (topic `driver-events`)
- **WebSocket**: STOMP endpoint at `/ws`, message broker on `/topic` and `/queue`
- **File storage**: driver documents stored locally at `./uploads`
- **API base path**: `/api`

### Testing

- Integration tests use **TestContainers** (PostgreSQL + Kafka) — see `src/test/java/com/grab/api/integration/`
- `ApiTestExtension` handles DB setup/teardown between integration tests
- Controller tests and unit tests are in separate packages under `src/test/`

### Deployment

- Dockerfile uses `eclipse-temurin:25-jre-alpine`, runs as non-root `spring:spring` user
- Kubernetes manifests in `k8s.yaml` (PostgreSQL, Kafka, grab-api deployment, Ingress with TLS)
- CI/CD via GitHub Actions (`.github/workflows/ci.yaml`): build → Docker push → deploy to K8s via bastion host
