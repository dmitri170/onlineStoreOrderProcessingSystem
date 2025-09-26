package com.example.OrderService.kafka;

import com.example.OrderService.message.model.OrderMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrder(OrderMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("orders", messageJson);
            log.info("Order sent to Kafka: {}", message.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order message: {}", e.getMessage());
            throw new RuntimeException("Failed to send order to Kafka", e);
        }
    }
}