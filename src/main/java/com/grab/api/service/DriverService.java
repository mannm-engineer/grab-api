package com.grab.api.service;

import static com.grab.api.share.enumeration.DomainEventType.CREATED;
import static com.grab.api.share.enumeration.DomainType.DRIVER;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.driver.DriverCreate;
import com.grab.api.service.domain.event.OutboxEvent;
import com.grab.api.service.store.DriverStore;
import com.grab.api.service.store.FileContentStore;
import com.grab.api.service.store.OutboxEventStore;
import java.util.LinkedHashMap;
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
  public String create(DriverCreate driverCreate) {
    var uploadedFileUrls = new LinkedHashMap<String, String>();
    try {
      for (var document : driverCreate.documents()) {
        for (var file : document.files()) {
          var fileUrl = fileContentStore.create(file.filename(), file.content());
          uploadedFileUrls.put(file.filename(), fileUrl);
        }
      }

      var driver = Driver.create(driverCreate, uploadedFileUrls);
      var id = driverStore.create(driver);

      var event = new OutboxEvent(id, DRIVER, CREATED, id);
      outboxEventStore.create(event);

      return id;
    } catch (Exception e) {
      cleanUpFiles(uploadedFileUrls);
      throw e;
    }
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
