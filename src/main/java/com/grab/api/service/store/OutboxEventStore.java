package com.grab.api.service.store;

import com.grab.api.service.domain.event.OutboxEvent;

public interface OutboxEventStore {

  void createEvent(OutboxEvent outboxEvent);
}
