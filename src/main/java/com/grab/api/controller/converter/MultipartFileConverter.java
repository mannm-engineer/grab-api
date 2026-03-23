package com.grab.api.controller.converter;

import com.grab.api.service.domain.file.NewFile;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public final class MultipartFileConverter {

  private MultipartFileConverter() {}

  public static List<NewFile> toFileUploads(List<MultipartFile> files) {
    return files.stream().map(MultipartFileConverter::toFileUpload).toList();
  }

  private static NewFile toFileUpload(MultipartFile file) {
    var filename = file.getOriginalFilename();

    if (filename == null || filename.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Uploaded file is missing a filename");
    }

    try {
      return new NewFile(filename, file.getInputStream());
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Failed to read uploaded file: " + filename, e);
    }
  }
}
