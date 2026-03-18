package com.grab.api.scheduler;

import com.grab.api.service.RideDispatchService;
import com.grab.api.service.RideStore;
import com.grab.api.share.enumeration.RideStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RideDispatchScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RideDispatchScheduler.class);

  private final RideStore rideStore;
  private final RideDispatchService rideDispatchService;

  public RideDispatchScheduler(RideStore rideStore, RideDispatchService rideDispatchService) {
    this.rideStore = rideStore;
    this.rideDispatchService = rideDispatchService;
  }

  @Scheduled(fixedDelay = 5000)
  public void dispatchPendingRides() {
    var pendingRides = rideStore.findByStatus(RideStatus.REQUESTED);
    for (var ride : pendingRides) {
      try {
        rideDispatchService.dispatchRide(ride);
      } catch (Exception e) {
        LOGGER.error("Failed to dispatch ride id={}, will retry next poll", ride.id(), e);
      }
    }
  }
}
