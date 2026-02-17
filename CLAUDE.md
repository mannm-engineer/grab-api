# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
mvn clean package              # Build + run all tests (includes Spotless check)
mvn test                       # Run all tests only
mvn test -pl . -Dtest=DriverApiIntegrationTest   # Run a single test class
mvn test -pl . -Dtest=DriverApiIntegrationTest#testMethodName  # Run a single test method
mvn spotless:apply             # Auto-format code
mvn spotless:check             # Check formatting without fixing
```

Tests require Docker running (Testcontainers spins up PostgreSQL).

## Architecture

Spring Boot 4 application (Java 25) using Spring Data JDBC and PostgreSQL. Uses Java Platform Module System (`module-info.java`).

**4-layer architecture with strict boundaries:**

1. **Controller** (`controller/`) — API interfaces (`*Api`) define OpenAPI contracts; implementations (`*RestController`) handle HTTP only
2. **Service** (`service/`) — Business logic; depends on Store interfaces, never on repositories directly
3. **Store** (`service/*Store`) — Interfaces abstracting data access (e.g., `DriverStore`, `RideStore`)
4. **Repository** (`repository/`) — Database implementations of Store interfaces (`*DatabaseStore`), Spring Data JDBC repositories, and entity mappings

**Key conventions:**
- All data objects are Java **records** (DTOs, domain models, entities)
- Domain models use static factory methods: `Driver.newDriver(...)`, `Ride.newRide(...)`
- Immutable updates return new instances: `driver.withNewLocation(location)`
- DTOs have `driver()`/`ride()` methods to convert to domain; entities have `of(Domain)` and `domain()` for bidirectional conversion
- Naming: `*DTO` (controller), plain names (domain), `*Entity` (repository)
- **Only generate code that is actually used.** Do not create methods, classes, or fields that have no caller in the current scope of work. If a convention template includes code that won't be needed by the operation being implemented, skip it — add it later when it's actually needed.

## Nullability

Module-level `@NullMarked` (JSpecify) — all types non-null by default. Use `@Nullable` explicitly where needed. NullAway enforces this at compile time via ErrorProne.

## Code Style

Spotless with Palantir Java Format (Google style). Runs automatically during `validate` phase. No wildcard imports. Imports sorted with static imports first. OpenAPI description text must use simple/base verb form (e.g., "Create a new driver"), never third-person singular with "s"/"es" (e.g., ~~"Creates a new driver"~~).

## Test Patterns

- `@ApiTest` — Integration tests: full Spring context, random port, `RestTestClient`, Testcontainers PostgreSQL, auto table truncation between tests
- `@ApiUnitTest` — Controller unit tests: `@WebMvcTest` with `MockitoBean` for services
- Input validation tests use `@ParameterizedTest` with `@MethodSource`
- AAA comments (Arrange/Act/Assert) in test methods
- AssertJ for assertions

## Key Integrations

- **Firebase Cloud Messaging** for push notifications (`NotificationService`)
- **Async ride dispatch** (`@Async` on `RideDispatchService.dispatchRide()`)
- Exceptions map to RFC 7807 `ProblemDetail` via `GlobalExceptionHandler`

---

## Convention: Create Resource Operation

When asked to implement a "create" operation for a new resource, you MUST follow this convention strictly. Use Driver as the reference implementation.

### Files to create (replace `{Resource}` with the resource name, e.g., `Passenger`):

**1. Domain model** — `service/domain/{resource}/{Resource}.java`
```java
public record {Resource}(
    @Nullable String id,
    /* fields */,
    {Resource}Status status,
    @Nullable Audit audit) {
  public static {Resource} new{Resource}(/* fields without id, status, and audit */) {
    return new {Resource}(null, /* fields */, {Resource}Status.DEFAULT_STATUS, null);
  }
}
```
- Java record, `@Nullable String id`, status field with enum, `@Nullable Audit audit`
- Static factory method `new{Resource}(...)` sets `id = null`, default status, and `audit = null`
- Audit is always present — `GlobalAuditCallback` auto-populates it at the repository layer before save

**2. Status enum** — `share/enumeration/{Resource}Status.java`

**3. Store interface** — `service/{Resource}Store.java`
```java
public interface {Resource}Store {
  String create{Resource}({Resource} resource);
}
```
- Lives in `service/` package (NOT repository)
- Returns `String` id

**4. Service** — `service/{Resource}Service.java`
```java
@Service
public class {Resource}Service {
  private final {Resource}Store {resource}Store;
  // Constructor injection, no @Autowired
  public String create{Resource}({Resource} resource) {
    return {resource}Store.create{Resource}(resource);
  }
}
```

**5. Entity** — `repository/entity/{Resource}Entity.java`
```java
@Table("{resource}")
public record {Resource}Entity(
    @Id @Column("id") @Nullable Long id,
    /* columns */,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) @Nullable AuditEntity audit)
    implements AuditableEntity {

  @Override
  public AuditableEntity withAudit(AuditEntity audit) {
    return new {Resource}Entity(id, /* fields */, audit);
  }

  public static {Resource}Entity of({Resource} domain) { /* domain -> entity */ }
  public {Resource} {resource}() { /* entity -> domain, use Objects.requireNonNull(id) */ }
}
```
- MUST have both `of()` and `{resource}()` methods for bidirectional conversion
- MUST implement `AuditableEntity` with `withAudit()` to enable auto-audit via `GlobalAuditCallback`
- `@Nullable Long id` for auto-generated IDs
- `@Nullable AuditEntity audit` embedded with `USE_NULL`

**6. Repository** — `repository/{Resource}Repository.java`
```java
public interface {Resource}Repository extends Repository<{Resource}Entity, Long> {
  {Resource}Entity save({Resource}Entity entity);
}
```
- Extend `Repository` (not `CrudRepository`), declare only methods you need

**7. DatabaseStore** — `repository/{Resource}DatabaseStore.java`
```java
@Component
public class {Resource}DatabaseStore implements {Resource}Store {
  private final {Resource}Repository repository;
  // Constructor injection
  @Override
  public String create{Resource}({Resource} resource) {
    var created = repository.save({Resource}Entity.of(resource));
    return Objects.requireNonNull(created.id()).toString();
  }
}
```

**8. CreateDTO** — `controller/dto/{Resource}CreateDTO.java`
```java
@Schema(description = "Request payload to create a new {resource}")
public record {Resource}CreateDTO(
    @Schema(description = "...") @NotBlank String field1,
    /* more validated fields */) {
  public {Resource} {resource}() {
    return {Resource}.new{Resource}(field1, ...);
  }
}
```
- Validation annotations: `@NotBlank` for strings, `@NotNull` for objects
- Conversion method named after the domain model (lowercase): `driver()`, `ride()`

**9. ResponseDTO** — `controller/dto/{Resource}DTO.java`
```java
@Schema(description = "{Resource} response DTO")
public record {Resource}DTO(@Schema(description = "...") String id) {
  public static {Resource}DTO of(String id) { return new {Resource}DTO(id); }
}
```

**10. API interface** — `controller/{Resource}Api.java`
```java
@Tag(name = "{Resource}s", description = "{Resource} management APIs")
public interface {Resource}Api {
  @Operation(summary = "Create a {resource}", description = "Creates a new {resource} and returns its ID")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "{Resource} created successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = {Resource}DTO.class)))
  })
  {Resource}DTO create{Resource}(
      @Schema(description = "{Resource} creation payload") @Valid {Resource}CreateDTO {resource}CreateDTO);
}
```

**11. RestController** — `controller/{Resource}RestController.java`
```java
@RestController
@RequestMapping("/{resource}s")
public class {Resource}RestController implements {Resource}Api {
  private static final Logger LOGGER = LoggerFactory.getLogger({Resource}RestController.class);
  private final {Resource}Service {resource}Service;
  // Constructor injection
  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public {Resource}DTO create{Resource}(@RequestBody {Resource}CreateDTO {resource}CreateDTO) {
    LOGGER.info("Receive request to create {resource}");
    var id = {resource}Service.create{Resource}({resource}CreateDTO.{resource}());
    LOGGER.info("{Resource} created with id={}", id);

    return {Resource}DTO.of(id);
  }
}
```

**12. Schema** — Add `DROP TABLE IF EXISTS` + `CREATE TABLE` to `src/main/resources/schema.sql`
- Use `BIGSERIAL PRIMARY KEY` for id
- Column naming: `snake_case`
- MUST include audit columns: `created_at TIMESTAMP NOT NULL`, `created_by VARCHAR(100) NOT NULL`, `updated_at TIMESTAMP`, `updated_by VARCHAR(100)`

**13. module-info.java** — Add any new `requires` if needed

### Tests to create:

**14. Input validation test** — `test/.../controller/{resource}/{Resource}ApiInputValidationTest.java`
```java
@ApiUnitTest(controllers = {Resource}RestController.class)
@MockitoBean(types = {Resource}Service.class)
class {Resource}ApiInputValidationTest {
  @Autowired private MockMvc mockMvc;

  static Stream<Arguments> create{Resource}_notBlankFieldScenarios() {
    return Stream.of(
        Arguments.of("missing", """{}"""),
        Arguments.of("null", """{ "field1": null }"""),
        Arguments.of("empty string", """{ "field1": "" }"""));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("create{Resource}_notBlankFieldScenarios")
  void create{Resource}_missingRequiredField_responseBadRequest(String scenario, String requestBody)
      throws Exception {
    mockMvc.perform(post("/{resource}s")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request content."))
        .andExpect(jsonPath("$.fieldErrors", hasItem("field1: must not be blank")));
  }
}
```

**15. Integration test** — `test/.../controller/{resource}/{Resource}ApiIntegrationTest.java`
```java
@ApiTest
class {Resource}ApiIntegrationTest {
  private static final BasicJsonTester JSON_TESTER = new BasicJsonTester({Resource}ApiIntegrationTest.class);
  @Autowired private RestTestClient restTestClient;
  @Autowired private JdbcClient jdbcClient;

  @Test
  void create{Resource}_validRequest_responseCreated() {
    // ARRANGE — verify resource does not exist via jdbcClient SELECT, assertThat(...).isEmpty()

    // ACT — POST /{resource}s with valid JSON body via restTestClient
    var responseSpec = restTestClient
        .post().uri("/{resource}s")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(/* JSON string */)
        .exchange();

    // ASSERT
    // 1. Response: wrap in @spotless:off / @spotless:on
    responseSpec
      .expectStatus().isCreated()
      .expectBody(String.class)
      .value(body ->
        assertThat(JSON_TESTER.from(body))
          .isStrictlyEqualToJson(/* expected JSON */));

    // 2. Verify database row via jdbcClient SQL query (.query().singleRow())
    //    - Assert created_at isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS))
    //    - Use usingRecursiveComparison().ignoringFields("created_at")
    //      .isEqualTo(new HashMap<String, Object>() {{ put("col", value); ... }})
  }
}
```

### Naming rules:
- Test method: `create{Resource}_condition_expectedResult`
- MethodSource: `create{Resource}_notBlankFieldScenarios`
- Endpoint: `POST /{resource}s` (plural, lowercase)
- Package for tests: `controller/{resource}/`

---

## Saved Prompts

### Sync create resource convention with baseline

> Look at the current baseline implementation of the create resource operation across all layers (use Driver as the reference). Compare it with the "Convention: Create Resource Operation" section in CLAUDE.md. Update the convention to reflect the current state of the code. Do not add anything that is not in the actual code. Do not remove rules that still hold true.
