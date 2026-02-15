package com.grab.api.repository;

import com.grab.api.repository.entity.AuditEntity;
import com.grab.api.repository.entity.AuditableEntity;
import java.time.Instant;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class GlobalAuditCallback implements BeforeConvertCallback<Object> {

  @Override
  public Object onBeforeConvert(Object entity) {

    if (!(entity instanceof AuditableEntity auditable)) {
      return entity;
    }

    var now = Instant.now();
    var user = "SYSTEM";

    var audit = auditable.audit();

    var updated = audit == null
        ? new AuditEntity(now, user, null, null)
        : new AuditEntity(audit.createdAt(), audit.createdBy(), now, user);

    return auditable.withAudit(updated);
  }
}
