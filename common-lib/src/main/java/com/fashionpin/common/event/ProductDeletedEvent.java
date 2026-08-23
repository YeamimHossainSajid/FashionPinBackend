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
public class ProductDeletedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String productId;
    private String brandId;

    public static ProductDeletedEvent create(String productId, String brandId, String correlationId) {
        return ProductDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ProductDeleted")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .productId(productId)
                .brandId(brandId)
                .build();
    }
}
