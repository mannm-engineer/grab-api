package com.grab.api.repository.entity;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.share.enumeration.DriverStatus;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("driver")
public record DriverEntity(
    @Id @Column("id") @Nullable Long id,
    @Column("full_name") String fullName,
    @Column("mobile_phone") String mobilePhone,
    @Column("status") DriverStatus status) {

  public static DriverEntity of(Driver driver) {
    return new DriverEntity(
        Optional.ofNullable(driver.id()).map(Long::valueOf).orElse(null),
        driver.fullName(),
        driver.mobilePhone(),
        driver.status());
  }
}
