package com.example.OrderService.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsConfig {
    private String orderEvents;

    public String getOrderEvents() {
        return orderEvents;
    }

    public void setOrderEvents(String orderEvents) {
        this.orderEvents = orderEvents;
    }
}