package com.grab.api.controller;

import com.grab.api.controller.dto.RideCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Rides", description = "Ride management APIs")
public interface RideApi {

  @Operation(summary = "Create a ride")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Ride created successfully",
        content = @Content)
  })
  void createRide(@Valid RideCreateDTO rideCreateDTO);
}
