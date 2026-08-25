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
public class OutfitDetectionCompletedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String jobId;
    private String userId;
    private int detectedItemCount;

    public static OutfitDetectionCompletedEvent create(String jobId, String userId, int detectedItemCount, String correlationId) {
        return OutfitDetectionCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OutfitDetectionCompleted")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .jobId(jobId)
                .userId(userId)
                .detectedItemCount(detectedItemCount)
                .build();
    }
}
