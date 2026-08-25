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
public class FashionPostDeletedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String postId;
    private String userId;

    public static FashionPostDeletedEvent create(String postId, String userId, String correlationId) {
        return FashionPostDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FashionPostDeleted")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .postId(postId)
                .userId(userId)
                .build();
    }
}
