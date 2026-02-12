package com.grab.api.repository;

import com.grab.api.repository.entity.DriverEntity;
import org.springframework.data.repository.Repository;

public interface DriverRepository extends Repository<DriverEntity, Long> {

  DriverEntity save(DriverEntity driver);
}
