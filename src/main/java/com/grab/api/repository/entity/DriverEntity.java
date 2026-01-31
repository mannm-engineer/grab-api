package com.grab.api.repository.entity;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.share.enumeration.DriverStatus;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Table("driver")
public record DriverEntity(
    @Id @Column("id") @Nullable Long id,
    @Column("full_name") String fullName,
    @Column("mobile_phone") String mobilePhone,

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix = "location_") @Nullable
    LocationEntity location,

    @Column("status") DriverStatus status,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) @Nullable AuditEntity audit)
    implements AuditableEntity {

  @Override
  public AuditableEntity withAudit(AuditEntity audit) {
    return new DriverEntity(id, fullName, mobilePhone, location, status, audit);
  }

  public static DriverEntity of(Driver driver) {
    return new DriverEntity(
        Optional.ofNullable(driver.id()).map(Long::valueOf).orElse(null),
        driver.fullName(),
        driver.mobilePhone(),
        Optional.ofNullable(driver.location()).map(LocationEntity::of).orElse(null),
        driver.status(),
        Optional.ofNullable(driver.audit()).map(AuditEntity::of).orElse(null));
  }

  public Driver driver() {
    return new Driver(
        Objects.requireNonNull(id).toString(),
        fullName,
        mobilePhone,
        Optional.ofNullable(location).map(LocationEntity::location).orElse(null),
        status,
        Objects.requireNonNull(audit).audit());
  }
}
