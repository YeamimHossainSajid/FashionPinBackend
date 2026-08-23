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
public class BrandDeletedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String brandId;

    public static BrandDeletedEvent create(String brandId, String correlationId) {
        return BrandDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("BrandDeleted")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .brandId(brandId)
                .build();
    }
}
