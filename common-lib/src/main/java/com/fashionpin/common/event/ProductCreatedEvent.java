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
public class ProductCreatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String productId;
    private String brandId;
    private String name;
    private String category;

    public static ProductCreatedEvent create(String productId, String brandId, String name, String category, String correlationId) {
        return ProductCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ProductCreated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .productId(productId)
                .brandId(brandId)
                .name(name)
                .category(category)
                .build();
    }
}
