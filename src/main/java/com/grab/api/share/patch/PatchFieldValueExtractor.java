package com.grab.api.share.patch;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

public class PatchFieldValueExtractor implements ValueExtractor<PatchField<@ExtractedValue ?>> {

  @Override
  public void extractValues(PatchField<?> originalValue, ValueReceiver receiver) {
    receiver.value(null, originalValue.value());
  }
}
