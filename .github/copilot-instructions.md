# Copilot Instructions for grab-api

## Overview
Grab API is a Spring Boot microservice providing ride-hailing operations (drivers, rides, file uploads). It uses Java 25, PostgreSQL, Kafka for events, and deploys to Kubernetes.

## Build, Test, and Lint Commands

### Build and Test
```bash
# Full build and test (compiles, runs all tests, formats code)
mvn clean package

# Build only (skip tests)
mvn clean package -DskipTests

# Compile and check formatting/linting
mvn validate
```

### Test Execution
```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ApiTest

# Run a single test method
mvn test -Dtest=ApiTest#testMethodName

# Run integration tests only
mvn test -Dgroups=integration

# Run unit tests only
mvn test -Dgroups=unit
```

### Code Formatting and Linting
```bash
# Check code format and style (runs on every build)
mvn spotless:check

# Auto-format all code to Google style
mvn spotless:apply

# All linting is done via compile phase:
# - ErrorProne for common Java errors
# - NullAway for null safety (using @Nullable/@NonNull from jspecify)
# - Spotless for code formatting
```

### Local Development
```bash
# Run the application locally
mvn spring-boot:run

# Skip tests during development (for faster iteration)
mvn clean install -DskipTests && mvn spring-boot:run
```

## High-Level Architecture

### Layered Structure
```
controller/ (HTTP API layer)
├── api/           # Interface definitions (OpenAPI/Swagger documented)
├── converter/     # DTOs ↔ Domain object conversion
├── dto/           # Data Transfer Objects for HTTP requests/responses
└── exception/     # Global exception handling

service/ (Business logic)
├── RideService, DriverService, etc.  # Main domain services
├── domain/        # Domain models (Ride, Driver, etc.)
└── store/         # Interfaces for persistence operations

repository/ (Data access layer)
├── *Repository   # Spring Data JDBC repository interfaces
├── entity/        # JPA entities for database
├── adapter/       # Implements Store interfaces using repositories
└── GlobalAuditCallback  # Automatically sets audit fields (created_at, created_by, etc.)

config/           # Spring configuration beans
scheduler/        # Scheduled tasks (RideDispatchScheduler, OutboxEventPublishScheduler)
share/            # Shared utilities and enumerations
```

### Data Flow Pattern
1. **HTTP Request** → `Controller` (validates input)
2. **Convert** → `converter` (DTO → Domain)
3. **Business Logic** → `Service` (orchestrates)
4. **Persistence** → `Store` interface (abstraction)
5. **Implementation** → `*Adapter` (uses Repository)
6. **Database** → PostgreSQL via Spring Data JDBC

### Event-Driven Architecture
- **Outbox Pattern**: Services create `OutboxEventEntity` records; scheduler publishes to Kafka
- **Kafka Topics**: `driver-events` (configurable in application.yaml)
- **Async Processing**: WebSocket notifications and event subscribers listen to Kafka

### Key Crosscutting Concerns
- **Audit Trail**: `AuditableEntity` interface + `GlobalAuditCallback` auto-populates `created_at/by`, `updated_at/by`
- **Validation**: Spring `@Valid` on DTOs, constraint annotations on domain objects
- **Error Handling**: `GlobalExceptionHandler` in controller/exception for consistent error responses

## Key Conventions

### Naming and Structure
- **API Interfaces**: Placed in `controller/api/` with `*Api` suffix (e.g., `RideApi`, `DriverApi`)
- **Controllers**: Implement corresponding API interface
- **Services**: One service per domain (e.g., `RideService`, `DriverService`)
- **Stores**: Abstract persistence behind `*Store` interface; implementations are `*StoreAdapter` in `repository/adapter/`
- **DTOs**: Use `*DTO` suffix for request/response objects; simple domain models without DTO suffix

### Code Style
- **Formatting**: Google Java style via Spotless (automatically applied on build)
- **Null Safety**: Use `@Nullable` (jspecify) on nullable fields; otherwise assume non-null (enforced by NullAway compiler plugin)
- **Imports**: No wildcard imports; automatically managed by Spotless
- **Formatting Blocks**: Can disable formatting with `// @spotless:off` and `// @spotless:on` if needed

### Testing
- **Unit Tests**: `src/test/java/com/grab/api/unit/` (quick, no DB)
- **Integration Tests**: `src/test/java/com/grab/api/integration/` (use TestContainers for PostgreSQL/Kafka)
- **Test Setup**: Use `@ApiTest` annotation or inherit from `ApiTest` base class; auto-configures Spring context
- **Fixtures**: `TestDataSourceConfig`, `TestKafkaConfig` provide test infrastructure
- **Assertions**: Standard JUnit 5 assertions (preferred: `org.junit.jupiter.api.Assertions`)

### Annotations and Framework
- **Controllers**: `@RestController`, `@RequestMapping`, OpenAPI annotations (`@Tag`, `@Operation`, `@ApiResponse`)
- **Services**: `@Service`
- **Repositories**: Spring Data JDBC `Repository<Entity, ID>` interfaces
- **Validation**: Jakarta `@Valid`, constraint annotations (`@NotNull`, `@NotBlank`, etc.)
- **Scheduling**: `@Scheduled` for periodic tasks; `@EnableScheduling` on Application class

### Database and Schema
- **Auto-Init**: `schema.sql` runs on startup (via `spring.sql.init.mode=always`)
- **Audit Fields**: All entities should implement `AuditableEntity` interface (auto-populated by callback)
- **Migrations**: SQL schema changes go in `src/main/resources/schema.sql`; consider versioning if complexity grows
- **JDBC**: Uses Spring Data JDBC (row mapper automatically converts query results to objects)

### Storage and Files
- **Path**: Configured at `app.storage.path` (default: `./uploads`); **must be shared storage in multi-instance deployments**
- **Access**: `FileService` handles upload/download; see `controller/file/FileApiIntegrationTest.java` for usage

### Kafka and Events
- **Topics**: `driver-events` (defined in `app.kafka.topics.driver-events`)
- **Publishing**: Use `OutboxEventStoreAdapter` to create events; scheduler publishes in batches (default: 50)
- **Outbox Batch Size**: Configured at `outbox.batch-size` in `application.yaml`

### WebSocket
- **Configuration**: `WebSocketConfig` configures message broker and endpoints
- **Usage**: Real-time notifications (e.g., ride status updates) sent over WebSocket

## Important Files and Directories

| Path | Purpose |
|------|---------|
| `pom.xml` | Maven build config; includes ErrorProne, NullAway, Spotless, Spring Boot plugins |
| `src/main/resources/application.yaml` | App config: datasource, Kafka, storage paths, outbox settings |
| `src/main/resources/schema.sql` | Database schema; auto-initialized on startup |
| `.github/workflows/ci.yaml` | CI/CD: builds, tests, builds Docker image, deploys to K8s via Bastion |
| `Dockerfile` | Multi-stage build; runs as non-root; exposes port 8080 |
| `k8s.yaml` | Kubernetes manifest; deployment, service, secrets |

## Common Tasks

### Add a New API Endpoint
1. Define interface in `controller/api/*Api.java` (e.g., `RideApi`)
2. Implement in `controller/*Controller.java` with OpenAPI annotations
3. Add DTO in `controller/dto/`
4. Add converter logic in `controller/converter/`
5. Call service method in controller
6. Add service logic in `service/*Service.java`
7. Add store interface in `service/store/*Store.java`
8. Implement store in `repository/adapter/*StoreAdapter.java` using `*Repository`
9. Add integration test in `src/test/java/com/grab/api/integration/*ApiIntegrationTest.java`
10. Run `mvn test` to ensure all tests pass and code is formatted

### Add a New Entity and Database Table
1. Create entity class in `repository/entity/` implementing `AuditableEntity` for audit trail
2. Add table to `src/main/resources/schema.sql`
3. Create repository interface in `repository/` extending `Repository<Entity, Long>`
4. Create domain model in `service/domain/`
5. Create store interface in `service/store/`
6. Create adapter in `repository/adapter/` implementing store using repository

### Publish an Event to Kafka
1. Create `OutboxEventEntity` via `OutboxEventStore.create(...)`
2. Scheduler (`OutboxEventPublishScheduler`) publishes periodically
3. Subscribe to events in services or external systems listening to Kafka topic

### Debug Failed Tests
- Check test logs: errors from TestContainers, assertions, or Spring context initialization
- Most failures are in integration tests (TestContainer startup, database state)
- Use `@ApiTest` annotation to auto-configure test environment
- Verify test data setup in test class methods

## Deployment

### Local Docker Build
```bash
mvn clean package
docker build -t grab-api:local .
docker run -p 8080:8080 -e DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/grab-api grab-api:local
```

### Kubernetes Deployment
- CI/CD pipeline builds and pushes to Docker Hub: `mannmengineer/grab-api:latest` and `mannmengineer/grab-api:<commit-sha>`
- Deployed via Bastion host; requires SSH key and Kubernetes access
- Image updated via: `kubectl set image deployment/grab-api grab-api=mannmengineer/grab-api:<sha>`

### Environment Variables (set in Kubernetes secrets)
- `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `FIREBASE_PRIVATE_KEY` (for notifications)

## Java Version and Compatibility
- **Target**: Java 25 (latest LTS-adjacent version)
- **Compiler**: Uses advanced javac plugins and module exports for ErrorProne/NullAway
- **Docker Base**: `eclipse-temurin:25-jre-alpine`
