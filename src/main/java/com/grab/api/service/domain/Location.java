package com.grab.api.service.domain;

public record Location(Double lat, Double lng) {

  public double distanceTo(Location other) {
    return Math.sqrt(Math.pow(other.lat - lat, 2) + Math.pow(other.lng - lng, 2));
  }
}
