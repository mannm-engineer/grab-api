package com.grab.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class DriverApiIntegrationTest {

  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private TestRestTemplate http;

  @Autowired
  private JdbcTemplate jdbc;

  @Test
  @Sql(statements = "INSERT INTO driver (location_lat, location_lng) VALUES (NULL, NULL)")
  void updateDriverLocation_driverExists_databaseStateIsCorrect() {

    // --- STEP 2: send HTTP request ---
    var requestJson = """
      {
        "lat": 10.0,
        "lng": 20.0
      }
      """;

    var response = http.exchange("/drivers/1/location", HttpMethod.PUT, new HttpEntity<>(
      requestJson, new HttpHeaders() {

        {
          setContentType(MediaType.APPLICATION_JSON);
          setAccept(List.of(MediaType.APPLICATION_JSON));
        }
      }
    ), String.class
    );

    // --- STEP 3: assert the HTTP response ---
    assertThat(response.getStatusCode().value()).isEqualTo(204);
    assertThat(response.getBody()).isNull();

    // --- STEP 4: verify database values ---
    Map<String, Object> row = jdbc.queryForMap(
      "SELECT location_lat, location_lng FROM driver WHERE id = 1"
    );

    assertThat(row.get("location_lat")).isEqualTo(10.0);
    assertThat(row.get("location_lng")).isEqualTo(20.0);
  }

  @Test
  void updateDriverLocation_driverNotExist_responseNotFound() {

    // --- STEP 1: send HTTP request ---
    var requestJson = """
      {
        "lat": 10.0,
        "lng": 20.0
      }
      """;

    var response = http.exchange("/drivers/" + -1 + "/location", HttpMethod.PUT, new HttpEntity<>(
      requestJson, new HttpHeaders() {

        {
          setContentType(MediaType.APPLICATION_JSON);
          setAccept(List.of(MediaType.APPLICATION_JSON));
        }
      }
    ), String.class
    );

    // --- STEP 2: assert the HTTP response ---
    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(response.getBody()).isNotNull();
  }
}
