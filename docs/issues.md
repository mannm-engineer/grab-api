# Issues

Analysis of bugs, technical debt, and redundant code in the grab-api project, organized by the commit that introduced each issue.

---

## test: set up API testing

### Test Source

#### Technical Debt

##### 1. `ApiTestExtension` raw SQL truncation is fragile

**File:** `src/test/java/com/grab/api/integration/ApiTestExtension.java:44-74`

The extension uses a dynamically constructed SQL statement to truncate all tables in the `public` schema after each test:

```sql
SELECT 'TRUNCATE TABLE ' || string_agg(format('%I.%I', schemaname, tablename), ', ')
    || ' RESTART IDENTITY CASCADE;'
FROM pg_tables WHERE schemaname = 'public';
```

Issues with this approach:

- **PostgreSQL-specific** — queries `pg_tables`, a PostgreSQL system catalog. If the project ever needs to support another database (e.g., H2 for faster tests), this will break.
- **Separate connection** — creates its own `SingleConnectionDataSource` outside the Spring application context. If connection properties change (e.g., SSL settings, connection pool configuration), this connection won't reflect those changes.
- **Truncates everything** — including tables the test didn't touch, which could cause unexpected side effects if new tables with triggers or foreign keys are added.
- **No error context** — if the truncation fails (e.g., due to a lock or permission issue), the error will be a raw SQL exception with no indication that it came from test cleanup.

---

## feat(create-driver): introduce

### Main Source

#### Bugs

##### 2. `schema.sql` drops table on every startup

**File:** `src/main/resources/schema.sql:4`

The schema file contains `DROP TABLE IF EXISTS driver` and `application.yaml` sets `spring.sql.init.mode: always`. This means every time the application starts, the `driver` table is dropped and recreated, destroying all existing data.

In development this may go unnoticed because the database is often empty, but in staging or production this would silently wipe all driver records on every deployment or restart.

---

##### 3. `Driver.id` type mismatch with database

**Files:** `src/main/java/com/grab/api/service/domain/driver/Driver.java:9`, `src/main/java/com/grab/api/repository/entity/DriverEntity.java:27`

The domain record `Driver` declares `id` as `@Nullable String`, but the database column `id` is `BIGSERIAL` (a PostgreSQL auto-incrementing `Long`). This creates a type mismatch that requires conversion at the repository boundary:

- `DriverEntity.of(Driver)` converts `String → Long` via `Long.valueOf()`, which throws `NumberFormatException` if the string is not a valid number (e.g., `"abc"`, `""`).
- `DriverStoreAdapter.createDriver()` converts `Long → String` via `toString()`.

There is no validation anywhere to ensure the string id is actually numeric. If any code path sets a non-numeric id on a `Driver` record, it will fail at runtime deep in the repository layer with an unclear error.

---

##### 4. Raw `NullPointerException` from `Objects.requireNonNull`

**File:** `src/main/java/com/grab/api/repository/adapter/DriverStoreAdapter.java:50`

```java
return Objects.requireNonNull(created.id()).toString();
```

After `driverRepository.save()`, the code assumes the returned entity always has a non-null `id`. If for any reason the id is null (e.g., database trigger issue, Spring Data JDBC configuration problem), this throws a bare `NullPointerException` with no message. The exception propagates up as a 500 Internal Server Error with no context about what went wrong.

---

#### Technical Debt

##### 5. No database migration tool (Flyway/Liquibase)

**File:** `src/main/resources/schema.sql`

The project uses `spring.sql.init` with a raw `schema.sql` file for database initialization. This approach has several limitations:

- No version tracking — there is no record of which schema changes have been applied.
- No incremental migrations — every change requires modifying the single `schema.sql` file.
- No rollback support — if a schema change breaks something, there is no automated way to revert.
- The `DROP TABLE` + `CREATE TABLE` pattern (Bug #2) is a symptom of this — without migrations, there is no clean way to evolve the schema.

---

##### 6. `DriverService` is a pure pass-through

**File:** `src/main/java/com/grab/api/service/DriverService.java:16-18`

```java
public String createDriver(Driver driver) {
    return driverStore.createDriver(driver);
}
```

The service method does nothing beyond forwarding the call to `DriverStore`. There is no business logic, no validation, no transaction management, no event publishing — the controller could call `DriverStore` directly and the behavior would be identical.

This is not necessarily a bug, but it is a code smell. The service layer exists to encapsulate business logic. An empty service suggests either:

- Business logic is missing that should be here (e.g., duplicate phone number checks at the domain level, audit logging, notifications).
- The service layer was added prematurely and adds indirection without value.

---

##### 7. `DriverStatus` enum has only one value

**File:** `src/main/java/com/grab/api/share/enumeration/DriverStatus.java:4`

```java
public enum DriverStatus {
    AVAILABLE
}
```

An enum with a single value provides no discriminating power. The `status` field on `Driver` and `DriverEntity` is always `AVAILABLE` (hardcoded in `Driver.newDriver()`), and there is no code path that sets it to anything else.

This suggests either planned future statuses that have not been implemented yet (e.g., `BUSY`, `OFFLINE`, `SUSPENDED`), or premature design. In either case, the database stores a `VARCHAR(50)` status column that is always `"AVAILABLE"`, wasting storage and query complexity for no current benefit.

---

##### 8. `application.yaml` environment variables have no defaults

**File:** `src/main/resources/application.yaml:2-4`

```yaml
datasource:
  url: ${DATASOURCE_URL}
  username: ${DATASOURCE_USERNAME}
  password: ${DATASOURCE_PASSWORD}
```

These environment variables have no fallback values. If a developer clones the repo and runs the application without setting them, Spring Boot fails at startup with:

```
Could not resolve placeholder 'DATASOURCE_URL' in value "${DATASOURCE_URL}"
```

There is no `application-local.yaml`, no `.env.example`, and no documentation explaining which env vars are required or what values to use for local development.

---

#### Redundant / Unnecessary Code

##### 9. All `@Column` annotations on `DriverEntity` are redundant

**File:** `src/main/java/com/grab/api/repository/entity/DriverEntity.java:15-23`

```java
@Column("full_name") String fullName,
@Column("mobile_phone") String mobilePhone,
@Column("is_verified") Boolean isVerified,
// ... etc
```

Spring Data JDBC's default `NamingStrategy` in automatically converts `camelCase` Java field names to `snake_case` SQL column names. So `fullName` already maps to `full_name`, `mobilePhone` to `mobile_phone`, and so on. Every `@Column` annotation here is redundant — removing them all would produce identical behavior.

---

##### 10. `DriverDTO.of()` static factory method

**File:** `src/main/java/com/grab/api/controller/dto/DriverDTO.java:10-12`

```java
public static DriverDTO of(String id) {
    return new DriverDTO(id);
}
```

This factory method wraps the canonical record constructor with no additional logic. For a single-field record, `new DriverDTO(id)` is equally readable and more idiomatic. The `of()` method adds a layer of indirection without providing any value such as validation, caching, or alternative construction paths.

---

##### 11. `DriverEntity.of()` static factory method

**File:** `src/main/java/com/grab/api/repository/entity/DriverEntity.java:25-36`

This factory method exists primarily to perform the `String → Long` id conversion. If Bug #3 is fixed (making `Driver.id` a `Long`), this method becomes a trivial field-by-field copy with no added logic, making it redundant.

---

##### 12. `Driver.newDriver()` static factory method

**File:** `src/main/java/com/grab/api/service/domain/driver/Driver.java:19-37`

```java
public static Driver newDriver(...) {
    return new Driver(null, fullName, mobilePhone, DriverStatus.AVAILABLE, ...);
}
```

This method hardcodes `id = null` and `status = DriverStatus.AVAILABLE`. With only one status value (see #7) and id always null for new drivers, this is a thin wrapper. However, it does encode domain knowledge ("new drivers start with null id and AVAILABLE status"), so it has slightly more justification than the other factory methods.

---

##### 13. Boxed primitives where primitives suffice

**Files:** `src/main/java/com/grab/api/service/domain/driver/Driver.java`, `src/main/java/com/grab/api/repository/entity/DriverEntity.java`, `src/main/java/com/grab/api/controller/dto/DriverCreateDTO.java`

All three records use `Integer age`, `Double rating`, `Boolean isVerified` instead of `int`, `double`, `boolean`. These fields are:

- Annotated `@NotNull` at the DTO level (so null input is rejected before it reaches the domain).
- Defined as `NOT NULL` in the database schema (so null values cannot be persisted or retrieved).

Since nullability is impossible at every boundary, using boxed types has no benefit and introduces:

- Unnecessary autoboxing/unboxing overhead.
- False suggestion that these values could be null, confusing readers and static analysis.
- Risk of `NullPointerException` if code incorrectly assumes the boxed type might be null and adds unnecessary null checks.

---

### Test Source

#### Technical Debt

##### 14. No unit tests for `DriverService`

**File:** `src/main/java/com/grab/api/service/DriverService.java`

The service layer has no corresponding test class. Currently `DriverService.createDriver()` is a pass-through (see #6), so there is nothing meaningful to test. However, this creates a gap:

- If business logic is added to the service layer, there is no test scaffolding in place and no reminder to add tests.
- The integration tests (`DriverApiIntegrationTest`) exercise the service indirectly, but they test the full stack — they cannot isolate service-layer bugs from controller or repository issues.

---

## feat(create-driver): accept driver documents as multipart form data

### Main Source

#### Technical Debt

##### 15. File storage uses local filesystem with no cleanup on failure

**File:** `src/main/java/com/grab/api/service/DriverService.java:49-62`

The `createDriver` method stores uploaded files to the local filesystem (`./uploads`). If the database insert fails after files are already written, the catch block attempts cleanup by deleting each file individually. However:

- If the application crashes between writing files and the database insert, orphan files remain on disk permanently.
- The cleanup loop silently swallows exceptions (`LOGGER.warn`) — if deletion fails, the orphan files are never retried.
- Local filesystem storage doesn't work in multi-instance deployments (e.g., Kubernetes with multiple replicas) — each pod has its own `./uploads` directory.

---

## feat(create-driver): introduce audit for created tracking

No issues found.

---

## feat(create-driver): publish driver created event to Kafka

### Main Source

#### Technical Debt

##### 16. `OutboxEventPublishScheduler` bypasses the store abstraction

**File:** `src/main/java/com/grab/api/scheduler/OutboxEventPublishScheduler.java:15`

The scheduler directly depends on `OutboxEventRepository` (a repository-layer class) instead of going through `OutboxEventStore` (the store interface). This breaks the layered architecture pattern used everywhere else in the project, where services and schedulers depend on store interfaces, not repositories.

---

##### 17. `DomainEventType` enum has only one value

**File:** `src/main/java/com/grab/api/share/enumeration/DomainEventType.java:4`

```java
public enum DomainEventType {
    CREATED
}
```

Same pattern as `DriverStatus` (issue #7). A single-value enum provides no discriminating power. If more event types are planned (e.g., `UPDATED`, `DELETED`), they are missing.

---

##### 18. `OutboxEventEntity.of()` uses `Instant.now()` inside entity construction

**File:** `src/main/java/com/grab/api/repository/entity/OutboxEventEntity.java:19-21`

```java
public static OutboxEventEntity of(String topic, String eventKey, String eventType, String payload) {
    return new OutboxEventEntity(null, topic, eventKey, eventType, payload, Instant.now());
}
```

The `created_at` timestamp is set at entity construction time rather than being passed in from the domain layer or set by the database. In tests or replays, this makes the timestamp non-deterministic and untestable without time-based assertions with tolerances.

---

#### Redundant / Unnecessary Code

##### 19. All `@Column` annotations on `OutboxEventEntity` are redundant

**File:** `src/main/java/com/grab/api/repository/entity/OutboxEventEntity.java:11-16`

Same issue as #9. Spring Data JDBC auto-maps `camelCase` to `snake_case`. Every `@Column` annotation here (`@Column("topic")` for `topic`, `@Column("event_key")` for `eventKey`, etc.) is redundant.

---

##### 20. All `@Column` annotations on `AuditEntity` are redundant

**File:** `src/main/java/com/grab/api/repository/entity/AuditEntity.java:8-9`

Same as #9 and #19. `createdAt` maps to `created_at` and `createdBy` maps to `created_by` automatically.

---

## feat(download-driver-document-file): introduce

### Main Source

#### Bugs

##### 43. `DomainNotFoundException` does not pass a message to `super()`

**File:** `src/main/java/com/grab/api/service/exception/DomainNotFoundException.java:7-8`

```java
public DomainNotFoundException(String domainId) {
    this.domainId = domainId;
}
```

The constructor stores `domainId` but never calls `super(message)`. This means `getMessage()` returns `null`. While the `GlobalExceptionHandler` builds its own response message using `getDomainId()`, any code that logs the exception via standard patterns (e.g., `LOGGER.error("Failed", e)`) will see `null` as the exception message, making debugging harder.

---

##### 44. Non-numeric `fileId` path variable causes unhandled `NumberFormatException`

**File:** `src/main/java/com/grab/api/repository/adapter/DriverStoreAdapter.java:55`

```java
return driverDocumentFileRepository
    .findFileUrlById(Long.parseLong(id))
    .map(DriverDocumentFileEntity::fileUrl);
```

The `fileId` path variable arrives as a `String` from the controller (`GET /drivers/document-files/{fileId}`). If a non-numeric value is provided (e.g., `/drivers/document-files/abc`), `Long.parseLong(id)` throws `NumberFormatException`. There is no handler for `NumberFormatException` in `GlobalExceptionHandler`, so this propagates as a 500 Internal Server Error instead of 400 or 404.

---

#### Technical Debt

##### 45. `FileStoreAdapter` wraps `IOException` in bare `RuntimeException`

**File:** `src/main/java/com/grab/api/repository/adapter/FileStoreAdapter.java:22-27`

```java
public InputStream getFile(String url) {
    try {
      return Files.newInputStream(Path.of(url));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + url, e);
    }
}
```

All three methods (`getFile`, `createFile`, `deleteFile`) wrap `IOException` in an untyped `RuntimeException`. The project defines domain-specific exceptions (`DomainNotFoundException`, `InvalidInputException`), but file I/O errors use bare `RuntimeException`. This makes it impossible to handle file errors distinctly at the controller layer — they all surface as 500 errors with no specific exception type to catch.

---

## feat(update-driver-location): introduce

### Main Source

#### Bugs

##### 21. `Location` domain model imports repository entity — architecture violation (Fixed)

**File:** `src/main/java/com/grab/api/service/domain/Location.java:3`

```java
import com.grab.api.repository.entity.LocationEntity;
```

The `Location` domain record (in the `service/domain` package) directly imports and depends on `LocationEntity` (in the `repository/entity` package). This violates the layered architecture where domain models should have no knowledge of the persistence layer. The `entity()` and `of()` methods on `Location` create a bidirectional dependency between domain and repository layers.

The conversion logic belongs in `LocationEntity` or in the `DatabaseStore` classes, not in the domain model.

---

##### 46. Non-numeric `id` path variable causes unhandled `NumberFormatException`

**File:** `src/main/java/com/grab/api/repository/adapter/DriverStoreAdapter.java:44`

```java
public Optional<Driver> getDriver(String id) {
    return driverRepository.findById(Long.valueOf(id)).map(DriverEntity::driver);
}
```

Same pattern as issue #44. When `PUT /drivers/{id}/location` is called with a non-numeric `id` (e.g., `/drivers/abc/location`), `Long.valueOf(id)` throws `NumberFormatException` which is unhandled, resulting in a 500 error.

---

##### 22. `DriverEntity.driver()` uses `Objects.requireNonNull(audit)` without message

**File:** `src/main/java/com/grab/api/repository/entity/DriverEntity.java:85`

```java
Objects.requireNonNull(audit).audit()
```

Same issue as #4. If `audit` is null (e.g., when reading a driver record that was inserted before audit columns were added), this throws a bare `NullPointerException` with no message.

---

##### 23. `DriverEntity.driver()` uses `Objects.requireNonNull(id)` without message

**File:** `src/main/java/com/grab/api/repository/entity/DriverEntity.java:74`

```java
Objects.requireNonNull(id).toString()
```

Same issue as #4. Bare `NullPointerException` with no context if `id` is null.

---

#### Technical Debt

##### 24. `DriverLocationUpdateDTO` uses boxed `Double` for lat/lng

**File:** `src/main/java/com/grab/api/controller/dto/DriverLocationUpdateDTO.java:7`

`@NotNull Double lat, @NotNull Double lng` — these are annotated `@NotNull`, so null is rejected at validation. Using `double` primitives would be more appropriate. Same pattern as issue #13.

---

#### Redundant / Unnecessary Code

##### 25. All `@Column` annotations on `LocationEntity` are redundant

**File:** `src/main/java/com/grab/api/repository/entity/LocationEntity.java:7`

Same as #9. `lat` maps to `lat` and `lng` maps to `lng` automatically.

---

## feat(update-driver-location): introduce audit for updated tracking

No issues found.

---

## feat(create-ride): introduce

### Main Source

#### Bugs

##### 47. Missing `@Valid` on `RideCreateDTO` nested `LocationDTO` fields

**File:** `src/main/java/com/grab/api/controller/dto/RideCreateDTO.java:17-21`

```java
@Schema(description = "Pickup location of the ride") @NotNull
LocationDTO pickupLocation,

@Schema(description = "Dropoff location of the ride") @NotNull
LocationDTO dropoffLocation
```

The `pickupLocation` and `dropoffLocation` fields are annotated `@NotNull` but not `@Valid`. Without `@Valid`, Bean Validation does not cascade into the `LocationDTO` record, so the `@NotNull` constraints on `LocationDTO.lat` and `LocationDTO.lng` are never checked. A request like:

```json
{
  "passengerId": "550e8400-e29b-41d4-a716-446655440000",
  "pickupLocation": { "lat": null, "lng": null },
  "dropoffLocation": { "lat": null, "lng": null }
}
```

passes validation (the `LocationDTO` references are non-null) but produces `Location(null, null)`, which will throw `NullPointerException` when `distanceTo()` is called during ride dispatch.

---

##### 48. `RideEntity.of()` calls `UUID.fromString()` without format validation

**File:** `src/main/java/com/grab/api/repository/entity/RideEntity.java:30`

```java
UUID.fromString(ride.passengerId()),
```

The `passengerId` field on `RideCreateDTO` is validated with `@NotBlank` but not `@Pattern` for UUID format. Any non-blank, non-UUID string (e.g., `"not-a-uuid"`) passes controller validation but throws `IllegalArgumentException` at the repository layer when `UUID.fromString()` is called. There is no handler for `IllegalArgumentException` in `GlobalExceptionHandler`, so this returns a 500 error.

---

#### Technical Debt

##### 26. `RideService` is a pure pass-through

**File:** `src/main/java/com/grab/api/service/RideService.java:17-18`

```java
public void createRide(Ride ride) {
    rideStore.createRide(ride);
}
```

Same pattern as issue #6. The service delegates directly to the store with no business logic.

---

##### 27. `RideStatus` enum has only one value

**File:** `src/main/java/com/grab/api/share/enumeration/RideStatus.java:4`

```java
public enum RideStatus {
    REQUESTED
}
```

Same pattern as issues #7 and #17.

---

##### 28. `Ride.id` type mismatch with database

**File:** `src/main/java/com/grab/api/service/domain/ride/Ride.java:8`, `src/main/java/com/grab/api/repository/entity/RideEntity.java:29`

Same pattern as issue #3. `Ride.id` is `@Nullable String` but the database column is `BIGSERIAL` (`Long`). `RideEntity.of()` converts `String → Long` via `Long.valueOf()` with no validation.

---

#### Redundant / Unnecessary Code

##### 29. All `@Column` annotations on `RideEntity` are redundant

**File:** `src/main/java/com/grab/api/repository/entity/RideEntity.java:15-24`

Same as #9. `passengerId` maps to `passenger_id` automatically, etc.

---

## feat: introduce notification service using WebSocket

### Main Source

#### Bugs

##### 30. WebSocket endpoint allows all origins

**File:** `src/main/java/com/grab/api/config/WebSocketConfig.java:22`

```java
registry.addEndpoint("/ws").setAllowedOrigins("*");
```

`setAllowedOrigins("*")` permits any origin to open WebSocket connections. In production, this allows cross-site WebSocket hijacking — a malicious page can open a connection to the server using the victim's cookies/session, potentially receiving private notifications meant for the authenticated user.

---

#### Technical Debt

##### 49. WebSocket notifications sent to public topics without subscription authorization

**File:** `src/main/java/com/grab/api/service/NotificationService.java:20`

```java
var destination = "/topic/user/" + notification.recipientId();
messagingTemplate.convertAndSend(destination, ...);
```

Notifications are sent to topic destinations (`/topic/user/{driverId}`). In STOMP, any client can subscribe to any topic. Combined with the wildcard origin policy (issue #30), any WebSocket client can subscribe to `/topic/user/123` and receive ride dispatch notifications intended for driver 123. There is no `ChannelInterceptor` or `WebSocketMessageBrokerConfigurer.configureClientInboundChannel()` implementation to validate that subscribers are authorized to receive messages for a given user.

---

## feat: introduce ride dispatch service

### Main Source

#### Bugs

##### 31. Driver search loads all drivers into memory and filters in Java

**File:** `src/main/java/com/grab/api/repository/adapter/DriverStoreAdapter.java:29-39`

```java
public List<Driver> find(DriverSearchCriteria criteria) {
    var stream = driverRepository.findAll().stream();
    if (criteria.status() != null) {
        stream = stream.filter(entity -> entity.status() == criteria.status());
    }
    if (criteria.hasLocation()) {
        stream = stream.filter(entity -> entity.location() != null);
    }
    return stream.map(DriverEntity::driver).toList();
}
```

`findAll()` loads every driver record from the database into memory, then filters in Java. With even a moderate number of drivers (thousands), this causes:

- Excessive memory consumption — every driver row (with all columns and embedded documents) is loaded.
- Unnecessary database I/O — all rows are transferred over the network.
- Poor query performance — the database's indexing and query optimizer are bypassed entirely.

The filtering should be done in a SQL query with `WHERE` clauses.

---

##### 32. `Location.distanceTo()` uses Euclidean distance instead of geographic distance

**File:** `src/main/java/com/grab/api/service/domain/Location.java:15-17`

```java
public double distanceTo(Location other) {
    return Math.sqrt(Math.pow(other.lat - lat, 2) + Math.pow(other.lng - lng, 2));
}
```

This calculates Euclidean distance on latitude/longitude coordinates, which is incorrect for geographic distances. Latitude and longitude are not on a flat plane — one degree of longitude varies in distance depending on the latitude. At the equator, 1° longitude ≈ 111 km, but near the poles it approaches 0 km. This means the nearest driver calculation could return a driver who is geographically farther away than another candidate.

For a ride-sharing application, this should use the Haversine formula or similar geodesic calculation.

---

##### 50. `RideService.createRide()` dispatches ride with null id (Fixed)

**File:** `src/main/java/com/grab/api/service/RideService.java:17-18` (at this commit)

```java
public void createRide(Ride ride) {
    rideStore.createRide(ride);
    rideDispatchService.dispatchRide(ride);
}
```

The `ride` object passed to `dispatchRide()` is the original object with `id = null` (since `createRide()` does not return the generated id — see issue #40). The dispatch method logs `ride.id()` as `null`, and the notification payload does not reference the ride. In a later commit, dispatch was moved to a database polling scheduler that reads rides from the database (with their generated IDs), making this no longer reachable.

---

##### 33. `RideDispatchService.dispatchRide()` throws `DomainNotFoundException` inside `@Async` method

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java:48`

```java
var driver = driverService
    .findNearestDriver(ride.pickupLocation())
    .orElseThrow(() -> new DomainNotFoundException("Driver not found"));
```

Since `dispatchRide` is annotated `@Async`, exceptions thrown here are not propagated to the caller. The `DomainNotFoundException` is silently swallowed by Spring's async exception handler (which defaults to logging at ERROR level). The ride remains in `REQUESTED` status with no retry mechanism, no status update to indicate failure, and no notification to the passenger.

---

##### 34. `RideDispatchService.sendRide()` uses unchecked `ObjectMapper.writeValueAsString()`

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java:76`

```java
var jsonPayload = objectMapper.writeValueAsString(payload);
```

The Jackson `ObjectMapper.writeValueAsString()` can throw `JsonProcessingException` (a checked exception). If this is using the `tools.jackson.databind.ObjectMapper` import (which it is), this could behave differently than expected. If serialization fails, the ride may not be dispatched and the exception may be silently swallowed in the async context (see #33).

---

#### Technical Debt

##### 35. No unit tests for `RideDispatchService`

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java`

`RideDispatchService` contains the most complex business logic in the project (find nearest driver, send notification, handle failures) but has no unit tests. The async behavior makes it particularly important to have isolated unit tests that verify the dispatch logic, error handling, and edge cases (no available drivers, serialization failures, etc.).

---

## ci: create continuous integration pipeline

### Main Source

#### Bugs

##### 36. CI pipeline exposes secrets in the deploy script

**File:** `.github/workflows/ci.yaml:97-101`

```yaml
script: |
  kubectl create secret generic grab-api-secrets \
    --from-literal=DATASOURCE_USERNAME='${{ secrets.DATASOURCE_USERNAME }}' \
    --from-literal=DATASOURCE_PASSWORD='${{ secrets.DATASOURCE_PASSWORD }}' \
    --from-literal=FIREBASE_PRIVATE_KEY='${{ secrets.FIREBASE_PRIVATE_KEY }}' \
```

GitHub Actions secrets are interpolated into the shell script before it runs on the bastion host. If the SSH action logs the script or if the bastion host has command logging enabled, the secret values are exposed in plaintext in logs.

---

##### 37. Kubernetes manifests reference `FIREBASE_PRIVATE_KEY` but no Firebase code exists

**Files:** `.github/workflows/ci.yaml:100`, `k8s.yaml:157-160`

The CI pipeline creates a Kubernetes secret with `FIREBASE_PRIVATE_KEY`, and the deployment mounts it as an env var. However, there is no Firebase dependency in `pom.xml` and no code that uses this key. This is either a leftover from a removed feature or premature configuration for a feature not yet implemented.

---

#### Technical Debt

##### 51. Dockerfile `ENTRYPOINT` runs Java as child of shell — signals not forwarded

**File:** `Dockerfile:15`

```dockerfile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

The `sh -c` wrapper means the shell process is PID 1, not the Java process. On Alpine (busybox `sh`), PID 1 does not forward signals like SIGTERM to child processes. When Kubernetes sends SIGTERM during pod termination, the Java process never receives it, preventing graceful shutdown (Spring's `@PreDestroy`, connection draining, etc.). The pod is killed by SIGKILL after the termination grace period expires.

---

##### 52. PostgreSQL version mismatch between production and test

**Files:** `k8s.yaml:12`, `src/test/java/com/grab/api/integration/TestDataSourceConfig.java:14`

The Kubernetes deployment uses `postgres:15`:

```yaml
image: postgres:15
```

While the Testcontainers configuration uses `postgres:16-alpine`:

```java
new PostgreSQLContainer<>("postgres:16-alpine")
```

Tests run against PostgreSQL 16 but production runs PostgreSQL 15. Behavioral differences between major versions (e.g., query planner changes, deprecated features, new defaults) could cause tests to pass while production fails or behaves differently.

---

##### 38. No persistent storage for PostgreSQL in Kubernetes

**File:** `k8s.yaml:1-35`

The PostgreSQL deployment uses no `PersistentVolumeClaim`. Container storage is ephemeral — if the PostgreSQL pod is restarted, rescheduled, or evicted, all data is lost. Combined with `schema.sql` dropping tables on startup (issue #2), every pod restart results in a completely empty database.

---

##### 39. No persistent storage for uploaded driver documents in Kubernetes

**File:** `k8s.yaml:117-145`

The grab-api deployment writes driver documents to `./uploads` (local filesystem inside the container). Like the PostgreSQL issue (#38), container restarts lose all uploaded files. There is no `PersistentVolumeClaim` or external storage (S3, GCS) configured.

---

## refactor: replace async ride dispatch with database polling scheduler

### Main Source

#### Bugs

##### 40. `RideStoreAdapter.createRide()` saves but discards the returned entity

**File:** `src/main/java/com/grab/api/repository/adapter/RideStoreAdapter.java:22-25`

```java
public void createRide(Ride ride) {
    var created = rideRepository.save(RideEntity.of(ride));
    Objects.requireNonNull(created.id());
}
```

The `save()` return value is captured and the id is null-checked, but the id is never returned to the caller. The `createRide` method returns `void`, so the generated id is thrown away. This means the caller (and the ride's domain object) never gets its id, making it impossible to reference the created ride afterwards without a separate query.

---

##### 41. `RideDispatchService.dispatchPendingRides()` has no concurrency protection

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java:32-42`

```java
public void dispatchPendingRides() {
    var pendingRides = rideStore.findByStatus(RideStatus.REQUESTED);
    for (var ride : pendingRides) {
        try {
            dispatchRide(ride);
        } catch (Exception e) {
            LOGGER.error("Failed to dispatch ride id={}, will retry next poll", ride.id(), e);
        }
    }
}
```

Unlike the `OutboxEventPublishScheduler` which uses `SELECT ... FOR UPDATE SKIP LOCKED` for multi-instance safety, the ride dispatch scheduler uses a plain `findByStatus()` query. If multiple application instances run the scheduler concurrently, the same ride could be dispatched to multiple drivers simultaneously — each instance reads the same `REQUESTED` rides and processes them independently before any of them update the status to `DISPATCHED`.

---

##### 53. Ride dispatch sends notification before updating status — duplicate notification risk

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java:70-71`

```java
sendRide(driver, ride);
rideStore.updateRide(ride.withStatus(RideStatus.DISPATCHED));
```

The notification is sent to the driver *before* the ride status is updated to `DISPATCHED`. If `updateRide()` fails (e.g., database connection error, optimistic locking conflict), the ride remains in `REQUESTED` status. On the next poll cycle, `dispatchPendingRides()` picks it up again, finds a driver, and sends another notification — potentially to a different driver. This can result in multiple drivers receiving dispatch notifications for the same ride.

---

#### Technical Debt

##### 54. No poison-pill protection for permanently failing rides

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java:37-46`

```java
public void dispatchPendingRides() {
    var pendingRides = rideStore.findByStatus(RideStatus.REQUESTED);
    for (var ride : pendingRides) {
      try {
        dispatchRide(ride);
      } catch (Exception e) {
        LOGGER.error("Failed to dispatch ride id={}, will retry next poll", ride.id(), e);
      }
    }
}
```

If a ride consistently fails dispatch (e.g., the `ObjectMapper.writeValueAsString()` call throws on a malformed payload, or a data integrity issue prevents the status update), it remains in `REQUESTED` status and is retried every 5 seconds indefinitely. There is no retry counter, no exponential backoff, no maximum retry limit, and no mechanism to move the ride to a `FAILED` or dead-letter status. A single poison-pill ride also delays processing of all subsequent rides in the same batch since processing is sequential.

---

## docs: add ISSUES.md with code review analysis

No issues found.

---

## docs: add reading list for DDD entity identity design patterns

No issues found.

---

## docs: add prompt template for referencing project patterns

No issues found.

---

## Cross-commit issues (discovered during full review)

### Main Source

#### Bugs

##### 55. `DriverService.updateLocation()` has no `@Transactional` — read-modify-write race condition

**File:** `src/main/java/com/grab/api/service/DriverService.java:113-118`

```java
public void updateLocation(String id, Location newLocation) {
    var existing = driverStore.get(id).orElseThrow(() -> new DomainNotFoundException(id));
    var updated = existing.withLocation(newLocation);
    driverStore.update(updated);
}
```

The method performs a read-then-write sequence without a `@Transactional` annotation. Two concurrent requests for the same driver could both read the same state, then both write, with the second silently overwriting the first. The `create()` method in the same class is annotated `@Transactional`, but `updateLocation()` is not.

---

##### 56. `DriverDocumentEntity.of()` always sets `id=null` — driver updates re-insert all document rows with new IDs

**File:** `src/main/java/com/grab/api/repository/entity/DriverDocumentEntity.java:25-26`

```java
public static DriverDocumentEntity of(DriverDocument document) {
    // ...
    return new DriverDocumentEntity(
        null, document.type(), document.documentNumber(), document.expiryDate(), files);
}
```

`DriverDocumentEntity.of()` hardcodes `id` to `null`. The same applies to `DriverDocumentFileEntity.of()` at line 14. The round-trip conversion path `DriverEntity → Driver → DriverEntity` (via `DriverEntity.driver()` then `DriverEntity.of()`) loses all document and file entity IDs because the domain model `DriverDocument` does not carry them. Spring Data JDBC deletes and re-inserts all child rows on aggregate update, generating new IDs each time. This means every call to `DriverStoreAdapter.update()` (e.g., location updates) invalidates all existing document file download URLs (`GET /files/{fileId}`), since the `fileId` changes on every update.

---

##### 57. `DomainNotFoundException("Driver not found")` passes human-readable message as `domainId`

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java:61`

```java
var driver = driverService
    .findNearest(ride.pickupLocation())
    .orElseThrow(() -> new DomainNotFoundException("Driver not found"));
```

`DomainNotFoundException` takes a `domainId` parameter (see issue #43), not a message. The `GlobalExceptionHandler` produces the error detail by interpolating: `"Resource with id Driver not found not found."` — a nonsensical message. The string `"Driver not found"` is treated as the resource ID.

---

##### 58. Ride dispatch does not mark driver as busy — same driver dispatched to multiple rides

**File:** `src/main/java/com/grab/api/service/RideDispatchService.java:59-71`

```java
var driver = driverService
    .findNearest(ride.pickupLocation())
    .orElseThrow(() -> new DomainNotFoundException("Driver not found"));
// ...
notifyDriver(driver, ride);
rideStore.update(ride.withStatus(RideStatus.DISPATCHED));
```

After finding the nearest `AVAILABLE` driver, the dispatch method notifies the driver and marks the ride as `DISPATCHED`, but never changes the driver's status (e.g., to `BUSY`). In the same `dispatchAllPending()` loop, if multiple rides are pending, they all find the same nearest driver because the driver remains `AVAILABLE`. Each ride is dispatched to the same driver simultaneously.

---

##### 59. `FileContentStoreAdapter.findByUrl()` has no path traversal protection

**File:** `src/main/java/com/grab/api/repository/adapter/FileContentStoreAdapter.java:22-24`

```java
public InputStream findByUrl(String url) {
    try {
      return Files.newInputStream(Path.of(url));
```

The `url` parameter is passed directly to `Path.of()` with no validation that it resides within the storage root directory. While the URL is read from the database (not directly from user input), if the database is compromised or a SQL injection is exploited, an attacker could set a `file_url` to any absolute path (e.g., `/etc/passwd`) and the file download endpoint would serve it. The `storageRoot` field exists but is not used to validate the path.

---

##### 60. `FileContentStoreAdapter.create()` does not sanitize filename for path traversal

**File:** `src/main/java/com/grab/api/repository/adapter/FileContentStoreAdapter.java:35`

```java
var storedFilename = UUID.randomUUID() + "_" + filename;
var target = dateDir.resolve(storedFilename);
```

The `filename` originates from the uploaded file's `getOriginalFilename()`. While the UUID prefix is prepended, the filename could contain `../` sequences (e.g., `../../etc/cron.d/malicious`). The `resolve()` call does not normalize or validate that the resulting path stays within `dateDir`. A crafted filename could write files outside the storage root.

---

##### 61. `InputStream` from file download may leak on error

**File:** `src/main/java/com/grab/api/controller/FileRestController.java:34-39`

```java
var content = fileService.download(id);
return ResponseEntity.ok()
    .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .body(new InputStreamResource(content));
```

`fileService.download()` opens an `InputStream` from `Files.newInputStream()`. If an error occurs after the stream is opened but before Spring finishes writing the response (e.g., client disconnects, serialization error), the `InputStreamResource` does not guarantee closure. The `InputStream` holds a file descriptor that may leak.

---

#### Technical Debt

##### 62. Kafka test container uses `latest` tag — non-reproducible test builds

**File:** `src/test/java/com/grab/api/integration/TestKafkaConfig.java:14`

```java
return new KafkaContainer("apache/kafka-native:latest");
```

The `latest` tag is mutable — tests may pass today and fail tomorrow when the tag points to a new version with breaking changes. The PostgreSQL test container pins to `postgres:16-alpine`, but the Kafka container does not pin a version.

---

##### 63. File I/O happens inside `@Transactional` in `DriverService.create()`

**File:** `src/main/java/com/grab/api/service/DriverService.java:57-73`

```java
@Transactional
public String create(DriverCreate driverCreate, List<NewFile> files) {
    // ...
    filenameToUrl.putAll(saveFiles(files));   // file I/O
    var id = driverStore.create(...);          // DB
    outboxEventStore.create(...);              // DB
```

The `saveFiles()` call performs filesystem I/O (writing uploaded files to disk) inside the `@Transactional` boundary. If file writes are slow (large files, slow disk), the database connection is held open for the entire duration. Under load, this can exhaust the connection pool. The file save should happen outside the transaction.

---

##### 64. `WebSocketMessage` inner record is `public` — leaks implementation detail

**File:** `src/main/java/com/grab/api/service/NotificationService.java:31`

```java
public record WebSocketMessage(String title, String payload) {}
```

`WebSocketMessage` is declared as a `public` nested record inside `NotificationService`. It is only used internally by `convertAndSend()`. Being `public`, it becomes part of the service's public API, exposing an implementation detail of how notifications are serialized for WebSocket delivery.

---

##### 65. `handleDuplicateKey()` returns generic message with grammar error

**File:** `src/main/java/com/grab/api/controller/exception/GlobalExceptionHandler.java:57-58`

```java
@ExceptionHandler(DuplicateKeyException.class)
public ProblemDetail handleDuplicateKey() {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Resource already exist.");
}
```

The message `"Resource already exist."` has a grammar error ("exist" should be "exists"). It also does not log the exception or include any information about which resource or constraint was violated, making debugging difficult in production.

---

##### 66. Outbox event payload for driver creation is just the driver ID string

**File:** `src/main/java/com/grab/api/service/DriverService.java:66`

```java
outboxEventStore.create(new OutboxEvent(driverEventsTopic, id, CREATED, id));
```

The event payload (`id`) is the raw driver ID string (e.g., `"1"`). Consumers of the Kafka topic receive no driver data — they must call back to the API to get driver details. This defeats the purpose of event-driven architecture, where events should carry enough data for consumers to act without synchronous callbacks.

---

##### 67. `ApiInputValidationTest` meta-annotation name ends with "Test" — misleading

**File:** `src/test/java/com/grab/api/unit/ApiInputValidationTest.java`

```java
public @interface ApiInputValidationTest {
```

The annotation name ends with `Test`, which suggests it is a test class. It is actually a meta-annotation that configures `@WebMvcTest`. Test runners and IDEs may attempt to execute it as a test class. A name like `ApiInputValidation` or `InputValidationWebMvcTest` would be clearer.

---

##### 68. `Location` domain record uses boxed `Double` instead of primitive `double`

**File:** `src/main/java/com/grab/api/service/domain/Location.java`

At the domain level, a `Location` always has non-null coordinates — it is constructed from validated DTOs and stored in non-null database columns. Using `Double` (boxed) introduces unnecessary autoboxing in `distanceTo()` calculations and falsely suggests nullability. This is distinct from issue #13 (which covers DTOs and entities) — the domain model should use `double` primitives since null coordinates are never valid for a `Location`.

---

#### Redundant / Unnecessary Code

##### 69. All `@Column` annotations on `DriverDocumentEntity` are redundant

**File:** `src/main/java/com/grab/api/repository/entity/DriverDocumentEntity.java:16-19`

```java
@Id @Column("id") @Nullable Long id,
@Column("type") DocumentType type,
@Column("document_number") String documentNumber,
@Column("expiry_date") LocalDate expiryDate,
```

Same as #9. Spring Data JDBC auto-maps `camelCase` to `snake_case`, and `type` maps to `type`, `documentNumber` to `document_number`, etc.

---

##### 70. All `@Column` annotations on `DriverDocumentFileEntity` are redundant

**File:** `src/main/java/com/grab/api/repository/entity/DriverDocumentFileEntity.java:11-12`

```java
@Id @Column("id") @Nullable Long id, @Column("file_url") String fileUrl
```

Same as #9. `id` maps to `id` and `fileUrl` maps to `file_url` automatically.

---

##### 71. `DriverDocumentEntity.of()` and `DriverDocumentFileEntity.of()` factory methods are trivial wrappers

**File:** `src/main/java/com/grab/api/repository/entity/DriverDocumentEntity.java:22`, `DriverDocumentFileEntity.java:14`

Both `of()` methods are simple constructor calls with the same arguments, adding no validation, caching, or transformation beyond what the constructor does. Same pattern as issues #10 and #11.

---

##### 72. `DriverCreate.driver()` mapping method is a trivial field-by-field copy

**File:** `src/main/java/com/grab/api/service/domain/driver/DriverCreate.java:18-28`

The `driver(Map<String, String> filenameToUrl)` method maps `DriverCreateDocument` to `DriverDocument` by copying each field and resolving filenames to URLs via `Map.get()`. However, the mapping uses `filenameToUrl::get` which silently returns `null` for missing keys (see issue #57 in the domain model section). Beyond that concern, the method delegates to `Driver.newDriver()` with a 1:1 field mapping. Same pattern as #10.

---

##### 73. `LocationDTO.location()` factory method is a trivial constructor wrapper

**File:** `src/main/java/com/grab/api/controller/dto/LocationDTO.java:15-17`

```java
public Location location() {
    return new Location(lat, lng);
}
```

Same pattern as #10. A 1:1 field mapping with no added logic.
