package com.grab.api.controller.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.grab.api.integration.ApiTest;
import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.sql.Date;
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
              "mobilePhone": "+6591234567",
              "age": 30,
              "rating": 4.5,
              "isVerified": false,
              "balance": 1000.50,
              "dateOfBirth": "1990-01-15"
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

    assertThat(driverAfter)
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
          }
        });
  }

  @Test
  @Sql(statements = """
    INSERT INTO driver (full_name, mobile_phone, status, age, rating, is_verified, balance, date_of_birth)
    VALUES ('John Doe', '+6591234567', 'AVAILABLE', 30, 4.5, false, 1000.50, '1990-01-15');
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
              "mobilePhone": "+6591234567",
              "age": 25,
              "rating": 3.0,
              "isVerified": true,
              "balance": 500.00,
              "dateOfBirth": "1995-06-20"
            }
            """)
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
