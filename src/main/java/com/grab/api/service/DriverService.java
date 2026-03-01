package com.grab.api.service;

import com.grab.api.service.domain.driver.Driver;
import com.grab.api.service.domain.driver.DriverCreate;
import com.grab.api.service.store.DriverStore;
import com.grab.api.service.store.FileContentStore;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverService.class);

  private final DriverStore driverStore;
  private final FileContentStore fileContentStore;

  public DriverService(DriverStore driverStore, FileContentStore fileContentStore) {
    this.driverStore = driverStore;
    this.fileContentStore = fileContentStore;
  }

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
      return driverStore.create(driver);
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
