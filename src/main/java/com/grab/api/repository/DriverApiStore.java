package com.grab.api.repository;

import com.grab.api.service.DriverStore;
import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.driver.DriverSearchCriteria;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DriverApiStore implements DriverStore {

  private final DriverHttpClient driverHttpClient;

  public DriverApiStore(DriverHttpClient driverHttpClient) {
    this.driverHttpClient = driverHttpClient;
  }

  @Override
  public List<Driver> find(DriverSearchCriteria criteria) {
    var status = criteria.status() != null ? criteria.status().name() : null;
    var hasLocation = criteria.hasLocation() ? Boolean.TRUE : null;
    return driverHttpClient.findDrivers(status, hasLocation);
  }

  @Override
  public Optional<Driver> getDriver(String id) {
    return Optional.ofNullable(driverHttpClient.getDriver(id));
  }

  @Override
  public String createDriver(Driver driver) {
    var created = driverHttpClient.createDriver(driver);
    return Objects.requireNonNull(created.id());
  }

  @Override
  public void updateDriver(Driver driver) {
    driverHttpClient.updateDriver(Objects.requireNonNull(driver.id()), driver);
  }
}
