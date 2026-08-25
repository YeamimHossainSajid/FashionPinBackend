package com.fashionpin.outfitdetectionservice.kafka;

import com.fashionpin.common.event.OutfitDetectionCompletedEvent;
import com.fashionpin.common.event.OutfitDetectionFailedEvent;
import com.fashionpin.common.event.OutfitDetectionRequestedEvent;
import com.fashionpin.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutfitDetectionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishJobRequested(OutfitDetectionRequestedEvent event) {
        log.info("Publishing OutfitDetectionRequestedEvent for jobId: {}", event.getJobId());
        kafkaTemplate.send(KafkaTopics.OUTFIT_DETECTION_REQUESTED_V1, event.getJobId(), event);
    }

    public void publishJobCompleted(OutfitDetectionCompletedEvent event) {
        log.info("Publishing OutfitDetectionCompletedEvent for jobId: {}", event.getJobId());
        kafkaTemplate.send(KafkaTopics.OUTFIT_DETECTION_COMPLETED_V1, event.getJobId(), event);
    }

    public void publishJobFailed(OutfitDetectionFailedEvent event) {
        log.info("Publishing OutfitDetectionFailedEvent for jobId: {}", event.getJobId());
        kafkaTemplate.send(KafkaTopics.OUTFIT_DETECTION_FAILED_V1, event.getJobId(), event);
    }
}
