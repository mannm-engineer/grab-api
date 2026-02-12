package com.grab.api.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Driver response DTO")
public record DriverDTO(
    @Schema(description = "Unique identifier of the driver", example = "1")
    String id) {

  public static DriverDTO of(String id) {
    return new DriverDTO(id);
  }
}
