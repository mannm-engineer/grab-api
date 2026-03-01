package com.grab.api.repository.entity;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("driver_document_file")
public record DriverDocumentFileEntity(
    @Id @Column("id") @Nullable Long id, @Column("file_url") String fileUrl) {

  public static DriverDocumentFileEntity of(String fileUrl) {
    return new DriverDocumentFileEntity(null, fileUrl);
  }
}
