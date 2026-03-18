package com.grab.api.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.grab.api.integration.ApiTest;
import com.grab.api.share.enumeration.RideStatus;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@ApiTest
class RideDispatchSchedulerIntegrationTest {

  @Autowired private RideDispatchScheduler rideDispatchScheduler;

  @Autowired private JdbcClient jdbcClient;

  @LocalServerPort private int port;

  // @spotless:off
  @Test
  @Sql(statements = {"""
      INSERT INTO driver (full_name, mobile_phone, status, age, rating, is_verified, balance, date_of_birth, location_lat, location_lng, created_at, created_by)
      VALUES ('John Doe', '+6591234567', 'AVAILABLE', 30, 4.5, true, 1000.50, '1990-01-15', 10.0, 20.0, now(), 'SYSTEM');
      """, """
      INSERT INTO ride (passenger_id, pickup_lat, pickup_lng, dropoff_lat, dropoff_lng, status)
      VALUES ('28088f63-d3a6-4714-913d-940a084df57e', 10.5, 20.5, 30.0, 40.0, 'REQUESTED');
      """})
  // @spotless:on
  void dispatchPendingRides_rideRequestedAndDriverAvailable_dispatchesAndSendsNotification()
      throws Exception {
    // ARRANGE
    var receivedMessages = new LinkedBlockingQueue<String>();
    var session = connectStomp();
    session.subscribe("/topic/user/1", new TestStompFrameHandler(receivedMessages));
    Thread.sleep(500); // Wait for subscription to be established

    // ACT
    rideDispatchScheduler.dispatchPendingRides();

    // ASSERT — verify ride status updated
    var rideAfter =
        jdbcClient.sql("SELECT status FROM ride WHERE id = 1").query().singleRow();
    assertThat(rideAfter.get("status")).isEqualTo(RideStatus.DISPATCHED.name());

    // ASSERT — verify WebSocket notification received
    var message = receivedMessages.poll(5, TimeUnit.SECONDS);
    assertThat(message).isNotNull();
    assertThat(message).contains("28088f63-d3a6-4714-913d-940a084df57e"); // passengerId
    assertThat(message).contains("New Ride");

    session.disconnect();
  }

  @Test
  @Sql(
      statements =
          """
      INSERT INTO ride (passenger_id, pickup_lat, pickup_lng, dropoff_lat, dropoff_lng, status)
      VALUES ('28088f63-d3a6-4714-913d-940a084df57e', 10.5, 20.5, 30.0, 40.0, 'REQUESTED');
      """)
  void dispatchPendingRides_noDriverAvailable_rideRemainsRequested() {
    // ACT
    rideDispatchScheduler.dispatchPendingRides();

    // ASSERT
    var rideAfter =
        jdbcClient.sql("SELECT status FROM ride WHERE id = 1").query().singleRow();
    assertThat(rideAfter.get("status")).isEqualTo(RideStatus.REQUESTED.name());
  }

  @Test
  void dispatchPendingRides_noPendingRides_doesNothing() {
    // ARRANGE
    var ridesBefore = jdbcClient.sql("SELECT * FROM ride").query().listOfRows();
    assertThat(ridesBefore).isEmpty();

    // ACT
    rideDispatchScheduler.dispatchPendingRides();

    // ASSERT
    var ridesAfter = jdbcClient.sql("SELECT * FROM ride").query().listOfRows();
    assertThat(ridesAfter).isEmpty();
  }

  // @spotless:off
  @Test
  @Sql(statements = {"""
      INSERT INTO driver (full_name, mobile_phone, status, age, rating, is_verified, balance, date_of_birth, location_lat, location_lng, created_at, created_by)
      VALUES ('John Doe', '+6591234567', 'AVAILABLE', 30, 4.5, true, 1000.50, '1990-01-15', 10.0, 20.0, now(), 'SYSTEM');
      """, """
      INSERT INTO ride (passenger_id, pickup_lat, pickup_lng, dropoff_lat, dropoff_lng, status)
      VALUES ('28088f63-d3a6-4714-913d-940a084df57e', 10.5, 20.5, 30.0, 40.0, 'REQUESTED'),
             ('38088f63-d3a6-4714-913d-940a084df57e', 11.0, 21.0, 31.0, 41.0, 'DISPATCHED');
      """})
  // @spotless:on
  void dispatchPendingRides_mixedStatuses_onlyDispatchesRequestedRides() {
    // ACT
    rideDispatchScheduler.dispatchPendingRides();

    // ASSERT
    var rides =
        jdbcClient.sql("SELECT id, status FROM ride ORDER BY id").query().listOfRows();
    assertThat(rides).hasSize(2);
    assertThat(rides.get(0).get("status")).isEqualTo(RideStatus.DISPATCHED.name());
    assertThat(rides.get(1).get("status")).isEqualTo(RideStatus.DISPATCHED.name());
  }

  private StompSession connectStomp() throws Exception {
    var stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    var url = "ws://localhost:" + port + "/api/ws";
    return stompClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
  }

  private static class TestStompFrameHandler implements StompFrameHandler {

    private final BlockingQueue<String> messages;

    TestStompFrameHandler(BlockingQueue<String> messages) {
      this.messages = messages;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
      return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      messages.add((String) payload);
    }
  }
}
