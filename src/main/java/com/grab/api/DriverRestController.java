package com.grab.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("drivers")
public class DriverRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverRestController.class);

  @GetMapping
  public void getDrivers() {
    LOGGER.info("Received request to get drivers.");
  }
}
