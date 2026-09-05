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
public class OrderPaidEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String orderId;
    private String orderNumber;
    private String paymentTransactionId;
    private BigDecimal amountPaid;

    public static OrderPaidEvent create(String orderId, String orderNumber, String paymentTransactionId, BigDecimal amountPaid, String correlationId) {
        return OrderPaidEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderPaid")
                .timestamp(Instant.now())
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .orderId(orderId)
                .orderNumber(orderNumber)
                .paymentTransactionId(paymentTransactionId)
                .amountPaid(amountPaid)
                .build();
    }
}
