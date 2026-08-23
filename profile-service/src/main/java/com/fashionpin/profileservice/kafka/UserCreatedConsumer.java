package com.fashionpin.profileservice.kafka;

import com.fashionpin.common.event.UserCreatedEvent;
import com.fashionpin.common.kafka.KafkaTopics;
import com.fashionpin.profileservice.entity.ProcessedEvent;
import com.fashionpin.profileservice.entity.Profile;
import com.fashionpin.profileservice.repository.ProcessedEventRepository;
import com.fashionpin.profileservice.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedConsumer.class);

    private final ProfileRepository profileRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public UserCreatedConsumer(
            ProfileRepository profileRepository,
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.USER_CREATED_V1, groupId = "${spring.kafka.consumer.group-id:profile-service}")
    @Transactional
    public void consume(String message) {
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            processEvent(event);
        } catch (Exception e) {
            log.error("Failed to parse UserCreatedEvent message: {}", message, e);
        }
    }

    @Transactional
    public void processEvent(UserCreatedEvent event) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("Duplicate event received eventId={}, skipping processing", event.getEventId());
            return;
        }

        log.info("Processing UserCreatedEvent eventId={} for userId={}", event.getEventId(), event.getUserId());

        if (!profileRepository.existsByUserId(event.getUserId())) {
            Profile profile = Profile.createInitial(event.getUserId(), event.getEmail());
            profileRepository.save(profile);
        }

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .processedAt(Instant.now())
                .build();
        processedEventRepository.save(processedEvent);
    }
}
