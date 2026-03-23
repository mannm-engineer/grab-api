package com.grab.api.repository;

import com.grab.api.repository.entity.DriverDocumentFileEntity;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

public interface DriverDocumentFileRepository extends Repository<DriverDocumentFileEntity, Long> {

  @Query("SELECT file_url FROM driver_document_file WHERE id = :id")
  Optional<String> findFileUrlById(long id);
}
