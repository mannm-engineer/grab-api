package com.grab.api.service;

import com.grab.api.repository.DriverRepository;
import com.grab.api.service.domain.DriverLocation;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

  private final DriverRepository driverRepository;

  public DriverService(DriverRepository driverRepository) {
    this.driverRepository = driverRepository;
  }

  public void updateDriverLocation(Long id, DriverLocation newLocation) {
    int updatedRecords = driverRepository.updateLocation(id, newLocation.lat(), newLocation.lng());
    if (updatedRecords == 0) {
      throw new IllegalArgumentException("Driver with id " + id + " not found");
    }
  }
}
