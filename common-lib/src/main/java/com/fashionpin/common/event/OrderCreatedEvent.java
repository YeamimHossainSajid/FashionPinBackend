package com.fashionpin.common.event;

import java.math.BigDecimal;
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
public class OrderCreatedEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String orderId;
    private String orderNumber;
    private String userId;
    private String email;
    private BigDecimal totalAmount;
    private String currency;

    public static OrderCreatedEvent create(String orderId, String orderNumber, String userId, String email, BigDecimal totalAmount, String currency, String correlationId) {
        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderCreated")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .orderId(orderId)
                .orderNumber(orderNumber)
                .userId(userId)
                .email(email)
                .totalAmount(totalAmount)
                .currency(currency)
                .build();
    }
}
