package com.grab.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("drivers")
public class DriverRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverRestController.class);
  private static final AtomicInteger COUNTER = new AtomicInteger(0);

  @GetMapping
  public void getDrivers() throws InterruptedException {
    LOGGER.info("Getting drivers");
    Thread.sleep(Long.MAX_VALUE);
    LOGGER.info("Request call count: {}", COUNTER.incrementAndGet());
  }
}
