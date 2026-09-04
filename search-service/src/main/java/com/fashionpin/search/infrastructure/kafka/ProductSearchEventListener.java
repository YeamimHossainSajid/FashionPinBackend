package com.fashionpin.search.infrastructure.kafka;

import com.fashionpin.common.event.ProductEventPayload;
import com.fashionpin.common.kafka.KafkaTopicConstants;
import com.fashionpin.search.service.ProductIndexSyncService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchEventListener {

    private final ProductIndexSyncService syncService;

    @KafkaListener(
            topics = KafkaTopicConstants.FASHIONPIN_PRODUCT_CREATED_V1,
            groupId = "${spring.kafka.consumer.group-id:search-service-group}"
    )
    public void onProductCreated(@Payload ProductEventPayload payload) {
        log.info("Received ProductCreated event for productId={}", payload != null ? payload.getProductId() : null);
        syncService.upsertProductIndex(payload);
    }

    @KafkaListener(
            topics = KafkaTopicConstants.FASHIONPIN_PRODUCT_UPDATED_V1,
            groupId = "${spring.kafka.consumer.group-id:search-service-group}"
    )
    public void onProductUpdated(@Payload ProductEventPayload payload) {
        log.info("Received ProductUpdated event for productId={}", payload != null ? payload.getProductId() : null);
        syncService.upsertProductIndex(payload);
    }

    @KafkaListener(
            topics = KafkaTopicConstants.FASHIONPIN_PRODUCT_DELETED_V1,
            groupId = "${spring.kafka.consumer.group-id:search-service-group}"
    )
    public void onProductDeleted(@Payload Object payload) {
        log.info("Received ProductDeleted event: {}", payload);
        if (payload instanceof ProductEventPayload productPayload) {
            syncService.deleteProductIndex(productPayload.getProductId());
        } else if (payload instanceof String idStr) {
            try {
                syncService.deleteProductIndex(UUID.fromString(idStr));
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse deleted productId from string: {}", idStr, e);
            }
        } else if (payload instanceof UUID uuid) {
            syncService.deleteProductIndex(uuid);
        }
    }
}
