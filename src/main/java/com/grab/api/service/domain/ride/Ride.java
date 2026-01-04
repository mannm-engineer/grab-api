package com.grab.api.service.domain.ride;

import com.grab.api.service.domain.Location;
import com.grab.api.share.enumeration.RideStatus;
import org.jspecify.annotations.Nullable;

public record Ride(
    @Nullable String id,
    String passengerId,
    Location pickupLocation,
    Location dropoffLocation,
    RideStatus status) {

  public static Ride newRide(
      String passengerId, Location pickupLocation, Location dropoffLocation) {
    return new Ride(null, passengerId, pickupLocation, dropoffLocation, RideStatus.REQUESTED);
  }
}
