package com.grab.api.service.domain;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record Audit(
    Instant createdAt,
    String createdBy,
    @Nullable Instant updatedAt,
    @Nullable String updatedBy) {}
