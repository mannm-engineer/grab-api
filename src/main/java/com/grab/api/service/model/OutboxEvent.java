package com.grab.api.service.model;

public record OutboxEvent(String topic, String eventKey, String eventType, String payload) {}
