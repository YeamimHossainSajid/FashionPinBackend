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
public class FashionPostCreatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String postId;
    private String userId;
    private String outfitId;
    private List<String> mediaIds;
    private String caption;

    public static FashionPostCreatedEvent create(String postId, String userId, String outfitId, List<String> mediaIds, String caption, String correlationId) {
        return FashionPostCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FashionPostCreated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .postId(postId)
                .userId(userId)
                .outfitId(outfitId)
                .mediaIds(mediaIds)
                .caption(caption)
                .build();
    }
}
