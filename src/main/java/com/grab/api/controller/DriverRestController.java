package com.grab.api.controller;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import com.grab.api.controller.dto.DriverPatchDTO;
import com.grab.api.service.DriverService;
import com.grab.api.service.FileStore;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/drivers")
@Validated
public class DriverRestController implements DriverApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverRestController.class);

  private final DriverService driverService;
  private final FileStore fileStore;

  public DriverRestController(DriverService driverService, FileStore fileStore) {
    this.driverService = driverService;
    this.fileStore = fileStore;
  }

  @Override
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public DriverDTO createDriver(
      @RequestPart("driverData") DriverCreateDTO driverData,
      @RequestPart("documentFiles") List<MultipartFile> documentFiles) {

    LOGGER.info("Receive request to create driver");

    if (documentFiles.size() != driverData.documents().size()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Number of uploaded files (%d) must match number of documents (%d)"
              .formatted(documentFiles.size(), driverData.documents().size()));
    }

    var storedUrls = documentFiles.stream().map(this::storeFile).toList();
    var id = driverService.createDriver(driverData.driver(storedUrls));
    LOGGER.info("Driver created with id={}", id);

    return DriverDTO.of(id);
  }

  private String storeFile(MultipartFile file) {
    var filename = Objects.requireNonNull(file.getOriginalFilename());
    try {
      return fileStore.createFile(filename, file.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read uploaded file: " + filename, e);
    }
  }

  @Override
  @PatchMapping(value = "{id}", consumes = "application/merge-patch+json")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void patchDriver(@PathVariable String id, @RequestBody DriverPatchDTO driverPatchDTO) {
    LOGGER.info("Receive request to patch driver {}", id);
    driverService.patchDriver(id, driverPatchDTO.driverPatch());
  }
}
