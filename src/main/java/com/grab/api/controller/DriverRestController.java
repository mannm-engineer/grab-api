package com.grab.api.controller;

import static com.grab.api.controller.converter.MultipartFileConverter.toFileUploads;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import com.grab.api.controller.dto.DriverLocationUpdateDTO;
import com.grab.api.service.DriverService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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
  public void updateDriverLocation(
      @PathVariable String id, @RequestBody DriverLocationUpdateDTO driverLocationUpdateDTO) {
    LOGGER.info(
        "Receive request to update location of driver {} (lat={}, lng={})",
        id,
        driverLocationUpdateDTO.lat(),
        driverLocationUpdateDTO.lng());
    driverService.updateDriverLocation(id, driverLocationUpdateDTO.location());
  }
}
