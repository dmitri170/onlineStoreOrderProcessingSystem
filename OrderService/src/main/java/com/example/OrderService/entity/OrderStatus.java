package com.example.OrderService.entity;

public enum OrderStatus {
    CREATED("Order created"),
    PROCESSING("Checking inventory"),
    INSUFFICIENT_STOCK("Insufficient stock"),
    CALCULATED("Order calculated"),
    SAVED("Order saved to database"),
    SENT_TO_KAFKA("Order sent to Kafka"),
    COMPLETED("Order completed"),
    FAILED("Order failed");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}