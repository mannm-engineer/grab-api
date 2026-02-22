package com.grab.api.service;

import static com.grab.api.share.enumeration.DomainEventType.CREATED;

import com.grab.api.service.domain.driver.DriverCreate;
import com.grab.api.service.domain.event.OutboxEvent;
import com.grab.api.service.domain.file.FileUpload;
import com.grab.api.service.exception.InvalidInputException;
import com.grab.api.service.store.DriverStore;
import com.grab.api.service.store.FileStore;
import com.grab.api.service.store.OutboxEventStore;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverService.class);

  private final DriverStore driverStore;
  private final FileStore fileStore;
  private final OutboxEventStore outboxEventStore;

  private final String driverEventsTopic;

  public DriverService(
      DriverStore driverStore,
      FileStore fileStore,
      OutboxEventStore outboxEventStore,
      @Value("${app.kafka.topics.driver-events}") String driverEventsTopic) {
    this.driverStore = driverStore;
    this.fileStore = fileStore;
    this.outboxEventStore = outboxEventStore;
    this.driverEventsTopic = driverEventsTopic;
  }

  @Transactional
  public String createDriver(DriverCreate driverCreate, List<FileUpload> files) {
    validateFiles(driverCreate, files);

    var filenameToUrl = new LinkedHashMap<String, String>();
    try {
      for (var file : files) {
        filenameToUrl.put(file.filename(), fileStore.createFile(file.filename(), file.content()));
      }
      var id = driverStore.createDriver(driverCreate.driver(filenameToUrl));
      outboxEventStore.createEvent(new OutboxEvent(driverEventsTopic, id, CREATED, id));
      return id;
    } catch (Exception e) {
      filenameToUrl.values().forEach(url -> {
        try {
          fileStore.deleteFile(url);
        } catch (Exception deleteEx) {
          LOGGER.warn("Failed to delete file {} after driver creation failure", url, deleteEx);
        }
      });
      throw e;
    }
  }

  private void validateFiles(DriverCreate driverCreate, List<FileUpload> files) {
    var declaredFilenames =
        driverCreate.documents().stream().flatMap(d -> d.filenames().stream()).toList();

    var duplicateDeclared = declaredFilenames.stream()
        .filter(f -> declaredFilenames.stream().filter(f::equals).count() > 1)
        .distinct()
        .toList();
    if (!duplicateDeclared.isEmpty()) {
      throw new InvalidInputException(
          "Duplicate filenames declared across documents: " + duplicateDeclared);
    }

    var uploadedFilenames = files.stream().map(FileUpload::filename).toList();

    var missingFiles =
        declaredFilenames.stream().filter(f -> !uploadedFilenames.contains(f)).toList();
    if (!missingFiles.isEmpty()) {
      throw new InvalidInputException("Missing uploaded files: " + missingFiles);
    }

    var declaredSet = new HashSet<>(declaredFilenames);
    var extraFiles =
        uploadedFilenames.stream().filter(f -> !declaredSet.contains(f)).toList();
    if (!extraFiles.isEmpty()) {
      throw new InvalidInputException("Unexpected uploaded files: " + extraFiles);
    }
  }
}
