package com.grab.api.service;

import static com.grab.api.share.enumeration.DomainEventType.REQUESTED;

import com.grab.api.service.domain.ride.Ride;
import com.grab.api.service.model.OutboxEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RideService {

  private final RideStore rideStore;
  private final OutboxEventStore outboxEventStore;
  private final String rideEventsTopic;

  public RideService(
      RideStore rideStore,
      OutboxEventStore outboxEventStore,
      @Value("${app.kafka.topics.ride-events}") String rideEventsTopic) {
    this.rideStore = rideStore;
    this.outboxEventStore = outboxEventStore;
    this.rideEventsTopic = rideEventsTopic;
  }

  @Transactional
  public void createRide(Ride ride) {
    var id = rideStore.createRide(ride);
    outboxEventStore.createEvent(new OutboxEvent(rideEventsTopic, id, REQUESTED, id));
  }
}
