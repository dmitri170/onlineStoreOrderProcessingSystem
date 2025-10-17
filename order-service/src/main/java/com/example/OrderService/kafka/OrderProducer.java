package com.example.OrderService.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Компонент для отправки заказов в Kafka.
 * Отвечает за сериализацию и отправку сообщений о заказах.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    /**
     * Отправляет заказ в Kafka топик.
     *
     * @param message сообщение с данными заказа
     * @throws RuntimeException если не удалось сериализовать сообщение
     */
    public void sendOrder(OrderMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            // Отправляем сообщение с ключом = orderId для гарантии порядка обработки заказов с одинаковым ID
            kafkaTemplate.send(ordersTopic, message.getOrderId(), messageJson);
            log.info("Заказ отправлен в Kafka: {}", message.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Не удалось сериализовать сообщение заказа: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить заказ в Kafka", e);
        }
    }
}