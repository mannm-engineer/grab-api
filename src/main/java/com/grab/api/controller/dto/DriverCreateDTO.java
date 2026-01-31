package com.grab.api.controller.dto;

import com.grab.api.service.domain.driver.Driver;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload to create a new driver")
public record DriverCreateDTO(
    @Schema(
        description = "UUID identifying the map associated with the driver",
        format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank
    String mapId,

    @Schema(description = "Full name of the driver", example = "John Doe") @NotBlank
    String fullName,

    @Schema(
        description = "Mobile phone number of the driver in E.164 format",
        example = "+6591234567")
    @NotBlank
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "must be a valid E.164 phone number (e.g., +6591234567)")
    String mobilePhone) {

  public Driver driver() {
    return Driver.newDriver(mapId, fullName, mobilePhone);
  }
}
