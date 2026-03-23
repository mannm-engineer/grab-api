package com.grab.api.service.store;

import com.grab.api.service.domain.driver.Driver;
import java.util.Optional;

public interface DriverStore {

  String createDriver(Driver driver);

  Optional<String> getDocumentFileUrl(String id);
}
