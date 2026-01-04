package com.grab.api.repository.adapter;

import com.grab.api.repository.RideRepository;
import com.grab.api.repository.entity.RideEntity;
import com.grab.api.service.domain.ride.Ride;
import com.grab.api.service.store.RideStore;
import org.springframework.stereotype.Component;

@Component
public class RideStoreAdapter implements RideStore {

  private final RideRepository rideRepository;

  public RideStoreAdapter(RideRepository rideRepository) {
    this.rideRepository = rideRepository;
  }

  @Override
  public void create(Ride ride) {
    rideRepository.save(RideEntity.of(ride));
  }
}
