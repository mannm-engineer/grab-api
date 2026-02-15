package com.grab.api.repository.entity;

import com.grab.api.service.domain.Audit;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.springframework.data.relational.core.mapping.Column;

public record AuditEntity(
    @Column("created_at") Instant createdAt,
    @Column("created_by") String createdBy,
    @Column("updated_at") @Nullable Instant updatedAt,
    @Column("updated_by") @Nullable String updatedBy) {

  public static AuditEntity of(Audit audit) {
    return new AuditEntity(
        audit.createdAt(), audit.createdBy(), audit.updatedAt(), audit.updatedBy());
  }

  public Audit audit() {
    return new Audit(createdAt, createdBy, updatedAt, updatedBy);
  }
}
