package com.grab.api.share.patch;

import org.springframework.stereotype.Component;
import tools.jackson.databind.module.SimpleModule;

@Component
public class PatchFieldModule extends SimpleModule {

  public PatchFieldModule() {
    super("PatchFieldModule");
    addDeserializer(PatchField.class, new PatchFieldDeserializer());
  }
}
