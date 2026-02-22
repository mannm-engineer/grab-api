package com.grab.api.service;

import com.grab.api.service.model.OutboxEvent;

public interface OutboxEventStore {

  void createEvent(OutboxEvent outboxEvent);
}
