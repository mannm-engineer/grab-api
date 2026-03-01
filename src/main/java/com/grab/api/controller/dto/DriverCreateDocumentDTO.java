package com.grab.api.controller.dto;

import com.grab.api.service.domain.driver.DriverCreateDocument;
import com.grab.api.service.domain.file.FileCreate;
import com.grab.api.share.enumeration.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Schema(
    name = "DriverCreateDocument",
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

  public DriverCreateDocument driverCreateDocument(List<MultipartFile> uploadedFiles) {
    var files = uploadedFiles.stream().map(DriverCreateDocumentDTO::toFileCreate).toList();
    return DriverCreateDocument.of(type, documentNumber, expiryDate, filenames, files);
  }

  private static FileCreate toFileCreate(MultipartFile uploadedFile) {
    var filename = uploadedFile.getOriginalFilename();
    try {
      return new FileCreate(filename, uploadedFile.getInputStream());
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Failed to read uploaded file: " + filename, e);
    }
  }
}
