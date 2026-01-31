package com.grab.api.controller.dto;

import com.grab.api.service.domain.ride.Ride;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to create a new ride")
public record RideCreateDTO(
    @Schema(
        description = "UUID identifying the map associated with the ride",
        format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank
    String mapId,

    @Schema(
        description = "Unique identifier of the passenger requesting the ride",
        format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank
    String passengerId,

    @Schema(description = "Pickup location of the ride") @NotNull
    LocationDTO pickupLocation,

    @Schema(description = "Dropoff location of the ride") @NotNull
    LocationDTO dropoffLocation) {

  public Ride ride() {
    return Ride.newRide(mapId, passengerId, pickupLocation.location(), dropoffLocation.location());
  }
}
