package com.fashionpin.search.infrastructure.kafka;

import com.fashionpin.common.event.FashionPostEventPayload;
import com.fashionpin.common.kafka.KafkaTopicConstants;
import com.fashionpin.search.service.FashionPostIndexSyncService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FashionPostSearchEventListener {

    private final FashionPostIndexSyncService syncService;

    @KafkaListener(
            topics = KafkaTopicConstants.FASHIONPIN_FASHION_POST_CREATED_V1,
            groupId = "${spring.kafka.consumer.group-id:search-service-group}"
    )
    public void onFashionPostCreated(@Payload FashionPostEventPayload payload) {
        log.info("Received FashionPostCreated event for postId={}", payload != null ? payload.getPostId() : null);
        syncService.upsertPostIndex(payload);
    }

    @KafkaListener(
            topics = KafkaTopicConstants.FASHIONPIN_FASHION_POST_UPDATED_V1,
            groupId = "${spring.kafka.consumer.group-id:search-service-group}"
    )
    public void onFashionPostUpdated(@Payload FashionPostEventPayload payload) {
        log.info("Received FashionPostUpdated event for postId={}", payload != null ? payload.getPostId() : null);
        syncService.upsertPostIndex(payload);
    }

    @KafkaListener(
            topics = KafkaTopicConstants.FASHIONPIN_FASHION_POST_DELETED_V1,
            groupId = "${spring.kafka.consumer.group-id:search-service-group}"
    )
    public void onFashionPostDeleted(@Payload Object payload) {
        log.info("Received FashionPostDeleted event: {}", payload);
        if (payload instanceof FashionPostEventPayload postPayload) {
            syncService.deletePostIndex(postPayload.getPostId());
        } else if (payload instanceof String idStr) {
            try {
                syncService.deletePostIndex(UUID.fromString(idStr));
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse deleted postId from string: {}", idStr, e);
            }
        } else if (payload instanceof UUID uuid) {
            syncService.deletePostIndex(uuid);
        }
    }
}
