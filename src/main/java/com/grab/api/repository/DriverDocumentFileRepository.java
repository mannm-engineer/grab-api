package com.grab.api.repository;

import com.grab.api.repository.entity.DriverDocumentFileEntity;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface DriverDocumentFileRepository extends Repository<DriverDocumentFileEntity, Long> {

  Optional<DriverDocumentFileEntity> findFileUrlById(Long id);
}
