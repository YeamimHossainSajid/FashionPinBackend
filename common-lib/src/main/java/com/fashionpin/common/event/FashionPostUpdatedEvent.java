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
public class FashionPostUpdatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String postId;
    private String userId;

    public static FashionPostUpdatedEvent create(String postId, String userId, String correlationId) {
        return FashionPostUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FashionPostUpdated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .postId(postId)
                .userId(userId)
                .build();
    }
}
