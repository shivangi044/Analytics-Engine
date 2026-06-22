package com.example.analyticsEngine.producer;


import com.example.analyticsEngine.model.PriceUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceProducerService {

    private static final String TOPIC = "product-price-updates";
    private final KafkaTemplate<String, PriceUpdateEvent> kafkaTemplate;

    public void publishPriceUpdate(PriceUpdateEvent event) {
        log.info("Publishing price update event to Kafka for Product: {}", event.productId());

        // Using the productId as the message key guarantees that all updates
        // for a specific product land on the exact same partition, preserving strict order.
        kafkaTemplate.send(TOPIC, event.productId(), event);
    }
}