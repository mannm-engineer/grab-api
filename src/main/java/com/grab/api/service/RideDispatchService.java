package com.grab.api.service;

import com.grab.api.service.domain.Location;
import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.notification.Notification;
import com.grab.api.service.domain.ride.Ride;
import com.grab.api.service.exception.DomainNotFoundException;
import com.grab.api.service.store.RideStore;
import com.grab.api.share.enumeration.RideStatus;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class RideDispatchService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RideDispatchService.class);

  private final DriverService driverService;
  private final NotificationService notificationService;
  private final RideStore rideStore;
  private final ObjectMapper objectMapper;

  public RideDispatchService(
      DriverService driverService,
      NotificationService notificationService,
      RideStore rideStore,
      ObjectMapper objectMapper) {
    this.driverService = driverService;
    this.notificationService = notificationService;
    this.rideStore = rideStore;
    this.objectMapper = objectMapper;
  }

  public void dispatchPendingRides() {
    var pendingRides = rideStore.findByStatus(RideStatus.REQUESTED);
    LOGGER.info("Found {} pending rides to dispatch", pendingRides.size());
    for (var ride : pendingRides) {
      try {
        dispatchRide(ride);
      } catch (Exception e) {
        LOGGER.error("Failed to dispatch ride id={}, will retry next poll", ride.id(), e);
      }
    }
  }

  private void dispatchRide(Ride ride) {
    LOGGER.info(
        "Dispatching ride: rideId={}, passengerId={}, pickup=({}, {}), dropoff=({}, {})",
        ride.id(),
        ride.passengerId(),
        ride.pickupLocation().lat(),
        ride.pickupLocation().lng(),
        ride.dropoffLocation().lat(),
        ride.dropoffLocation().lng());

    var driver = driverService
        .findNearestDriver(ride.pickupLocation())
        .orElseThrow(() -> new DomainNotFoundException("Driver not found"));

    LOGGER.info(
        "Nearest driver found: driverId={}, driverLocation=({}, {}), rideId={}",
        driver.id(),
        Objects.requireNonNull(driver.location()).lat(),
        Objects.requireNonNull(driver.location()).lng(),
        ride.id());

    sendRide(driver, ride);
    rideStore.updateRide(ride.withStatus(RideStatus.DISPATCHED));
  }

  private void sendRide(Driver driver, Ride ride) {
    var payload = new RideDispatchNotificationPayload(
        ride.passengerId(),
        String.valueOf(driver.id()),
        ride.pickupLocation(),
        ride.dropoffLocation(),
        String.format(
            "Pickup: (%.6f, %.6f) → Dropoff: (%.6f, %.6f)",
            ride.pickupLocation().lat(),
            ride.pickupLocation().lng(),
            ride.dropoffLocation().lat(),
            ride.dropoffLocation().lng()));

    var jsonPayload = objectMapper.writeValueAsString(payload);

    notificationService.sendNotification(
        new Notification(Objects.requireNonNull(driver.id()), "New Ride", jsonPayload));
  }

  public record RideDispatchNotificationPayload(
      String passengerId,
      String driverId,
      Location pickupLocation,
      Location dropoffLocation,
      String message) {}
}
