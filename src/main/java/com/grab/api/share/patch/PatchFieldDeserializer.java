package com.grab.api.share.patch;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

public class PatchFieldDeserializer extends StdDeserializer<PatchField<?>> {

  private final @Nullable ValueDeserializer<Object> valueDeserializer;

  public PatchFieldDeserializer() {
    super(PatchField.class);
    this.valueDeserializer = null;
  }

  private PatchFieldDeserializer(ValueDeserializer<Object> valueDeserializer) {
    super(PatchField.class);
    this.valueDeserializer = valueDeserializer;
  }

  @Override
  public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
    var innerType = property.getType().containedType(0);
    ValueDeserializer<Object> deser = ctxt.findContextualValueDeserializer(innerType, property);
    return new PatchFieldDeserializer(deser);
  }

  @Override
  public PatchField<?> deserialize(JsonParser p, DeserializationContext ctxt) {
    Object value = Objects.requireNonNull(valueDeserializer).deserialize(p, ctxt);
    return new PatchField<>(value);
  }

  @Override
  public @Nullable Object getAbsentValue(DeserializationContext ctxt) {
    return null;
  }

  @Override
  public @Nullable PatchField<?> getNullValue(DeserializationContext ctxt) {
    return new PatchField<>(null);
  }
}
