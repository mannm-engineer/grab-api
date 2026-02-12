package com.grab.api.repository.entity;

import org.jspecify.annotations.Nullable;

public interface AuditableEntity {

  @Nullable
  AuditEntity audit();

  AuditableEntity withAudit(AuditEntity audit);
}
