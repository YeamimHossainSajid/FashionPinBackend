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
public class MediaReadyEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String mediaId;
    private String ownerId;

    public static MediaReadyEvent create(String mediaId, String ownerId, String correlationId) {
        return MediaReadyEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MediaReady")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .mediaId(mediaId)
                .ownerId(ownerId)
                .build();
    }
}
