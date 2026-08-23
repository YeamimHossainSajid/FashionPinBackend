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
public class BrandCreatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String brandId;
    private String name;
    private String slug;

    public static BrandCreatedEvent create(String brandId, String name, String slug, String correlationId) {
        return BrandCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("BrandCreated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .brandId(brandId)
                .name(name)
                .slug(slug)
                .build();
    }
}
