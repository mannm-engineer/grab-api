package com.grab.api.repository.adapter;

import com.grab.api.service.store.FileContentStore;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileContentStoreAdapter implements FileContentStore {

  private final Path storageRoot;

  public FileContentStoreAdapter(@Value("${app.storage.path:./uploads}") String storagePath) {
    this.storageRoot = Path.of(storagePath);
  }

  @Override
  public String create(String filename, InputStream content) {
    try {
      var dateDir = storageRoot.resolve(java.time.LocalDate.now().toString());
      Files.createDirectories(dateDir);
      var storedFilename = UUID.randomUUID() + "_" + filename;
      var target = dateDir.resolve(storedFilename);
      Files.copy(content, target);
      return target.toAbsolutePath().toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create file: " + filename, e);
    }
  }

  @Override
  public void delete(String url) {
    try {
      Files.deleteIfExists(Path.of(url));
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete file: " + url, e);
    }
  }
}
