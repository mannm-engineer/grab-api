package com.grab.api.service.store;

import com.grab.api.service.domain.driver.Driver;
import java.util.Optional;

public interface DriverStore {

  Optional<Driver> get(String id);

  String create(Driver driver);

  void update(Driver driver);
}
