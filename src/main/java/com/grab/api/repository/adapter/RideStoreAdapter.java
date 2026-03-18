package com.grab.api.repository.adapter;

import com.grab.api.repository.RideRepository;
import com.grab.api.repository.entity.RideEntity;
import com.grab.api.service.domain.ride.Ride;
import com.grab.api.service.store.RideStore;
import com.grab.api.share.enumeration.RideStatus;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class RideStoreAdapter implements RideStore {

  private final RideRepository rideRepository;

  public RideStoreAdapter(RideRepository rideRepository) {
    this.rideRepository = rideRepository;
  }

  @Override
  public void create(Ride ride) {
    var created = rideRepository.save(RideEntity.of(ride));
    Objects.requireNonNull(created.id());
  }

  @Override
  public List<Ride> findByStatus(RideStatus status) {
    return rideRepository.findByStatus(status).stream().map(RideEntity::ride).toList();
  }

  @Override
  public void update(Ride ride) {
    rideRepository.save(RideEntity.of(ride));
  }
}
