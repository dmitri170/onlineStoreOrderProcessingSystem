package com.example.OrderService.kafka;

import com.example.OrderService.message.model.OrderMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    // Топик можно вынести в конфигурацию, но для простоты укажем здесь
    private static final String ORDERS_TOPIC = "orders";

    public OrderProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendOrder(OrderMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            // Отправляем сообщение с ключом = orderId для гарантии порядка обработки заказов с одинаковым ID
            kafkaTemplate.send(ORDERS_TOPIC, message.getOrderId(), messageJson);
            log.info("Order sent to Kafka: {}", message.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order message: {}", e.getMessage());
            throw new RuntimeException("Failed to send order to Kafka", e);
        }
    }
}