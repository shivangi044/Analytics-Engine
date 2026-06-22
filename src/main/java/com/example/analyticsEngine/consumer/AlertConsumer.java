package com.example.analyticsEngine.consumer;

import com.example.analyticsEngine.model.PriceUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertConsumer {

    // groupId ensures this service tracks its own position in the stream
    @KafkaListener(topics = "product-price-updates", groupId = "alert-service-group")
    public void listen(PriceUpdateEvent event) {
        if ("CRASH-ME".equals(event.productId())) {
            throw new RuntimeException("Simulated processing failure!");
        }
        log.info("[Alert Service] Received update for {}: ${} -> ${}",
                event.productId(), event.oldPrice(), event.newPrice());

        // Simple mock logic: if price drops by more than 20%
        if (event.newPrice() < event.oldPrice() * 0.8) {
            log.warn("[ALERT] 🔥 Major price drop detected for {}! Sending notification to users...",
                    event.productId());
        }
    }
}