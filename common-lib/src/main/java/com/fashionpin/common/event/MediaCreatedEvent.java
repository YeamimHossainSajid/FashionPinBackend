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
public class MediaCreatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String mediaId;
    private String ownerId;
    private String mediaType;
    private String storageKey;

    public static MediaCreatedEvent create(String mediaId, String ownerId, String mediaType, String storageKey, String correlationId) {
        return MediaCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MediaCreated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .mediaId(mediaId)
                .ownerId(ownerId)
                .mediaType(mediaType)
                .storageKey(storageKey)
                .build();
    }
}
