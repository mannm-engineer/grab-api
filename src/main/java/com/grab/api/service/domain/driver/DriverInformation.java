package com.grab.api.service.domain.driver;

import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record DriverInformation(
    @Nullable String id,
    String fullName,
    String mobilePhone,
    DriverStatus status,
    Integer age,
    Double rating,
    Boolean isVerified,
    BigDecimal balance,
    LocalDate dateOfBirth) {}
