package com.grab.api.controller;

import com.grab.api.controller.dto.DriverLocationUpdateRequestDTO;
import com.grab.api.service.DriverService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("drivers")
public class DriverRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverRestController.class);

  private final DriverService driverService;

  public DriverRestController(DriverService driverService) {
    this.driverService = driverService;
  }

  @PutMapping("{id}/location")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateDriverLocation(@PathVariable String id,
    @RequestBody @Valid DriverLocationUpdateRequestDTO request) {

    LOGGER.info(
      "Receive request to update location of driver {} (lat={}, lng={})", id, request.location().lat(), request
        .location()
        .lng()
    );
    driverService.updateDriverLocation(Long.valueOf(id), request.location());
  }
}
