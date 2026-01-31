package com.grab.api.repository.entity;

import com.grab.api.service.domain.Location;
import org.springframework.data.relational.core.mapping.Column;

public record LocationEntity(
    @Column("lat") Double lat, @Column("lng") Double lng) {

  public static LocationEntity of(Location location) {
    return new LocationEntity(location.lat(), location.lng());
  }

  public Location location() {
    return new Location(lat, lng);
  }
}
