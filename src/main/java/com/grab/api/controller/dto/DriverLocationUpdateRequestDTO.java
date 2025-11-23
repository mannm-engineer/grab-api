package com.grab.api.controller.dto;

import com.grab.api.service.domain.DriverLocation;
import jakarta.validation.constraints.NotNull;

public record DriverLocationUpdateRequestDTO(@NotNull Double lat,
  @NotNull Double lng) {

  public DriverLocation location() {
    return new DriverLocation(lat, lng);
  }
}
