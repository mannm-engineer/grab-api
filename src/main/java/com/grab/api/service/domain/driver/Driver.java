package com.grab.api.service.domain.driver;

import com.grab.api.share.enumeration.DriverStatus;
import java.util.Map;

public final class Driver {

  private final DriverInformation information;

  private Driver(DriverInformation information) {
    this.information = information;
  }

  public static Driver create(DriverCreate driverCreate, Map<String, String> fileUrls) {
    var driverDocuments = driverCreate.documents().stream()
        .map(document -> {
          var urls = document.files().stream().map(file -> fileUrls.get(file.filename())).toList();
          return new DriverDocument(
              document.type(), document.documentNumber(), document.expiryDate(), urls);
        })
        .toList();
    return new Driver(new DriverInformation(
        null,
        driverCreate.fullName(),
        driverCreate.mobilePhone(),
        DriverStatus.AVAILABLE,
        driverCreate.age(),
        driverCreate.rating(),
        driverCreate.isVerified(),
        driverCreate.balance(),
        driverCreate.dateOfBirth(),
        driverDocuments));
  }

  public DriverInformation information() {
    return information;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof Driver other
        && information.id() != null
        && information.id().equals(other.information.id());
  }

  @Override
  public int hashCode() {
    return Driver.class.hashCode();
  }
}
