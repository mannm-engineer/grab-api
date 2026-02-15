package com.grab.api.controller.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.grab.api.integration.ApiTest;
import com.grab.api.share.enumeration.DriverStatus;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApiTest
class DriverApiIntegrationTest {

  private static final BasicJsonTester JSON_TESTER =
      new BasicJsonTester(DriverApiIntegrationTest.class);

  @Autowired
  private RestTestClient restTestClient;

  @Autowired
  private JdbcClient jdbcClient;

  @Test
  void createDriver_validRequest_responseCreated() {
    // ARRANGE
    var driverBefore = jdbcClient.sql("""
      SELECT *
      FROM driver
      WHERE id = 1
    """).query().optionalValue();

    assertThat(driverBefore).isEmpty();

    var outboxEventBefore = jdbcClient.sql("""
      SELECT *
      FROM outbox_event
      WHERE id = 1
    """).query().optionalValue();

    assertThat(outboxEventBefore).isEmpty();

    // ACT
    var responseSpec = restTestClient
        .post()
        .uri("/drivers")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {
              "fullName": "John Doe",
              "mobilePhone": "+6591234567"
            }
            """)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isCreated()
      .expectBody(String.class)
      .value(body ->
        assertThat(JSON_TESTER.from(body))
          .isStrictlyEqualToJson(
            // language=JSON
            """
            {
              "id": "1"
            }
            """));
    // @spotless:on

    var driverAfter = jdbcClient.sql("""
      SELECT *
      FROM driver
      WHERE id = 1
    """).query().singleRow();

    assertThat(driverAfter).satisfies(driver -> {
      var createdAt = ((Timestamp) driver.get("created_at")).toInstant();
      assertThat(createdAt).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));

      assertThat(driver)
          .usingRecursiveComparison()
          .ignoringFields("created_at")
          .isEqualTo(new HashMap<String, Object>() {
            {
              put("id", 1L);
              put("full_name", "John Doe");
              put("mobile_phone", "+6591234567");
              put("location_lat", null);
              put("location_lng", null);
              put("status", DriverStatus.AVAILABLE.name());
              put("created_by", "SYSTEM");
              put("updated_at", null);
              put("updated_by", null);
            }
          });
    });

    var outboxEventAfter = jdbcClient.sql("""
      SELECT *
      FROM outbox_event
      WHERE id = 1
    """).query().singleRow();

    assertThat(outboxEventAfter).satisfies(event -> {
      var eventCreatedAt = ((Timestamp) event.get("created_at")).toInstant();
      assertThat(eventCreatedAt).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));

      assertThat(event)
          .usingRecursiveComparison()
          .ignoringFields("created_at")
          .isEqualTo(new HashMap<String, Object>() {
            {
              put("id", 1L);
              put("topic", "driver-events");
              put("event_key", "1");
              put("event_type", "CREATED");
              put("payload", "1");
            }
          });
    });
  }

  @Test
  @Sql(statements = """
    INSERT INTO driver (full_name, mobile_phone, status, created_at, created_by)
    VALUES ('John Doe', '+6591234567', 'AVAILABLE', now(), 'SYSTEM');
  """)
  void createDriver_duplicateMobilePhone_responseConflict() {
    // ACT
    var responseSpec = restTestClient
        .post()
        .uri("/drivers")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {
              "fullName": "Jane Doe",
              "mobilePhone": "+6591234567"
            }
            """)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
      .expectBody(String.class)
      .value(body ->
        assertThat(JSON_TESTER.from(body))
          .isStrictlyEqualToJson(
            // language=JSON
            """
            {
               "detail" : "An unexpected error occurred.",
               "instance" : "/api/drivers",
               "status" : 500,
               "title" : "Internal Server Error"
             }
            """));
    // @spotless:on
  }

  @Test
  @Sql(statements = """
    INSERT INTO driver (full_name, mobile_phone, location_lat, location_lng, status, created_at, created_by, updated_at, updated_by)
    VALUES ('John Doe', '+6591234567', NULL, NULL, 'AVAILABLE', now(), 'SYSTEM', null, null);
  """)
  void updateDriverLocation_driverExists_responseNoContent() {
    // ARRANGE
    var driverBefore = jdbcClient.sql("""
      SELECT *
      FROM driver
      WHERE id = 1
    """).query().singleRow();

    assertThat(driverBefore).satisfies(driver -> {
      var createdAt = ((Timestamp) driverBefore.get("created_at")).toInstant();
      assertThat(createdAt).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));

      assertThat(driver)
          .usingRecursiveComparison()
          .ignoringFields("created_at")
          .isEqualTo(new HashMap<String, Object>() {
            {
              put("id", 1L);
              put("full_name", "John Doe");
              put("mobile_phone", "+6591234567");
              put("location_lat", null);
              put("location_lng", null);
              put("status", DriverStatus.AVAILABLE.name());
              put("created_by", "SYSTEM");
              put("updated_at", null);
              put("updated_by", null);
            }
          });
    });

    // ACT
    var responseSpec = restTestClient
        .put()
        .uri("/drivers/1/location")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {
              "lat": 10.0,
              "lng": 20.0
            }
            """)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isNoContent()
      .expectBody().isEmpty();
    // @spotless:on

    var driverAfter = jdbcClient.sql("""
      SELECT *
      FROM driver
      WHERE id = 1
    """).query().singleRow();

    assertThat(driverAfter).satisfies(driver -> {
      assertThat(driverAfter.get("created_at")).isEqualTo(driverBefore.get("created_at"));

      var updatedAt = ((Timestamp) driver.get("updated_at")).toInstant();
      assertThat(updatedAt).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));

      assertThat(driver)
          .usingRecursiveComparison()
          .ignoringFields("created_at", "updated_at")
          .isEqualTo(new HashMap<String, Object>() {
            {
              put("id", 1L);
              put("full_name", "John Doe");
              put("mobile_phone", "+6591234567");
              put("location_lat", 10.0);
              put("location_lng", 20.0);
              put("status", DriverStatus.AVAILABLE.name());
              put("created_by", "SYSTEM");
              put("updated_by", "SYSTEM");
            }
          });
    });
  }

  @Test
  void updateDriverLocation_driverNotExist_responseNotFound() {
    // ACT
    var responseSpec = restTestClient
        .put()
        .uri("/drivers/1/location")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {
              "lat": 10.0,
              "lng": 20.0
            }
            """)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isNotFound()
      .expectBody(String.class)
      .value(body ->
        assertThat(JSON_TESTER.from(body))
          .isStrictlyEqualToJson(
            // language=JSON
            """
            {
              "detail": "Driver with id 1 not found",
              "instance": "/api/drivers/1/location",
              "status": 404,
              "title": "Not Found"
            }
            """));
    // @spotless:on
  }
}
