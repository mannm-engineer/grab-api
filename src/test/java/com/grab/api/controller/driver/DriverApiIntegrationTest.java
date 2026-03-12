package com.grab.api.controller.driver;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.grab.api.integration.ApiTest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@ApiTest
@EnableWireMock(@ConfigureWireMock(name = "driver-api", baseUrlProperties = "driver.api.base-url"))
class DriverApiIntegrationTest {

  private static final BasicJsonTester JSON_TESTER =
      new BasicJsonTester(DriverApiIntegrationTest.class);

  private static final MediaType MERGE_PATCH_JSON =
      MediaType.valueOf("application/merge-patch+json");

  @Autowired
  private RestTestClient restTestClient;

  @Autowired
  private JdbcClient jdbcClient;

  @InjectWireMock("driver-api")
  private WireMockServer driverApi;

  @Test
  void createDriver_validRequest_responseCreated() {
    // ARRANGE
    var outboxEventBefore = jdbcClient.sql("""
      SELECT *
      FROM outbox_event
      WHERE id = 1
    """).query().optionalValue();

    assertThat(outboxEventBefore).isEmpty();

    // language=JSON
    var driverApiResponse = """
        {
          "id": "1",
          "fullName": "John Doe",
          "mobilePhone": "+6591234567",
          "location": null,
          "status": "AVAILABLE",
          "age": 30,
          "rating": 4.5,
          "isVerified": false,
          "balance": 1000.50,
          "dateOfBirth": "1990-01-15",
          "documents": [],
          "audit": null
        }
        """;

    driverApi.stubFor(post(urlEqualTo("/drivers")).willReturn(okJson(driverApiResponse)));

    // ACT
    var jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    var formData = new LinkedMultiValueMap<String, Object>();
    formData.add(
        "driverData",
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
                  "expiryDate": "2030-06-01"
                }
              ]
            }
            """, jsonHeaders));
    formData.add("documentFiles", new ClassPathResource("test-license.txt"));

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
  void patchDriver_updateLocation_responseNoContent() {
    // ARRANGE
    // language=JSON
    var existingDriverJson = """
        {
          "id": "1",
          "fullName": "John Doe",
          "mobilePhone": "+6591234567",
          "location": null,
          "status": "AVAILABLE",
          "age": 30,
          "rating": 4.5,
          "isVerified": false,
          "balance": 1000.50,
          "dateOfBirth": "1990-01-15",
          "documents": [],
          "audit": null
        }
        """;

    driverApi.stubFor(get(urlEqualTo("/drivers/1")).willReturn(okJson(existingDriverJson)));
    driverApi.stubFor(put(urlEqualTo("/drivers/1")).willReturn(WireMock.ok()));

    // ACT
    var responseSpec = restTestClient
        .patch()
        .uri("/drivers/1")
        .contentType(MERGE_PATCH_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {
              "location": {
                "lat": 10.0,
                "lng": 20.0
              }
            }
            """)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isNoContent()
      .expectBody().isEmpty();
    // @spotless:on

    driverApi.verify(putRequestedFor(urlEqualTo("/drivers/1"))
        .withRequestBody(equalToJson(
            // language=JSON
            """
                {
                  "id": "1",
                  "fullName": "John Doe",
                  "mobilePhone": "+6591234567",
                  "location": {
                    "lat": 10.0,
                    "lng": 20.0
                  },
                  "status": "AVAILABLE",
                  "age": 30,
                  "rating": 4.5,
                  "isVerified": false,
                  "balance": 1000.50,
                  "dateOfBirth": "1990-01-15",
                  "documents": [],
                  "audit": null
                }
                """)));
  }

  @Test
  void patchDriver_remainLocation_responseNoContent() {
    // ARRANGE
    // language=JSON
    var existingDriverJson = """
        {
          "id": "1",
          "fullName": "John Doe",
          "mobilePhone": "+6591234567",
          "location": {
            "lat": 10.0,
            "lng": 20.0
          },
          "status": "AVAILABLE",
          "age": 30,
          "rating": 4.5,
          "isVerified": false,
          "balance": 1000.50,
          "dateOfBirth": "1990-01-15",
          "documents": [],
          "audit": null
        }
        """;

    driverApi.stubFor(get(urlEqualTo("/drivers/1")).willReturn(okJson(existingDriverJson)));
    driverApi.stubFor(put(urlEqualTo("/drivers/1")).willReturn(WireMock.ok()));

    // ACT
    var responseSpec = restTestClient
        .patch()
        .uri("/drivers/1")
        .contentType(MERGE_PATCH_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {}
            """)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isNoContent()
      .expectBody().isEmpty();
    // @spotless:on

    driverApi.verify(
        putRequestedFor(urlEqualTo("/drivers/1")).withRequestBody(equalToJson(existingDriverJson)));
  }

  @Test
  void patchDriver_driverNotExist_responseNotFound() {
    // ARRANGE
    driverApi.stubFor(get(urlEqualTo("/drivers/1")).willReturn(okJson("null")));

    // ACT
    var responseSpec = restTestClient
        .patch()
        .uri("/drivers/1")
        .contentType(MERGE_PATCH_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {
              "location": {
                "lat": 10.0,
                "lng": 20.0
              }
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
              "instance": "/api/drivers/1",
              "status": 404,
              "title": "Not Found"
            }
            """));
    // @spotless:on
  }
}
