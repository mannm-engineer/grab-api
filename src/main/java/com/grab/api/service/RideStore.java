package com.grab.api.service;

import com.grab.api.service.domain.ride.Ride;
import java.util.Optional;

public interface RideStore {

  String createRide(Ride ride);

  Optional<Ride> getRide(String id);
}
