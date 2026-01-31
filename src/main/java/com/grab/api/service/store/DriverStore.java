package com.grab.api.service.store;

import com.grab.api.service.domain.driver.Driver;
import java.util.Optional;

public interface DriverStore {

  Optional<Driver> getDriver(String id);

  String createDriver(Driver driver);

  void updateDriver(Driver driver);
}
