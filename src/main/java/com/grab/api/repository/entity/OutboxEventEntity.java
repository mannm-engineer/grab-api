package com.grab.api.repository.entity;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("outbox_event")
public record OutboxEventEntity(
    @Id @Column("id") @Nullable Long id,
    @Column("topic") String topic,
    @Column("event_key") String eventKey,
    @Column("event_type") String eventType,
    @Column("payload") String payload,
    @Column("created_at") Instant createdAt) {

  public static OutboxEventEntity of(
      String topic, String eventKey, String eventType, String payload) {
    return new OutboxEventEntity(null, topic, eventKey, eventType, payload, Instant.now());
  }
}
