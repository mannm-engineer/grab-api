package com.grab.api.consumer;

import com.grab.api.service.RideDispatchService;
import com.grab.api.share.enumeration.DomainEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class RideEventConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(RideEventConsumer.class);

  private final RideDispatchService rideDispatchService;

  public RideEventConsumer(RideDispatchService rideDispatchService) {
    this.rideDispatchService = rideDispatchService;
  }

  @KafkaListener(topics = "${app.kafka.topics.ride-events}")
  public void onRideEvent(@Payload String rideId, @Header("eventType") String eventType) {
    LOGGER.info("Received ride event: eventType={}, rideId={}", eventType, rideId);

    if (DomainEventType.REQUESTED.name().equals(eventType)) {
      rideDispatchService.dispatchRide(rideId);
    } else {
      LOGGER.warn("Unknown ride event type: {}", eventType);
    }
  }
}
