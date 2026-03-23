package com.grab.api.service;

import static com.grab.api.share.enumeration.DomainEventType.CREATED;
import static com.grab.api.share.enumeration.DomainType.DRIVER;

import com.grab.api.service.domain.driver.DriverCreate;
import com.grab.api.service.domain.event.OutboxEvent;
import com.grab.api.service.domain.file.NewFile;
import com.grab.api.service.exception.InvalidInputException;
import com.grab.api.service.store.DriverStore;
import com.grab.api.service.store.FileContentStore;
import com.grab.api.service.store.OutboxEventStore;
import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverService.class);

  private final DriverStore driverStore;
  private final FileContentStore fileContentStore;
  private final OutboxEventStore outboxEventStore;

  public DriverService(
      DriverStore driverStore,
      FileContentStore fileContentStore,
      OutboxEventStore outboxEventStore) {
    this.driverStore = driverStore;
    this.fileContentStore = fileContentStore;
    this.outboxEventStore = outboxEventStore;
  }

  @Transactional
  public String create(DriverCreate driverCreate, List<NewFile> files) {
    validateFiles(driverCreate, files);

    var filenameToUrl = new LinkedHashMap<String, String>();
    try {
      filenameToUrl.putAll(saveFiles(files));
      var id = driverStore.create(driverCreate.driver(filenameToUrl));

      var event = new OutboxEvent(id, DRIVER, CREATED, id);
      outboxEventStore.create(event);

      return id;
    } catch (Exception e) {
      cleanUpFiles(filenameToUrl);
      throw e;
    }
  }

  private void validateFiles(DriverCreate driverCreate, List<NewFile> files) {
    var metadataFileNames = driverCreate.documents().stream()
        .flatMap(d -> d.filenames().stream())
        .sorted()
        .toList();
    var uploadFileNames = files.stream().map(NewFile::filename).sorted().toList();

    if (!metadataFileNames.equals(uploadFileNames)) {
      throw new InvalidInputException("Uploaded files do not match declared documents. Declared: "
          + metadataFileNames
          + ", uploaded: "
          + uploadFileNames);
    }
  }

  private LinkedHashMap<String, String> saveFiles(List<NewFile> files) {
    var filenameToUrl = new LinkedHashMap<String, String>();

    for (var file : files) {
      var fileName = file.filename();
      var fileUrl = fileContentStore.create(fileName, file.content());

      filenameToUrl.put(fileName, fileUrl);
    }

    return filenameToUrl;
  }

  private void cleanUpFiles(LinkedHashMap<String, String> filenameToUrl) {
    filenameToUrl.values().forEach(url -> {
      try {
        fileContentStore.delete(url);
      } catch (Exception e) {
        LOGGER.error("Failed to delete file {} after driver creation failure", url, e);
      }
    });
  }
}
