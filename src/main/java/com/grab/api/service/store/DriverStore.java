package com.grab.api.service.store;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.driver.DriverSearchCriteria;
import java.util.List;
import java.util.Optional;

public interface DriverStore {

  List<Driver> find(DriverSearchCriteria criteria);

  Optional<Driver> getDriver(String id);

  String createDriver(Driver driver);

  void updateDriver(Driver driver);
}
