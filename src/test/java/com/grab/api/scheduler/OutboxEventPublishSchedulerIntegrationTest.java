package com.grab.api.scheduler;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

import com.grab.api.integration.ApiTest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.jdbc.Sql;

@ApiTest
class OutboxEventPublishSchedulerIntegrationTest {

  private static final String TOPIC = "driver-events";

  @Autowired
  private OutboxEventPublishScheduler outboxEventPublishScheduler;

  @Autowired
  private JdbcClient jdbcClient;

  @Autowired
  private KafkaAdmin kafkaAdmin;

  @Test
  @Sql(statements = """
    INSERT INTO outbox_event (topic, event_key, event_type, payload, created_at)
    VALUES ('driver-events', '1', 'CREATED', '{"id":"1"}', now()),
           ('driver-events', '2', 'CREATED', '{"id":"2"}', now());
  """)
  void processEvents_eventsExist_publishesToKafkaAndDeletesFromDatabase() {
    // ACT
    outboxEventPublishScheduler.processEvents();

    // ASSERT
    var records = consumeRecords(TOPIC, 2);
    assertThat(records).hasSize(2);

    assertThat(toMap(records.get(0)))
        .usingRecursiveComparison()
        .isEqualTo(new HashMap<String, Object>() {
          {
            put("topic", "driver-events");
            put("key", "1");
            put("value", "{\"id\":\"1\"}");
            put("eventType", "CREATED");
          }
        });

    assertThat(toMap(records.get(1)))
        .usingRecursiveComparison()
        .isEqualTo(new HashMap<String, Object>() {
          {
            put("topic", "driver-events");
            put("key", "2");
            put("value", "{\"id\":\"2\"}");
            put("eventType", "CREATED");
          }
        });

    var eventsAfter = jdbcClient.sql("SELECT * FROM outbox_event").query().listOfRows();
    assertThat(eventsAfter).isEmpty();
  }

  @Test
  void processEvents_noEvents_doesNotPublish() {
    // ARRANGE
    var eventsBefore = jdbcClient.sql("SELECT * FROM outbox_event").query().listOfRows();
    assertThat(eventsBefore).isEmpty();

    // ACT
    outboxEventPublishScheduler.processEvents();

    // ASSERT
    var records = consumeRecords(TOPIC, 0);
    assertThat(records).isEmpty();
  }

  private static Map<String, Object> toMap(ConsumerRecord<String, String> record) {
    return new HashMap<>() {
      {
        put("topic", record.topic());
        put("key", record.key());
        put("value", record.value());
        put("eventType", new String(record.headers().lastHeader("eventType").value()));
      }
    };
  }

  // @spotless:off
  private List<ConsumerRecord<String, String>> consumeRecords(String topic, int expectedCount) {
    var bootstrapServers =
        kafkaAdmin.getConfigurationProperties().get(BOOTSTRAP_SERVERS_CONFIG);

    try (var consumer = new KafkaConsumer<String, String>(Map.of(
        BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers,
        GROUP_ID_CONFIG,                 "test-" + System.nanoTime(),
        AUTO_OFFSET_RESET_CONFIG,        "earliest",
        KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName(),
        VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()))) {

      consumer.subscribe(List.of(topic));

      var records = new ArrayList<ConsumerRecord<String, String>>();
      var deadline = System.currentTimeMillis() + 10_000;
      while (records.size() < expectedCount && System.currentTimeMillis() < deadline) {
        consumer.poll(Duration.ofMillis(500)).forEach(records::add);
      }
      return records;
    }
  }
  // @spotless:on
}
