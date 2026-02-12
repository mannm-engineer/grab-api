package com.grab.api.repository.entity;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
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
    @Column("date_of_birth") LocalDate dateOfBirth,
    @MappedCollection(idColumn = "driver_id") Set<DriverDocumentEntity> documents,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) @Nullable AuditEntity audit)
    implements AuditableEntity {

  @Override
  public AuditableEntity withAudit(AuditEntity audit) {
    return new DriverEntity(
        id,
        fullName,
        mobilePhone,
        status,
        age,
        rating,
        isVerified,
        balance,
        dateOfBirth,
        documents,
        audit);
  }

  public static DriverEntity of(Driver driver) {
    var documents =
        driver.documents().stream().map(DriverDocumentEntity::of).collect(Collectors.toSet());
    return new DriverEntity(
        Optional.ofNullable(driver.id()).map(Long::valueOf).orElse(null),
        driver.fullName(),
        driver.mobilePhone(),
        driver.status(),
        driver.age(),
        driver.rating(),
        driver.isVerified(),
        driver.balance(),
        driver.dateOfBirth(),
        documents,
        Optional.ofNullable(driver.audit()).map(AuditEntity::of).orElse(null));
  }
}
