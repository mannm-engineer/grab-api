package com.grab.api.repository.entity;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Column("status") DriverStatus status,
    @Column("age") Integer age,
    @Column("rating") Double rating,
    @Column("is_verified") Boolean isVerified,
    @Column("balance") BigDecimal balance,
    @Column("date_of_birth") LocalDate dateOfBirth) {

  public static DriverEntity of(Driver driver) {
    var information = driver.information();
    return new DriverEntity(
        Optional.ofNullable(information.id()).map(Long::valueOf).orElse(null),
        information.fullName(),
        information.mobilePhone(),
        information.status(),
        information.age(),
        information.rating(),
        information.isVerified(),
        information.balance(),
        information.dateOfBirth());
  }
}
