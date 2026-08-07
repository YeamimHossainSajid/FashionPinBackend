package com.fashionpin.imageprocessingservice.kafka;

import com.fashionpin.common.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@org.springframework.context.annotation.Profile({"!dev", "!test"})
public class KafkaTopicConfig {

    @Bean
    public NewTopic serviceEventsTopic() {
        return TopicBuilder.name("fashionpin.imageprocessingservice.events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

