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
public class MediaDeletedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String mediaId;
    private String ownerId;

    public static MediaDeletedEvent create(String mediaId, String ownerId, String correlationId) {
        return MediaDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MediaDeleted")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .mediaId(mediaId)
                .ownerId(ownerId)
                .build();
    }
}
