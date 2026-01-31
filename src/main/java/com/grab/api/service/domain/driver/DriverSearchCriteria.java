package com.grab.api.service.domain.driver;

import com.grab.api.share.enumeration.DriverStatus;
import org.jspecify.annotations.Nullable;

public record DriverSearchCriteria(
    @Nullable String mapId, @Nullable DriverStatus status, boolean hasLocation) {}
