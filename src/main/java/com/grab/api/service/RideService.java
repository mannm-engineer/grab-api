package com.grab.api.service;

import com.grab.api.service.domain.ride.Ride;
import org.springframework.stereotype.Service;

@Service
public class RideService {

  private final RideDispatchService rideDispatchService;
  private final RideStore rideStore;

  public RideService(RideDispatchService rideDispatchService, RideStore rideStore) {
    this.rideDispatchService = rideDispatchService;
    this.rideStore = rideStore;
  }

  public void createRide(Ride ride) {
    rideStore.createRide(ride);
    rideDispatchService.dispatchRide(ride);
  }
}
