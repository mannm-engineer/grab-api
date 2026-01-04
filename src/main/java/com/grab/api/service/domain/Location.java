package com.grab.api.service.domain;

import com.grab.api.repository.entity.LocationEntity;

public record Location(Double lat, Double lng) {

  public static Location of(LocationEntity entity) {
    return new Location(entity.lat(), entity.lng());
  }

  public LocationEntity entity() {
    return new LocationEntity(lat, lng);
  }

  public double distanceTo(Location other) {
    return Math.sqrt(Math.pow(other.lat - lat, 2) + Math.pow(other.lng - lng, 2));
  }
}
