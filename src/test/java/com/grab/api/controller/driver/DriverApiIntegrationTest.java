package com.grab.api.controller.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.grab.api.integration.ApiTest;
import com.grab.api.share.enumeration.DocumentType;
import com.grab.api.share.enumeration.DriverStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;

@ApiTest
class DriverApiIntegrationTest {

  private static final BasicJsonTester JSON_TESTER =
      new BasicJsonTester(DriverApiIntegrationTest.class);

  @Autowired
  private RestTestClient restTestClient;

  @Autowired
  private JdbcClient jdbcClient;

  @Test
  void createDriver_validRequest_responseCreated() throws IOException {
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
    var jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    var formData = new LinkedMultiValueMap<String, Object>();
    formData.add(
        "data",
        new HttpEntity<>(
            // language=JSON
            """
            {
              "fullName": "John Doe",
              "mobilePhone": "+6591234567",
              "age": 30,
              "rating": 4.5,
              "isVerified": false,
              "balance": 1000.50,
              "dateOfBirth": "1990-01-15",
              "documents": [
                {
                  "type": "DRIVERS_LICENSE",
                  "documentNumber": "S1234567A",
                  "expiryDate": "2030-06-01",
                  "filenames": ["test-license.txt"]
                }
              ]
            }
            """, jsonHeaders));
    formData.add("files", new ClassPathResource("test-license.txt"));

    var responseSpec = restTestClient
        .post()
        .uri("/drivers")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(MediaType.APPLICATION_JSON)
        .body(formData)
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
              put("status", DriverStatus.AVAILABLE.name());
              put("age", 30);
              put("rating", 4.5);
              put("is_verified", false);
              put("balance", new BigDecimal("1000.50"));
              put("date_of_birth", Date.valueOf("1990-01-15"));
              put("created_by", "SYSTEM");
            }
          });
    });

    var documentAfter = jdbcClient.sql("""
      SELECT *
      FROM driver_document
      WHERE driver_id = 1
    """).query().singleRow();

    // @spotless:off
    assertThat(documentAfter)
        .isEqualTo(new HashMap<String, Object>() {
          {
            put("id", 1L);
            put("driver_id", 1L);
            put("type", DocumentType.DRIVERS_LICENSE.name());
            put("document_number", "S1234567A");
            put("expiry_date", Date.valueOf(LocalDate.of(2030, 6, 1)));
          }
        });
    // @spotless:on

    var documentFileAfter = jdbcClient.sql("""
      SELECT *
      FROM driver_document_file
      WHERE document_id = 1
    """).query().singleRow();

    var storedFilePath = (String) documentFileAfter.get("file_url");
    assertThat(storedFilePath).isNotBlank();

    var sentFileResource = new ClassPathResource("test-license.txt");
    assertThat(Files.readAllBytes(Path.of(storedFilePath)))
        .isEqualTo(sentFileResource.getContentAsByteArray());

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
              put("domain_type", "DRIVER");
              put("event_key", "1");
              put("event_type", "CREATED");
              put("payload", "1");
            }
          });
    });
  }

  @Test
  @Sql(statements = """
    INSERT INTO driver (full_name, mobile_phone, status, age, rating, is_verified, balance, date_of_birth, created_at, created_by)
    VALUES ('John Doe', '+6591234567', 'AVAILABLE', 30, 4.5, false, 1000.50, '1990-01-15', now(), 'SYSTEM');
  """)
  void createDriver_duplicateMobilePhone_responseConflict() {
    // ACT
    var jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    var formData = new LinkedMultiValueMap<String, Object>();
    formData.add(
        "data",
        new HttpEntity<>(
            // language=JSON
            """
            {
              "fullName": "John Doe",
              "mobilePhone": "+6591234567",
              "age": 30,
              "rating": 4.5,
              "isVerified": false,
              "balance": 1000.50,
              "dateOfBirth": "1990-01-15",
              "documents": [
                {
                  "type": "DRIVERS_LICENSE",
                  "documentNumber": "S1234567A",
                  "expiryDate": "2030-06-01",
                  "filenames": ["test-license.txt"]
                }
              ]
            }
            """, jsonHeaders));
    formData.add("files", new ClassPathResource("test-license.txt"));

    var responseSpec = restTestClient
        .post()
        .uri("/drivers")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(MediaType.APPLICATION_JSON)
        .body(formData)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isEqualTo(HttpStatus.CONFLICT)
      .expectBody(String.class)
      .value(body ->
        assertThat(JSON_TESTER.from(body))
          .isStrictlyEqualToJson(
            // language=JSON
            """
            {
              "detail": "Resource already exist.",
              "instance": "/api/drivers",
              "status": 409,
              "title": "Conflict"
            }
            """));
    // @spotless:on
  }
}
