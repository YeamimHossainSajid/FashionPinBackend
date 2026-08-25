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
public class OutfitUpdatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String outfitId;
    private String userId;

    public static OutfitUpdatedEvent create(String outfitId, String userId, String correlationId) {
        return OutfitUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OutfitUpdated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .outfitId(outfitId)
                .userId(userId)
                .build();
    }
}
