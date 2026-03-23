package com.grab.api.repository;

import com.grab.api.service.store.FileStore;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileLocalStore implements FileStore {

  private final Path storageRoot;

  public FileLocalStore(@Value("${app.storage.path:./uploads}") String storagePath) {
    this.storageRoot = Path.of(storagePath);
  }

  @Override
  public InputStream getFile(String url) {
    try {
      return Files.newInputStream(Path.of(url));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + url, e);
    }
  }

  @Override
  public String createFile(String filename, InputStream content) {
    try {
      Files.createDirectories(storageRoot);
      var storedFilename = UUID.randomUUID() + "_" + filename;
      var target = storageRoot.resolve(storedFilename);
      Files.copy(content, target);
      return target.toAbsolutePath().toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create file: " + filename, e);
    }
  }

  @Override
  public void deleteFile(String url) {
    try {
      Files.deleteIfExists(Path.of(url));
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete file: " + url, e);
    }
  }
}
