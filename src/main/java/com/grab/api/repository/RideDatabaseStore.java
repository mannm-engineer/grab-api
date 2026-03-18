package com.grab.api.repository;

import com.grab.api.repository.entity.RideEntity;
import com.grab.api.service.RideStore;
import com.grab.api.service.domain.ride.Ride;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RideDatabaseStore implements RideStore {

  private final RideRepository rideRepository;

  public RideDatabaseStore(RideRepository rideRepository) {
    this.rideRepository = rideRepository;
  }

  @Override
  public String createRide(Ride ride) {
    var created = rideRepository.save(RideEntity.of(ride));
    return Objects.requireNonNull(created.id()).toString();
  }

  @Override
  public Optional<Ride> getRide(String id) {
    return rideRepository.findById(Long.valueOf(id)).map(RideEntity::ride);
  }
}
