package com.grab.api.service.domain.driver;

import com.grab.api.share.enumeration.DriverStatus;
import org.jspecify.annotations.Nullable;

public record Driver(
    @Nullable String id, String fullName, String mobilePhone, DriverStatus status) {

  public static Driver newDriver(String fullName, String mobilePhone) {
    return new Driver(null, fullName, mobilePhone, DriverStatus.AVAILABLE);
  }
}
