package com.grab.api.repository;

import com.grab.api.repository.entity.DriverDocumentFileEntity;
import com.grab.api.repository.entity.DriverEntity;
import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.store.DriverStore;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DriverDatabaseStore implements DriverStore {

  private final DriverRepository driverRepository;
  private final DriverDocumentFileRepository driverDocumentFileRepository;

  public DriverDatabaseStore(
      DriverRepository driverRepository,
      DriverDocumentFileRepository driverDocumentFileRepository) {
    this.driverRepository = driverRepository;
    this.driverDocumentFileRepository = driverDocumentFileRepository;
  }

  @Override
  public String createDriver(Driver driver) {
    var created = driverRepository.save(DriverEntity.of(driver));
    return Objects.requireNonNull(created.id()).toString();
  }

  @Override
  public Optional<String> getDocumentFileUrl(String id) {
    return driverDocumentFileRepository
        .findFileUrlById(Long.parseLong(id))
        .map(DriverDocumentFileEntity::fileUrl);
  }
}
