package com.grab.api.controller.ride;

import static org.assertj.core.api.Assertions.assertThat;

import com.grab.api.integration.ApiTest;
import com.grab.api.share.enumeration.RideStatus;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

@ApiTest
class RideApiIntegrationTest {

  @Autowired
  private RestTestClient restTestClient;

  @Autowired
  private JdbcClient jdbcClient;

  @Test
  void createRide_validRequest_responseCreated() {
    // ARRANGE
    var rideBefore = jdbcClient.sql("""
      SELECT *
      FROM ride
      WHERE id = 1
    """).query().optionalValue();

    assertThat(rideBefore).isEmpty();

    // ACT
    var responseSpec = restTestClient
        .post()
        .uri("/rides")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(
            // language=JSON
            """
            {
              "passengerId": "28088f63-d3a6-4714-913d-940a084df57e",
              "pickupLocation": {
                "lat": 10.0,
                "lng": 20.0
              },
              "dropoffLocation": {
                "lat": 30.0,
                "lng": 40.0
              }
            }
            """)
        .exchange();

    // ASSERT
    // @spotless:off
    responseSpec
      .expectStatus().isCreated()
      .expectBody().isEmpty();
    // @spotless:on

    var rideAfter = jdbcClient.sql("""
      SELECT *
      FROM ride
      WHERE id = 1
    """).query().singleRow();

    assertThat(rideAfter).usingRecursiveComparison().isEqualTo(new HashMap<String, Object>() {
      {
        put("id", 1L);
        put("passenger_id", UUID.fromString("28088f63-d3a6-4714-913d-940a084df57e"));
        put("pickup_lat", 10.0);
        put("pickup_lng", 20.0);
        put("dropoff_lat", 30.0);
        put("dropoff_lng", 40.0);
        put("status", RideStatus.REQUESTED.name());
      }
    });
  }
}
