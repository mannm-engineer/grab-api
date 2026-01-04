package com.grab.api.repository;

import com.grab.api.repository.entity.RideEntity;
import com.grab.api.service.store.RideStore;
import com.grab.api.service.domain.ride.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideDatabaseStore implements RideStore {

  private final RideRepository rideRepository;

  public RideDatabaseStore(RideRepository rideRepository) {
    this.rideRepository = rideRepository;
  }

  @Override
  public void createRide(Ride ride) {
    rideRepository.save(RideEntity.of(ride));
  }
}
