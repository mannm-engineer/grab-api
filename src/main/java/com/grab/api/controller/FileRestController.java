package com.grab.api.controller;

import com.grab.api.controller.api.FileApi;
import com.grab.api.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@Validated
public class FileRestController implements FileApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileRestController.class);

  private final FileService fileService;

  public FileRestController(FileService fileService) {
    this.fileService = fileService;
  }

  @Override
  @GetMapping("{id}")
  public ResponseEntity<Resource> download(@PathVariable String id) {
    LOGGER.info("Receive request to download file with id={}", id);
    var content = fileService.download(id);
    LOGGER.info("Successfully retrieved file");

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(content));
  }
}
