package com.grab.api.repository;

import com.grab.api.repository.entity.RideEntity;
import com.grab.api.share.enumeration.RideStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RideRepository extends Repository<RideEntity, Long> {

  RideEntity save(RideEntity ride);

  Optional<RideEntity> findById(Long id);

  List<RideEntity> findByStatus(RideStatus status);
}
