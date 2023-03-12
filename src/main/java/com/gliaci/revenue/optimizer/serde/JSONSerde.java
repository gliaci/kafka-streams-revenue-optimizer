package com.gliaci.revenue.optimizer.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

// Copied from
// https://github.com/apache/kafka/blob/3.3/streams/examples/src/main/java/org/apache/kafka/streams/examples/pageview/PageViewTypedDemo.java#L83
public class JSONSerde<T> implements Serializer<T>, Deserializer<T>, Serde<T> {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Class<T> type;

  public JSONSerde(Class<T> type) {
    this.type = type;
  }

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {}

  @Override
  public T deserialize(final String topic, final byte[] data) {
    if (data == null) {
      return null;
    }

    try {
      return OBJECT_MAPPER.readValue(data, type);
    } catch (IOException e) {
      throw new SerializationException(e);
    }
  }

  @Override
  public byte[] serialize(final String topic, final T data) {
    if (data == null) {
      return null;
    }

    try {
      return OBJECT_MAPPER.writeValueAsBytes(data);
    } catch (final Exception e) {
      throw new SerializationException("Error serializing JSON message", e);
    }
  }

  @Override
  public void close() {}

  @Override
  public Serializer<T> serializer() {
    return this;
  }

  @Override
  public Deserializer<T> deserializer() {
    return this;
  }
}
