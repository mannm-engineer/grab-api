package com.grab.api.repository.entity;

import com.grab.api.service.domain.ride.Ride;
import com.grab.api.share.enumeration.RideStatus;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Table("ride")
public record RideEntity(
    @Id @Column("id") @Nullable Long id,
    @Column("map_id") UUID mapId,
    @Column("passenger_id") UUID passengerId,

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix = "pickup_")
    LocationEntity pickupLocation,

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix = "dropoff_")
    LocationEntity dropoffLocation,

    @Column("status") RideStatus status) {

  public static RideEntity of(Ride ride) {
    return new RideEntity(
        Optional.ofNullable(ride.id()).map(Long::valueOf).orElse(null),
        UUID.fromString(ride.mapId()),
        UUID.fromString(ride.passengerId()),
        LocationEntity.of(ride.pickupLocation()),
        LocationEntity.of(ride.dropoffLocation()),
        ride.status());
  }
}
