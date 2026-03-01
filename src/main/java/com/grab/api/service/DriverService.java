package com.grab.api.service;

import com.grab.api.service.domain.driver.DriverCreate;
import com.grab.api.service.domain.file.FileUpload;
import com.grab.api.service.exception.InvalidInputException;
import com.grab.api.service.store.DriverStore;
import com.grab.api.service.store.FileStore;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverService.class);

  private final DriverStore driverStore;
  private final FileStore fileStore;

  public DriverService(DriverStore driverStore, FileStore fileStore) {
    this.driverStore = driverStore;
    this.fileStore = fileStore;
  }

  public String createDriver(DriverCreate driverCreate, List<FileUpload> files) {
    validateFiles(driverCreate, files);

    var filenameToUrl = new LinkedHashMap<String, String>();
    try {
      for (var file : files) {
        filenameToUrl.put(file.filename(), fileStore.createFile(file.filename(), file.content()));
      }
      return driverStore.createDriver(driverCreate.driver(filenameToUrl));
    } catch (Exception e) {
      filenameToUrl.values().forEach(url -> {
        try {
          fileStore.deleteFile(url);
        } catch (Exception deleteEx) {
          LOGGER.warn("Failed to delete file {} after driver creation failure", url, deleteEx);
        }
      });
      throw e;
    }
  }

  private void validateFiles(DriverCreate driverCreate, List<FileUpload> files) {
    var declaredFilenames =
        driverCreate.documents().stream().flatMap(d -> d.filenames().stream()).toList();

    var duplicateDeclared = declaredFilenames.stream()
        .filter(f -> declaredFilenames.stream().filter(f::equals).count() > 1)
        .distinct()
        .toList();
    if (!duplicateDeclared.isEmpty()) {
      throw new InvalidInputException(
          "Duplicate filenames declared across documents: " + duplicateDeclared);
    }

    var uploadedFilenames = files.stream().map(FileUpload::filename).toList();

    var missingFiles =
        declaredFilenames.stream().filter(f -> !uploadedFilenames.contains(f)).toList();
    if (!missingFiles.isEmpty()) {
      throw new InvalidInputException("Missing uploaded files: " + missingFiles);
    }

    var declaredSet = new HashSet<>(declaredFilenames);
    var extraFiles =
        uploadedFilenames.stream().filter(f -> !declaredSet.contains(f)).toList();
    if (!extraFiles.isEmpty()) {
      throw new InvalidInputException("Unexpected uploaded files: " + extraFiles);
    }
  }
}
