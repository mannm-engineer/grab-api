package com.grab.api.service;

import com.grab.api.service.domain.ride.Ride;
import com.grab.api.share.enumeration.RideStatus;
import java.util.List;
import java.util.Optional;

public interface RideStore {

  String createRide(Ride ride);

  Optional<Ride> getRide(String id);

  List<Ride> findByStatus(RideStatus status);

  void updateRide(Ride ride);
}
