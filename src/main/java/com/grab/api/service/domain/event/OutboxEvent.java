package com.grab.api.service.domain.event;

import com.grab.api.share.enumeration.DomainEventType;

public record OutboxEvent(
    String topic, String eventKey, DomainEventType eventType, String payload) {}
