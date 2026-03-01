package com.grab.api.service.domain.driver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DriverCreate(
    String fullName,
    String mobilePhone,
    Integer age,
    Double rating,
    Boolean isVerified,
    BigDecimal balance,
    LocalDate dateOfBirth,
    List<DriverCreateDocument> documents) {

  public Driver driver(Map<String, String> filenameToUrl) {
    var driverDocuments = documents.stream()
        .map(document -> {
          var fileUrls = document.filenames().stream().map(filenameToUrl::get).toList();
          return new DriverDocument(
              document.type(), document.documentNumber(), document.expiryDate(), fileUrls);
        })
        .toList();
    return Driver.newDriver(
        fullName, mobilePhone, age, rating, isVerified, balance, dateOfBirth, driverDocuments);
  }
}
