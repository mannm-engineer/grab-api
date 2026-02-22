package com.grab.api.scheduler;

import com.grab.api.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxEventPublishScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxEventPublishScheduler.class);

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;
  /**
   * Max events to fetch per poll. Without this limit, a single instance would {@code SELECT ... FOR
   * UPDATE} every row in the outbox table, locking them all for the duration of the transaction.
   * Since each event is published to Kafka synchronously ({@code .get()}), the transaction — and
   * therefore the locks — stay open until the entire batch finishes. This causes three problems:
   *
   * <ol>
   *   <li>Other instances are starved — {@code SKIP LOCKED} makes them skip all locked rows, so
   *       they find nothing to process and sit idle.
   *   <li>Long-held row locks increase the chance of lock timeouts and contention with the writers
   *       that insert new outbox events.
   *   <li>A large batch amplifies the impact of a single Kafka failure — all remaining events in
   *       the batch are delayed until the next poll cycle.
   * </ol>
   */
  private final int batchSize;

  public OutboxEventPublishScheduler(
      OutboxEventRepository outboxEventRepository,
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${outbox.batch-size}") int batchSize) {
    this.outboxEventRepository = outboxEventRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.batchSize = batchSize;
  }

  /**
   * Poll outbox events and publish them to Kafka.
   *
   * <p>Uses {@code SELECT ... FOR UPDATE SKIP LOCKED} so multiple application instances can run
   * this poller concurrently without processing the same events. Each instance locks only the rows
   * it fetches; others skip those rows and pick up different ones.
   *
   * <p>The Kafka send is synchronous ({@code .get()}) to ensure at-least-once delivery — the event
   * is only deleted after Kafka acknowledges receipt. Failures are caught per-event so one bad
   * message does not block the rest of the batch.
   */
  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void processEvents() {
    var events = outboxEventRepository.findEventsForProcessing(batchSize);
    for (var event : events) {
      try {
        var message = MessageBuilder.withPayload(event.payload())
            .setHeader(KafkaHeaders.TOPIC, event.topic())
            .setHeader(KafkaHeaders.KEY, event.eventKey())
            .setHeader("eventType", event.eventType())
            .build();
        kafkaTemplate.send(message).get();
        outboxEventRepository.delete(event);
        LOGGER.info(
            "Published and deleted outbox event id={} to topic={}", event.id(), event.topic());
      } catch (Exception e) {
        LOGGER.error(
            "Failed to publish outbox event id={} to topic={}", event.id(), event.topic(), e);
      }
    }
  }
}
