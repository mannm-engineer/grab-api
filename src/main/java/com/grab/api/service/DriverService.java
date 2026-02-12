package com.grab.api.service;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.store.DriverStore;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

  private final DriverStore driverStore;

  public DriverService(DriverStore driverStore) {
    this.driverStore = driverStore;
  }

  public String createDriver(Driver driver) {
    return driverStore.createDriver(driver);
  }
}
