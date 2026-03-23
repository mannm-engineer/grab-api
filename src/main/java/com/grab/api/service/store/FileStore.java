package com.grab.api.service.store;

import com.grab.api.service.domain.file.ExistingFile;
import java.util.Optional;

public interface FileStore {

  Optional<ExistingFile> findById(String id);
}
