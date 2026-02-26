package com.grab.api.controller.dto;

import com.grab.api.service.domain.driver.DriverPatch;
import com.grab.api.share.patch.PatchField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@Schema(
    description = "JSON Merge Patch payload for updating a driver. Currently supports: location")
public record DriverPatchDTO(
    @Schema(description = "Driver location") @Nullable @Valid
    PatchField<@NotNull LocationDTO> location) {

  public DriverPatch driverPatch() {
    return new DriverPatch(Optional.ofNullable(location)
        .map(p -> new PatchField<>(
            Optional.ofNullable(p.value()).map(LocationDTO::location).orElse(null)))
        .orElse(null));
  }
}
