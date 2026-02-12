package com.grab.api.service.domain.driver;

import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record Driver(
    @Nullable String id,
    String fullName,
    String mobilePhone,
    DriverStatus status,
    Integer age,
    Double rating,
    Boolean isVerified,
    BigDecimal balance,
    LocalDate dateOfBirth) {

  public static Driver newDriver(
      String fullName,
      String mobilePhone,
      Integer age,
      Double rating,
      Boolean isVerified,
      BigDecimal balance,
      LocalDate dateOfBirth) {
    return new Driver(
        null,
        fullName,
        mobilePhone,
        DriverStatus.AVAILABLE,
        age,
        rating,
        isVerified,
        balance,
        dateOfBirth);
  }
}
