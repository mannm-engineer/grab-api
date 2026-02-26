package com.grab.api.controller;

import com.grab.api.controller.dto.DriverCreateDTO;
import com.grab.api.controller.dto.DriverDTO;
import com.grab.api.controller.dto.DriverPatchDTO;
import com.grab.api.service.DriverService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drivers")
public class DriverRestController implements DriverApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverRestController.class);

  private final DriverService driverService;

  public DriverRestController(DriverService driverService) {
    this.driverService = driverService;
  }

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DriverDTO createDriver(@RequestBody DriverCreateDTO driverCreateDTO) {
    LOGGER.info("Receive request to create driver");
    var id = driverService.createDriver(driverCreateDTO.driver());
    LOGGER.info("Driver created with id={}", id);

    return DriverDTO.of(id);
  }

  @Override
  @PatchMapping(value = "{id}", consumes = "application/merge-patch+json")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void patchDriver(
      @PathVariable String id, @RequestBody @Valid DriverPatchDTO driverPatchDTO) {
    LOGGER.info("Receive request to patch driver {}", id);
    driverService.patchDriver(id, driverPatchDTO.driverPatch());
  }
}
