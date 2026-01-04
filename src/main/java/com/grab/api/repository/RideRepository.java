package com.grab.api.repository;

import com.grab.api.repository.entity.RideEntity;
import org.springframework.data.repository.Repository;

public interface RideRepository extends Repository<RideEntity, Long> {

  void save(RideEntity ride);
}
