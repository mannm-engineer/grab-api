package com.grab.api.repository.adapter;

import com.grab.api.repository.FileRepository;
import com.grab.api.repository.entity.FileEntity;
import com.grab.api.service.domain.file.ExistingFile;
import com.grab.api.service.store.FileStore;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FileStoreAdapter implements FileStore {

  private final FileRepository fileRepository;

  public FileStoreAdapter(FileRepository fileRepository) {
    this.fileRepository = fileRepository;
  }

  @Override
  public Optional<ExistingFile> findById(String id) {
    return fileRepository.findById(Long.parseLong(id)).map(FileEntity::existingFile);
  }
}
