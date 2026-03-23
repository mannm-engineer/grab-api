package com.grab.api.repository.entity;

import com.grab.api.service.domain.file.ExistingFile;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("file")
public record FileEntity(
    @Id @Column("id") @Nullable Long id, @Column("file_url") String fileUrl) {

  public static FileEntity of(String fileUrl) {
    return new FileEntity(null, fileUrl);
  }

  public ExistingFile existingFile() {
    return new ExistingFile(Objects.requireNonNull(id).toString(), fileUrl);
  }
}
