# Backend Java Developer — Ride-Sharing API

## Must Have

**Language & Framework**
- **Java 17+** (project uses Java 25 with records, sealed classes, pattern matching)
- **Spring Boot** — dependency injection, REST controllers, configuration, async processing
- **Spring Data JDBC** (not JPA/Hibernate) — repositories, entities, embedded types, callbacks

**Data**
- **PostgreSQL** — schema design, SQL queries

**Testing**
- **JUnit 5, AssertJ, MockMvc, Mockito** — unit and integration tests, parameterized tests
- **Docker** — required for local development (Testcontainers)

**Build & Architecture**
- **Maven** — build lifecycle, dependency management
- Experience with **layered architecture** and clean separation of concerns (Controller → Service → Store → Repository)

## Nice to Have

**Language & Framework**
- **Java Platform Module System** (JPMS / `module-info.java`)
- **Immutable data modeling** — Java records, static factory methods, functional update patterns

**Messaging & Real-Time**
- **Kafka** — event publishing, transactional outbox pattern
- **WebSocket / STOMP** — real-time messaging

**API Design**
- **OpenAPI / Swagger** — API-first design with annotations

**Testing**
- **Testcontainers** — integration testing with containerized dependencies

**Code Quality**
- **Null-safety tooling** — JSpecify, NullAway, ErrorProne
- **Code formatting tools** — Spotless, Palantir Java Format

## Mindset

- Preference for **simplicity over abstraction** — write only what's needed, no speculative code
- Comfortable with **strict code conventions** and automated formatting/linting
- Values **test coverage** across both unit and integration layers
- Familiarity with **RFC 7807 problem details** and REST API best practices
