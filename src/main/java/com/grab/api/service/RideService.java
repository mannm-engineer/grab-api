package com.grab.api.service;

import com.grab.api.service.domain.ride.Ride;
import com.grab.api.service.store.RideStore;
import org.springframework.stereotype.Service;

@Service
public class RideService {

  private final RideStore rideStore;

  public RideService(RideStore rideStore) {
    this.rideStore = rideStore;
  }

  public void createRide(Ride ride) {
    rideStore.createRide(ride);
  }
}
