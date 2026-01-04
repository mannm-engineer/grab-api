package com.grab.api.controller;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import com.grab.api.controller.dto.LocationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

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
      @Schema(description = "Driver creation payload") @Valid DriverCreateDTO driverData,
      @NotEmpty List<MultipartFile> documentFiles);

  @Operation(
      summary = "Update driver location",
      description = "Update the current latitude and longitude of a driver")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Driver location updated successfully"),
    @ApiResponse(responseCode = "404", description = "Driver not found", content = @Content)
  })
  void updateDriverLocation(
      @Parameter(description = "Driver ID", example = "123", required = true) String id,
      @Schema(description = "Driver location update payload") @Valid LocationDTO locationDTO);
}
