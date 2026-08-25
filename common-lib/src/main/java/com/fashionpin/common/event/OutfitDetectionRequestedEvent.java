package com.fashionpin.common.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutfitDetectionRequestedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String jobId;
    private String userId;
    private String inputMediaId;

    public static OutfitDetectionRequestedEvent create(String jobId, String userId, String inputMediaId, String correlationId) {
        return OutfitDetectionRequestedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OutfitDetectionRequested")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .jobId(jobId)
                .userId(userId)
                .inputMediaId(inputMediaId)
                .build();
    }
}
