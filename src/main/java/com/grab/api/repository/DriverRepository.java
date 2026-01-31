package com.grab.api.repository;

import com.grab.api.repository.entity.DriverEntity;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface DriverRepository extends Repository<DriverEntity, Long> {

  Optional<DriverEntity> findById(Long id);

  DriverEntity save(DriverEntity driver);
}
