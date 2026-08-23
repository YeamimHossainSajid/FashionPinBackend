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
public class ProductUpdatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String productId;
    private String brandId;
    private String name;

    public static ProductUpdatedEvent create(String productId, String brandId, String name, String correlationId) {
        return ProductUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ProductUpdated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .productId(productId)
                .brandId(brandId)
                .name(name)
                .build();
    }
}
