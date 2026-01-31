package com.grab.api.repository.entity;

import com.grab.api.service.domain.driver.DriverDocument;
import com.grab.api.share.enumeration.DocumentType;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("driver_document")
public record DriverDocumentEntity(
    @Id @Column("id") @Nullable Long id,
    @Column("type") DocumentType type,
    @Column("document_number") String documentNumber,
    @Column("expiry_date") LocalDate expiryDate,
    @Column("file_url") String fileUrl) {

  public static DriverDocumentEntity of(DriverDocument document) {
    return new DriverDocumentEntity(
        null,
        document.type(),
        document.documentNumber(),
        document.expiryDate(),
        document.fileUrl());
  }

  public DriverDocument driverDocument() {
    return new DriverDocument(type, documentNumber, expiryDate, fileUrl);
  }
}
