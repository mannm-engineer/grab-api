package com.grab.api.repository.entity;

import com.grab.api.service.domain.driver.DriverDocument;
import com.grab.api.share.enumeration.DocumentType;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("driver_document")
public record DriverDocumentEntity(
    @Id @Column("id") @Nullable Long id,
    @Column("type") DocumentType type,
    @Column("document_number") String documentNumber,
    @Column("expiry_date") LocalDate expiryDate,
    @MappedCollection(idColumn = "document_id") Set<DriverDocumentFileEntity> files) {

  public static DriverDocumentEntity of(DriverDocument document) {
    var files =
        document.fileUrls().stream().map(DriverDocumentFileEntity::of).collect(Collectors.toSet());
    return new DriverDocumentEntity(
        null, document.type(), document.documentNumber(), document.expiryDate(), files);
  }

  public DriverDocument driverDocument() {
    var fileUrls =
        files.stream().map(DriverDocumentFileEntity::fileUrl).collect(Collectors.toList());
    return new DriverDocument(type, documentNumber, expiryDate, fileUrls);
  }
}
