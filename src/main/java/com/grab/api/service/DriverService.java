package com.grab.api.service;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.model.OutboxEvent;
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

  @Transactional
  public String createDriver(Driver driver) {
    var id = driverStore.createDriver(driver);
    outboxEventStore.createEvent(new OutboxEvent("driver-events", id, "CREATED", id));
    return id;
  }
}
