package com.fashionpin.authservice.service;

import com.fashionpin.authservice.entity.OutboxEvent;
import com.fashionpin.authservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .build();
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }
}
