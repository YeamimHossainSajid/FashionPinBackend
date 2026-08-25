package com.fashionpin.fashiondiscoveryservice.kafka;

import com.fashionpin.common.event.FashionPostCreatedEvent;
import com.fashionpin.common.event.FashionPostDeletedEvent;
import com.fashionpin.common.event.FashionPostUpdatedEvent;
import com.fashionpin.common.event.OutfitCreatedEvent;
import com.fashionpin.common.event.OutfitDeletedEvent;
import com.fashionpin.common.event.OutfitUpdatedEvent;
import com.fashionpin.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FashionDiscoveryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPostCreated(FashionPostCreatedEvent event) {
        log.info("Publishing FashionPostCreatedEvent for postId: {}", event.getPostId());
        kafkaTemplate.send(KafkaTopics.FASHION_POST_CREATED_V1, event.getPostId(), event);
    }

    public void publishPostUpdated(FashionPostUpdatedEvent event) {
        log.info("Publishing FashionPostUpdatedEvent for postId: {}", event.getPostId());
        kafkaTemplate.send(KafkaTopics.FASHION_POST_UPDATED_V1, event.getPostId(), event);
    }

    public void publishPostDeleted(FashionPostDeletedEvent event) {
        log.info("Publishing FashionPostDeletedEvent for postId: {}", event.getPostId());
        kafkaTemplate.send(KafkaTopics.FASHION_POST_DELETED_V1, event.getPostId(), event);
    }

    public void publishOutfitCreated(OutfitCreatedEvent event) {
        log.info("Publishing OutfitCreatedEvent for outfitId: {}", event.getOutfitId());
        kafkaTemplate.send(KafkaTopics.OUTFIT_CREATED_V1, event.getOutfitId(), event);
    }

    public void publishOutfitUpdated(OutfitUpdatedEvent event) {
        log.info("Publishing OutfitUpdatedEvent for outfitId: {}", event.getOutfitId());
        kafkaTemplate.send(KafkaTopics.OUTFIT_UPDATED_V1, event.getOutfitId(), event);
    }

    public void publishOutfitDeleted(OutfitDeletedEvent event) {
        log.info("Publishing OutfitDeletedEvent for outfitId: {}", event.getOutfitId());
        kafkaTemplate.send(KafkaTopics.OUTFIT_DELETED_V1, event.getOutfitId(), event);
    }
}
