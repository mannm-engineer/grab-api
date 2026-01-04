package com.grab.api.controller.dto;

import com.grab.api.service.domain.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "A geographic coordinate represented by latitude and longitude")
public record LocationDTO(
    @Schema(description = "Latitude", example = "1.3521") @NotNull
    Double lat,

    @Schema(description = "Longitude", example = "103.8198") @NotNull
    Double lng) {

  public Location location() {
    return new Location(lat, lng);
  }
}
