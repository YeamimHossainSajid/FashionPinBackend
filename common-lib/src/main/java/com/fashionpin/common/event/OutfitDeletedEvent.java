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
public class OutfitDeletedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String outfitId;
    private String userId;

    public static OutfitDeletedEvent create(String outfitId, String userId, String correlationId) {
        return OutfitDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OutfitDeleted")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .outfitId(outfitId)
                .userId(userId)
                .build();
    }
}
