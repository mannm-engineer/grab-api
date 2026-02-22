package com.grab.api.repository.entity;

import com.grab.api.share.enumeration.DomainEventType;
import com.grab.api.share.enumeration.DomainType;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("outbox_event")
public record OutboxEventEntity(
    @Id @Column("id") @Nullable Long id,
    @Column("event_key") String eventKey,
    @Column("domain_type") DomainType domainType,
    @Column("event_type") DomainEventType eventType,
    @Column("payload") String payload,
    @Column("created_at") Instant createdAt) {

  public static OutboxEventEntity of(
      String eventKey, DomainType domainType, DomainEventType eventType, String payload) {
    return new OutboxEventEntity(null, eventKey, domainType, eventType, payload, Instant.now());
  }
}
