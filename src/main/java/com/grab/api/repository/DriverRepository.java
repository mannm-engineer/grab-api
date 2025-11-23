package com.grab.api.repository;

import com.grab.api.repository.entity.DriverEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface DriverRepository extends CrudRepository<DriverEntity, Long> {

  @Modifying
  @Query("""
        UPDATE driver
        SET location_lat = :lat, location_lng = :lng
        WHERE id = :id
    """)
  int updateLocation(Long id, Double lat, Double lng);
}
