package com.grab.api.repository;

import com.grab.api.repository.entity.OutboxEventEntity;
import com.grab.api.service.domain.event.OutboxEvent;
import com.grab.api.service.store.OutboxEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventDatabaseStore implements OutboxEventStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxEventDatabaseStore.class);

  private final OutboxEventRepository outboxEventRepository;

  public OutboxEventDatabaseStore(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Override
  public void createEvent(OutboxEvent outboxEvent) {
    var event = OutboxEventEntity.of(
        outboxEvent.topic(),
        outboxEvent.eventKey(),
        outboxEvent.eventType().name(),
        outboxEvent.payload());
    outboxEventRepository.save(event);
    LOGGER.info(
        "Saved outbox event type={} key={} to topic={}",
        outboxEvent.eventType(),
        outboxEvent.eventKey(),
        outboxEvent.topic());
  }
}
