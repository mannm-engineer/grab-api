package com.grab.api.service.domain.driver;

import com.grab.api.service.domain.Audit;
import com.grab.api.service.domain.Location;
import com.grab.api.share.enumeration.DriverStatus;
import org.jspecify.annotations.Nullable;

public record Driver(
    @Nullable String id,
    String mapId,
    String fullName,
    String mobilePhone,
    @Nullable Location location,
    DriverStatus status,
    @Nullable Audit audit) {

  public static Driver newDriver(String mapId, String fullName, String mobilePhone) {
    return new Driver(null, mapId, fullName, mobilePhone, null, DriverStatus.AVAILABLE, null);
  }

  public Driver withLocation(Location location) {
    return new Driver(id, mapId, fullName, mobilePhone, location, status, audit);
  }
}
