package com.fashionpin.authservice.service;

import com.fashionpin.authservice.entity.OutboxEvent;
import com.fashionpin.authservice.kafka.KafkaProducerService;
import com.fashionpin.authservice.repository.OutboxEventRepository;
import com.fashionpin.common.kafka.KafkaTopics;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
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
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                String topic = KafkaTopics.USER_REGISTERED_V1;
                kafkaProducerService.publish(topic, event.getAggregateId(), event.getPayload());
                event.setStatus("PROCESSED");
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event id={}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
