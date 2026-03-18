package com.grab.api.repository;

import com.grab.api.repository.entity.RideEntity;
import com.grab.api.share.enumeration.RideStatus;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface RideRepository extends Repository<RideEntity, Long> {

  RideEntity save(RideEntity ride);

  List<RideEntity> findByStatus(RideStatus status);
}
