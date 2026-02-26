package com.grab.api.service.domain.driver;

import com.grab.api.service.domain.Location;
import com.grab.api.share.patch.PatchField;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record DriverPatch(@Nullable PatchField<Location> location) {

  public Driver applyTo(Driver driver) {
    var updated = driver;
    if (location != null) {
      updated = updated.withLocation(Objects.requireNonNull(location.value()));
    }
    return updated;
  }
}
