package com.grab.api.controller;

import com.grab.api.controller.api.RideApi;
import com.grab.api.controller.dto.RideCreateDTO;
import com.grab.api.service.RideService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rides")
public class RideRestController implements RideApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(RideRestController.class);

  private final RideService rideService;

  public RideRestController(RideService rideService) {
    this.rideService = rideService;
  }

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void create(@RequestBody @Valid RideCreateDTO rideCreateDTO) {
    LOGGER.info(
        "Receive request to create ride (passengerId={}, pickupLocation={}, dropoffLocation={})",
        rideCreateDTO.passengerId(),
        rideCreateDTO.pickupLocation(),
        rideCreateDTO.dropoffLocation());
    rideService.create(rideCreateDTO.ride());
  }
}
