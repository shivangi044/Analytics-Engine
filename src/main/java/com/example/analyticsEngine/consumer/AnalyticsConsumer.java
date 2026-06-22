package com.example.analyticsEngine.consumer;

import com.example.analyticsEngine.model.PriceUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class AnalyticsConsumer {

    private final AtomicInteger totalUpdatesProcessed = new AtomicInteger(0);

    @KafkaListener(topics = "product-price-updates", groupId = "analytics-service-group")
    public void listen(PriceUpdateEvent event) {
        int count = totalUpdatesProcessed.incrementAndGet();
        log.info("[Analytics Service] Total price updates processed so far: {}", count);
    }
}