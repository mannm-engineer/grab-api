package com.grab.api.service.domain.driver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DriverCreate(
    String fullName,
    String mobilePhone,
    Integer age,
    Double rating,
    Boolean isVerified,
    BigDecimal balance,
    LocalDate dateOfBirth,
    List<DriverCreateDocument> documents) {}
