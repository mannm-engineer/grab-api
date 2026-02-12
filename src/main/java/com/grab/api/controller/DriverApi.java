package com.grab.api.controller;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Drivers", description = "Driver management APIs")
public interface DriverApi {

  @Operation(summary = "Create a driver", description = "Create a new driver")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Driver created successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DriverDTO.class)))
  })
  DriverDTO createDriver(
      @Schema(description = "Driver creation payload") @Valid DriverCreateDTO driverCreateDTO);
}
