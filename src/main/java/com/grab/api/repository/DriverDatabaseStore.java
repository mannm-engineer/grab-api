package com.grab.api.repository;

import com.grab.api.repository.entity.DriverEntity;
import com.grab.api.service.DriverStore;
import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.driver.DriverSearchCriteria;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DriverDatabaseStore implements DriverStore {

  private final DriverRepository driverRepository;

  public DriverDatabaseStore(DriverRepository driverRepository) {
    this.driverRepository = driverRepository;
  }

  @Override
  public List<Driver> find(DriverSearchCriteria criteria) {
    var stream = driverRepository.findAll().stream();

    if (criteria.status() != null) {
      stream = stream.filter(entity -> entity.status() == criteria.status());
    }
    if (criteria.hasLocation()) {
      stream = stream.filter(entity -> entity.location() != null);
    }

    return stream.map(DriverEntity::driver).toList();
  }

  @Override
  public Optional<Driver> getDriver(String id) {
    return driverRepository.findById(Long.valueOf(id)).map(DriverEntity::driver);
  }

  @Override
  public String createDriver(Driver driver) {
    var created = driverRepository.save(DriverEntity.of(driver));
    return Objects.requireNonNull(created.id()).toString();
  }

  @Override
  public void updateDriver(Driver driver) {
    driverRepository.save(DriverEntity.of(driver));
  }
}
