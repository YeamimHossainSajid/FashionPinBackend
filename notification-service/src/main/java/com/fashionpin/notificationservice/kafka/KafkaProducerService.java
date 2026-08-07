package com.fashionpin.notificationservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

    public KafkaProducerService(ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
    }

    public void publish(String topic, String key, Object payload) {
        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.debug("Kafka template unavailable; skipped publish topic={} key={}", topic, key);
            return;
        }
        log.debug("Publishing event topic={} key={}", topic, key);
        kafkaTemplate.send(topic, key, payload);
    }
}

