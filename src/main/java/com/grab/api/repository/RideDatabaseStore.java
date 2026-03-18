package com.grab.api.repository;

import com.grab.api.repository.entity.RideEntity;
import com.grab.api.service.domain.ride.Ride;
import com.grab.api.service.store.RideStore;
import com.grab.api.share.enumeration.RideStatus;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RideDatabaseStore implements RideStore {

  private final RideRepository rideRepository;

  public RideDatabaseStore(RideRepository rideRepository) {
    this.rideRepository = rideRepository;
  }

  @Override
  public void createRide(Ride ride) {
    var created = rideRepository.save(RideEntity.of(ride));
    Objects.requireNonNull(created.id());
  }

  @Override
  public List<Ride> findByStatus(RideStatus status) {
    return rideRepository.findByStatus(status).stream().map(RideEntity::ride).toList();
  }

  @Override
  public void updateRide(Ride ride) {
    rideRepository.save(RideEntity.of(ride));
  }
}
