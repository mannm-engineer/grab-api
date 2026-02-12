package com.grab.api.service.domain;

import java.time.Instant;

public record Audit(Instant createdAt, String createdBy) {}
