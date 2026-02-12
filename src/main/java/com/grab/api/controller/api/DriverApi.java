package com.grab.api.controller.api;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;

@Tag(name = "Drivers", description = "Driver management APIs")
public interface DriverApi {

  @Operation(summary = "Create a driver")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Driver created successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DriverDTO.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Driver already exists",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  DriverDTO create(
      @Schema(description = "Driver creation payload") @Valid DriverCreateDTO driverCreateDTO);
}
