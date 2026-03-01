package com.grab.api.service.domain.driver;

import com.grab.api.service.domain.file.FileCreate;
import com.grab.api.service.exception.InvalidInputException;
import com.grab.api.share.enumeration.DocumentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record DriverCreateDocument(
    DocumentType type, String documentNumber, LocalDate expiryDate, List<FileCreate> files) {

  public static DriverCreateDocument of(
      DocumentType type,
      String documentNumber,
      LocalDate expiryDate,
      List<String> filenames,
      List<FileCreate> files) {
    var actualFilenames = files.stream().map(FileCreate::filename).collect(Collectors.toSet());
    if (!actualFilenames.equals(Set.copyOf(filenames))) {
      throw new InvalidInputException("Files provided do not match declared filenames for this document");
    }
    return new DriverCreateDocument(type, documentNumber, expiryDate, files);
  }
}
