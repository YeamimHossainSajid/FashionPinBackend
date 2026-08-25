package com.fashionpin.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutfitCreatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String outfitId;
    private String userId;
    private String name;
    private List<String> productIds;

    public static OutfitCreatedEvent create(String outfitId, String userId, String name, List<String> productIds, String correlationId) {
        return OutfitCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OutfitCreated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .outfitId(outfitId)
                .userId(userId)
                .name(name)
                .productIds(productIds)
                .build();
    }
}
