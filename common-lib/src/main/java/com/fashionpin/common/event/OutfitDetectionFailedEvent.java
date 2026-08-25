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
public class OutfitDetectionFailedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String jobId;
    private String userId;
    private String errorMessage;

    public static OutfitDetectionFailedEvent create(String jobId, String userId, String errorMessage, String correlationId) {
        return OutfitDetectionFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OutfitDetectionFailed")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .jobId(jobId)
                .userId(userId)
                .errorMessage(errorMessage)
                .build();
    }
}
