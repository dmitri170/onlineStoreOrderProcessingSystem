package com.example.OrderService.kafka;

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

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrder(OrderMessage orderMessage) {
        try {
            log.info("[Заказ: {}] Отправка заказа в Kafka топик 'orders'", orderMessage.getOrderId());
            kafkaTemplate.send("orders", orderMessage.getOrderId(), orderMessage);
            log.info("[Заказ: {}] Заказ успешно отправлен в Kafka", orderMessage.getOrderId());
        } catch (Exception e) {
            log.error("[Заказ: {}] Ошибка при отправке заказа в Kafka: {}", orderMessage.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить заказ в Kafka", e);
        }
    }
}
