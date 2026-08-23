package com.fashionpin.productservice.service;

import com.fashionpin.common.kafka.KafkaTopics;
import com.fashionpin.productservice.entity.OutboxEvent;
import com.fashionpin.productservice.kafka.KafkaProducerService;
import com.fashionpin.productservice.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;

    public OutboxPublisherScheduler(
            OutboxEventRepository outboxEventRepository,
            KafkaProducerService kafkaProducerService) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                String topic = resolveTopic(event.getEventType());
                kafkaProducerService.publish(topic, event.getAggregateId(), event.getPayload());
                event.setStatus("PROCESSED");
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);
                log.info("Successfully published outbox event id={} type={} to topic={}", event.getId(), event.getEventType(), topic);
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}: {}", event.getId(), e.getMessage());
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "ProductCreated" -> KafkaTopics.PRODUCT_CREATED_V1;
            case "ProductUpdated" -> KafkaTopics.PRODUCT_UPDATED_V1;
            case "ProductDeleted" -> KafkaTopics.PRODUCT_DELETED_V1;
            default -> KafkaTopics.PRODUCT_EVENTS;
        };
    }
}
