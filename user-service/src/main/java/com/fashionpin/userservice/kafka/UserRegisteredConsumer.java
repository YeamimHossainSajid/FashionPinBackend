package com.fashionpin.userservice.kafka;

import com.fashionpin.common.event.UserCreatedEvent;
import com.fashionpin.common.event.UserRegisteredEvent;
import com.fashionpin.common.kafka.KafkaTopics;
import com.fashionpin.userservice.entity.ProcessedEvent;
import com.fashionpin.userservice.entity.User;
import com.fashionpin.userservice.repository.ProcessedEventRepository;
import com.fashionpin.userservice.repository.UserRepository;
import com.fashionpin.userservice.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final UserRepository userRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    public UserRegisteredConsumer(
            UserRepository userRepository,
            ProcessedEventRepository processedEventRepository,
            OutboxService outboxService,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.processedEventRepository = processedEventRepository;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED_V1, groupId = "${spring.kafka.consumer.group-id:user-service}")
    @Transactional
    public void consume(String message) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to parse UserRegisteredEvent message: {}", message, e);
        }
    }

    @Transactional
    public void processEvent(UserRegisteredEvent event) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("Duplicate event received eventId={}, skipping processing", event.getEventId());
            return;
        }

        log.info("Processing UserRegisteredEvent eventId={} for userId={}", event.getEventId(), event.getUserId());

        if (!userRepository.existsById(event.getUserId())) {
            User user = User.create(event.getUserId(), event.getEmail());
            userRepository.save(user);

            UserCreatedEvent createdEvent = UserCreatedEvent.create(user.getId(), user.getEmail(), event.getCorrelationId());
            outboxService.saveEvent("User", user.getId(), "UserCreated", createdEvent);
        }

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .processedAt(Instant.now())
                .build();
        processedEventRepository.save(processedEvent);
    }
}
