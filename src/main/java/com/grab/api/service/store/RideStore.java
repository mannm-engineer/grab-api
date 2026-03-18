package com.grab.api.service.store;

import com.grab.api.service.domain.ride.Ride;
import com.grab.api.share.enumeration.RideStatus;
import java.util.List;

public interface RideStore {

  List<Ride> findByStatus(RideStatus status);

  void create(Ride ride);

  void update(Ride ride);
}
