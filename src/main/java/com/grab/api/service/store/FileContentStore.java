package com.grab.api.service.store;

import java.io.InputStream;

public interface FileContentStore {

  InputStream findByUrl(String url);

  String create(String filename, InputStream content);

  void delete(String url);
}
