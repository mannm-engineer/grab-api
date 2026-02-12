package com.grab.api.repository.entity;

import com.grab.api.service.domain.Audit;
import java.time.Instant;
import org.springframework.data.relational.core.mapping.Column;

public record AuditEntity(
    @Column("created_at") Instant createdAt,
    @Column("created_by") String createdBy) {

  public static AuditEntity of(Audit audit) {
    return new AuditEntity(audit.createdAt(), audit.createdBy());
  }
}
