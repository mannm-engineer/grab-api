package com.grab.api.controller;

import static com.grab.api.controller.converter.MultipartFileConverter.toFileUploads;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import com.grab.api.controller.dto.LocationDTO;
import com.grab.api.service.DriverService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/drivers")
@Validated
public class DriverRestController implements DriverApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverRestController.class);

  private final DriverService driverService;

  public DriverRestController(DriverService driverService) {
    this.driverService = driverService;
  }

  @Override
  @GetMapping("/document-files/{fileId}")
  public ResponseEntity<Resource> downloadDocumentFile(@PathVariable String fileId) {

    LOGGER.info("Receive request to download file with id={}", fileId);

    var content = driverService.getDocumentFile(fileId);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(content));
  }

  @Override
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public DriverDTO createDriver(
      @RequestPart("data") DriverCreateDTO data, @RequestPart("files") List<MultipartFile> files) {

    LOGGER.info("Receive request to create driver");
    var id = driverService.createDriver(data.driverCreate(), toFileUploads(files));
    LOGGER.info("Driver created with id={}", id);

    return DriverDTO.of(id);
  }

  @Override
  @PutMapping("{id}/location")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateDriverLocation(@PathVariable String id, @RequestBody LocationDTO locationDTO) {
    LOGGER.info(
        "Receive request to update location of driver {} (lat={}, lng={})",
        id,
        locationDTO.lat(),
        locationDTO.lng());
    driverService.updateDriverLocation(id, locationDTO.location());
  }
}
