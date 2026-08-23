package com.fashionpin.mediaservice.service;

import com.fashionpin.common.kafka.KafkaTopics;
import com.fashionpin.mediaservice.entity.OutboxEvent;
import com.fashionpin.mediaservice.kafka.KafkaProducerService;
import com.fashionpin.mediaservice.repository.OutboxEventRepository;
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
            case "MediaCreated" -> KafkaTopics.MEDIA_CREATED_V1;
            case "MediaReady" -> KafkaTopics.MEDIA_READY_V1;
            case "MediaDeleted" -> KafkaTopics.MEDIA_DELETED_V1;
            default -> KafkaTopics.MEDIA_EVENTS;
        };
    }
}
