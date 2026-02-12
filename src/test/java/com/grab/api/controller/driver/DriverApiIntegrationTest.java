package com.grab.api.controller.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.grab.api.integration.ApiTest;
import com.grab.api.share.enumeration.DriverStatus;
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

    assertThat(driverAfter)
        .usingRecursiveComparison()
        .ignoringFields("created_at")
        .isEqualTo(new HashMap<String, Object>() {
          {
            put("id", 1L);
            put("full_name", "John Doe");
            put("mobile_phone", "+6591234567");
            put("status", DriverStatus.AVAILABLE.name());
          }
        });
  }

  @Test
  @Sql(statements = """
    INSERT INTO driver (full_name, mobile_phone, status)
    VALUES ('John Doe', '+6591234567', 'AVAILABLE');
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
}
