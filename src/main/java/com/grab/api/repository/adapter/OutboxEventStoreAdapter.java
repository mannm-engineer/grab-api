package com.grab.api.repository.adapter;

import com.grab.api.repository.OutboxEventRepository;
import com.grab.api.repository.entity.OutboxEventEntity;
import com.grab.api.service.domain.event.OutboxEvent;
import com.grab.api.service.store.OutboxEventStore;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventStoreAdapter implements OutboxEventStore {

  private final OutboxEventRepository outboxEventRepository;

  public OutboxEventStoreAdapter(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Override
  public void create(OutboxEvent outboxEvent) {
    var event = OutboxEventEntity.of(
        outboxEvent.eventKey(),
        outboxEvent.domainType(),
        outboxEvent.eventType(),
        outboxEvent.payload());
    outboxEventRepository.save(event);
  }
}
