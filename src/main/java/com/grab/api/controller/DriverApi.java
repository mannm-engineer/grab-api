package com.grab.api.controller;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import com.grab.api.controller.dto.DriverPatchDTO;
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
      summary = "Patch a driver",
      description =
          "Apply a JSON Merge Patch to update driver fields. Currently supports: location")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Driver patched successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid patch request", content = @Content),
    @ApiResponse(responseCode = "404", description = "Driver not found", content = @Content)
  })
  void patchDriver(
      @Parameter(description = "Driver ID", example = "123", required = true) String id,
      @Schema(description = "JSON Merge Patch payload") @Valid DriverPatchDTO driverPatchDTO);
}
