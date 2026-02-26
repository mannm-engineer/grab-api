package com.grab.api.service;

import com.grab.api.service.domain.Location;
import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.driver.DriverPatch;
import com.grab.api.service.domain.driver.DriverSearchCriteria;
import com.grab.api.service.exception.DomainNotFoundException;
import com.grab.api.service.model.OutboxEvent;
import com.grab.api.share.enumeration.DriverStatus;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {

  private final DriverStore driverStore;
  private final OutboxEventStore outboxEventStore;

  public DriverService(DriverStore driverStore, OutboxEventStore outboxEventStore) {
    this.driverStore = driverStore;
    this.outboxEventStore = outboxEventStore;
  }

  public Optional<Driver> findNearestDriver(String mapId, Location pickupLocation) {
    var criteria = new DriverSearchCriteria(mapId, DriverStatus.AVAILABLE, true);
    return driverStore.find(criteria).stream()
        .min(Comparator.comparing(
            driver -> Objects.requireNonNull(driver.location()).distanceTo(pickupLocation)));
  }

  @Transactional
  public String createDriver(Driver driver) {
    var id = driverStore.createDriver(driver);
    outboxEventStore.createEvent(new OutboxEvent("driver-events", id, "CREATED", id));
    return id;
  }

  public void patchDriver(String id, DriverPatch patch) {
    var existing = driverStore
        .getDriver(id)
        .orElseThrow(() -> new DomainNotFoundException("Driver with id " + id + " not found"));

    var updated = patch.applyTo(existing);

    driverStore.updateDriver(updated);
  }
}
