package com.grab.api.service;

import com.grab.api.service.exception.DomainNotFoundException;
import com.grab.api.service.store.FileContentStore;
import com.grab.api.service.store.FileStore;
import java.io.InputStream;
import org.springframework.stereotype.Service;

@Service
public class FileService {

  private final FileStore fileStore;
  private final FileContentStore fileContentStore;

  public FileService(FileStore fileStore, FileContentStore fileContentStore) {
    this.fileStore = fileStore;
    this.fileContentStore = fileContentStore;
  }

  public InputStream download(String id) {
    var file = fileStore.findById(id).orElseThrow(() -> new DomainNotFoundException(id));
    return fileContentStore.findByUrl(file.url());
  }
}
