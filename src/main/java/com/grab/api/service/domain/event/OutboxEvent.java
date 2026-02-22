package com.grab.api.service.domain.event;

import com.grab.api.share.enumeration.DomainEventType;
import com.grab.api.share.enumeration.DomainType;

public record OutboxEvent(
    String eventKey, DomainType domainType, DomainEventType eventType, String payload) {}
