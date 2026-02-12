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

    var audit = auditable.audit();

    if (audit != null) {
      return entity;
    }

    return auditable.withAudit(new AuditEntity(Instant.now(), "SYSTEM"));
  }
}
