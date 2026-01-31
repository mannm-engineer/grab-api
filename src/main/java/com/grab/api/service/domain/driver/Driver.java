package com.grab.api.service.domain.driver;

import com.grab.api.service.domain.Audit;
import com.grab.api.service.domain.Location;
import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record Driver(
    @Nullable String id,
    String fullName,
    String mobilePhone,
    @Nullable Location location,
    DriverStatus status,
    Integer age,
    Double rating,
    Boolean isVerified,
    BigDecimal balance,
    LocalDate dateOfBirth,
    List<DriverDocument> documents,
    @Nullable Audit audit) {

  public static Driver newDriver(
      String fullName,
      String mobilePhone,
      Integer age,
      Double rating,
      Boolean isVerified,
      BigDecimal balance,
      LocalDate dateOfBirth,
      List<DriverDocument> documents) {
    return new Driver(
        null,
        fullName,
        mobilePhone,
        null,
        DriverStatus.AVAILABLE,
        age,
        rating,
        isVerified,
        balance,
        dateOfBirth,
        documents,
        null);
  }

  public Driver withLocation(Location location) {
    return new Driver(
        id,
        fullName,
        mobilePhone,
        location,
        status,
        age,
        rating,
        isVerified,
        balance,
        dateOfBirth,
        documents,
        audit);
  }
}
