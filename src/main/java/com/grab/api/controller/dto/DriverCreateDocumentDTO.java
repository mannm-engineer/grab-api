package com.grab.api.controller.dto;

import com.grab.api.service.domain.driver.DriverCreateDocument;
import com.grab.api.share.enumeration.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(
    description =
        "Driver document metadata (the actual file is uploaded as a separate multipart part)")
public record DriverCreateDocumentDTO(
    @Schema(description = "Type of document", example = "DRIVERS_LICENSE") @NotNull
    DocumentType type,

    @Schema(description = "Document identification number", example = "S1234567A") @NotBlank
    String documentNumber,

    @Schema(description = "Document expiry date", example = "2030-01-01") @NotNull @Future
    LocalDate expiryDate,

    @Schema(description = "Unique filenames of the files uploaded for this document") @NotEmpty
    List<@NotBlank String> filenames) {

  public DriverCreateDocument driverCreateDocument() {
    return new DriverCreateDocument(type, documentNumber, expiryDate, filenames);
  }
}
