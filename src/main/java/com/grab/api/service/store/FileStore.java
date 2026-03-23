package com.grab.api.service.store;

import java.io.InputStream;

public interface FileStore {

  InputStream getFile(String url);

  String createFile(String filename, InputStream content);

  void deleteFile(String url);
}
