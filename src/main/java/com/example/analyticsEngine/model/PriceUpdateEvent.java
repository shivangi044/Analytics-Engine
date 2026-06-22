package com.example.analyticsEngine.model;

public record PriceUpdateEvent(
        String productId,
        double oldPrice,
        double newPrice,
        long timestamp
) {}