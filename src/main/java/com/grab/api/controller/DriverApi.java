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
import org.springframework.http.ProblemDetail;
import org.springframework.web.multipart.MultipartFile;

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
  DriverDTO createDriver(
      @Schema(description = "Driver creation payload") @Valid DriverCreateDTO data,
      @NotEmpty List<MultipartFile> files);

  @Operation(summary = "Update driver location")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Driver location updated successfully"),
    @ApiResponse(
        responseCode = "404",
        description = "Driver not found",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)))
  })
  void updateDriverLocation(
      @Parameter(description = "Driver ID", example = "123", required = true) String id,
      @Schema(description = "Driver location update payload") @Valid LocationDTO locationDTO);
}
