package com.fashionpin.orderservice.kafka;

import com.fashionpin.common.event.OrderCreatedEvent;
import com.fashionpin.common.event.OrderPaidEvent;
import com.fashionpin.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderNumber: {}", event.getOrderNumber());
        try {
            kafkaTemplate.send(KafkaTopics.ORDER_CREATED_V1, event.getOrderNumber(), event);
        } catch (Exception e) {
            log.warn("Kafka send failed for OrderCreatedEvent: {}", e.getMessage());
        }
    }

    public void publishOrderPaid(OrderPaidEvent event) {
        log.info("Publishing OrderPaidEvent for orderNumber: {}", event.getOrderNumber());
        try {
            kafkaTemplate.send(KafkaTopics.ORDER_PAID_V1, event.getOrderNumber(), event);
        } catch (Exception e) {
            log.warn("Kafka send failed for OrderPaidEvent: {}", e.getMessage());
        }
    }
}
