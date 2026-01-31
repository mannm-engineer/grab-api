package com.grab.api.service.domain;

import com.grab.api.repository.entity.LocationEntity;

public record Location(Double lat, Double lng) {

  public LocationEntity entity() {
    return new LocationEntity(lat, lng);
  }
}
