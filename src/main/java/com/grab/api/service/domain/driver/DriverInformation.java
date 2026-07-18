package com.grab.api.service.domain.driver;

import com.grab.api.service.domain.Audit;
import com.grab.api.service.domain.Location;
import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record DriverInformation(
    @Nullable String id,
    String fullName,
    String mobilePhone,
    @Nullable Location location,
    DriverStatus status,
    Integer age,
    Double rating,
    Boolean isVerified,
    BigDecimal balance,
    LocalDate birthDate,
    List<DriverDocument> documents,
    @Nullable Audit audit) {

  public DriverInformation withLocation(Location location) {
    return new DriverInformation(
        id,
        fullName,
        mobilePhone,
        location,
        status,
        age,
        rating,
        isVerified,
        balance,
        birthDate,
        documents,
        audit);
  }
}
