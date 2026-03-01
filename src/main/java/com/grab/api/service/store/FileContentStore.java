package com.grab.api.service.store;

import java.io.InputStream;

public interface FileContentStore {

  String create(String filename, InputStream content);

  void delete(String url);
}
