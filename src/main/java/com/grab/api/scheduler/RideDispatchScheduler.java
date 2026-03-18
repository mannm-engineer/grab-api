package com.grab.api.scheduler;

import com.grab.api.service.RideDispatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RideDispatchScheduler {

  private final RideDispatchService rideDispatchService;

  public RideDispatchScheduler(RideDispatchService rideDispatchService) {
    this.rideDispatchService = rideDispatchService;
  }

  @Scheduled(fixedDelay = 5000)
  public void dispatchPending() {
    rideDispatchService.dispatchAllPending();
  }
}
