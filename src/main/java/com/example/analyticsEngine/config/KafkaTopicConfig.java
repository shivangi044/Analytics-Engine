package com.example.analyticsEngine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic priceUpdatesTopic() {
        return TopicBuilder.name("product-price-updates")
                .partitions(3) // 3 partitions allows up to 3 parallel consumers later
                .replicas(1)   // Local single-node setup
                .build();
    }
}