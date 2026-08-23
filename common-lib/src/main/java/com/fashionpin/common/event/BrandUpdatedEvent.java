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
public class BrandUpdatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String brandId;
    private String name;
    private String slug;

    public static BrandUpdatedEvent create(String brandId, String name, String slug, String correlationId) {
        return BrandUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("BrandUpdated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .brandId(brandId)
                .name(name)
                .slug(slug)
                .build();
    }
}
