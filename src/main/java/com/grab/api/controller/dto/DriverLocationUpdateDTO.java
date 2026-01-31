package com.grab.api.controller.dto;

import com.grab.api.service.domain.Location;
import jakarta.validation.constraints.NotNull;

public record DriverLocationUpdateDTO(
    @NotNull Double lat, @NotNull Double lng) {

  public Location location() {
    return new Location(lat, lng);
  }
}
