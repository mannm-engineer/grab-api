package com.grab.api.repository.adapter;

import com.grab.api.repository.DriverRepository;
import com.grab.api.repository.entity.DriverEntity;
import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.store.DriverStore;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class DriverStoreAdapter implements DriverStore {

  private final DriverRepository driverRepository;

  public DriverStoreAdapter(DriverRepository driverRepository) {
    this.driverRepository = driverRepository;
  }

  @Override
  public String create(Driver driver) {
    var created = driverRepository.save(DriverEntity.of(driver));
    return Objects.requireNonNull(created.id()).toString();
  }
}
