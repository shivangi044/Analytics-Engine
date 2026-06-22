package com.example.analyticsEngine.controller;

import com.example.analyticsEngine.model.PriceUpdateEvent;
import com.example.analyticsEngine.producer.PriceProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/prices")
@RequiredArgsConstructor
public class PriceIngestionController {

    private final PriceProducerService priceProducerService;

    @PostMapping
    public ResponseEntity<String> ingestPriceUpdate(@RequestBody PriceUpdateEvent request) {
        // Enforce a valid system epoch timestamp if omitted by the client request payload
        PriceUpdateEvent event = new PriceUpdateEvent(
                request.productId(),
                request.oldPrice(),
                request.newPrice(),
                request.timestamp() == 0 ? Instant.now().getEpochSecond() : request.timestamp()
        );

        priceProducerService.publishPriceUpdate(event);
        return ResponseEntity.ok("Price update event queued onto Kafka successfully!");
    }
}