package com.grab.api.repository.entity;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.driver.DriverInformation;
import com.grab.api.share.enumeration.DriverStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
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

    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL, prefix = "location_") @Nullable
    LocationEntity location,

    @Column("status") DriverStatus status,
    @Column("age") Integer age,
    @Column("rating") Double rating,
    @Column("is_verified") Boolean isVerified,
    @Column("balance") BigDecimal balance,
    @Column("birth_date") LocalDate birthDate,
    @MappedCollection(idColumn = "driver_id") Set<DriverDocumentEntity> documents,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL) @Nullable AuditEntity audit)
    implements AuditableEntity {

  @Override
  public AuditableEntity withAudit(AuditEntity audit) {
    return new DriverEntity(
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

  public static DriverEntity of(Driver driver) {
    var information = driver.information();
    var documents =
        information.documents().stream().map(DriverDocumentEntity::of).collect(Collectors.toSet());
    return new DriverEntity(
        Optional.ofNullable(information.id()).map(Long::valueOf).orElse(null),
        information.fullName(),
        information.mobilePhone(),
        Optional.ofNullable(information.location()).map(LocationEntity::of).orElse(null),
        information.status(),
        information.age(),
        information.rating(),
        information.isVerified(),
        information.balance(),
        information.birthDate(),
        documents,
        Optional.ofNullable(information.audit()).map(AuditEntity::of).orElse(null));
  }

  public DriverInformation information() {
    return new DriverInformation(
        Objects.requireNonNull(id).toString(),
        fullName,
        mobilePhone,
        Optional.ofNullable(location).map(LocationEntity::location).orElse(null),
        status,
        age,
        rating,
        isVerified,
        balance,
        birthDate,
        documents.stream().map(DriverDocumentEntity::driverDocument).toList(),
        Objects.requireNonNull(audit).audit());
  }

  public Driver driver() {
    return Driver.restore(information());
  }
}
