package com.example.NotificationService.kafka;

import com.example.NotificationService.dto.KafkaOrderMessage;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.mapper.OrderMapper;
import com.example.NotificationService.service.OrderProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;
    private final OrderProcessingService orderProcessingService;

    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(String message) {
        try {
            log.info("Received message from Kafka: {}", message);

            KafkaOrderMessage kafkaMessage = objectMapper.readValue(message, KafkaOrderMessage.class);
            String orderId = kafkaMessage.getOrderId();

            // Проверяем существование заказа
            if (orderProcessingService.orderExists(orderId)) {
                log.warn("Order with id {} already exists", orderId);
                return;
            }

            // Маппим в entity
            Order order = orderMapper.toOrderEntity(kafkaMessage);
            List<OrderItem> orderItems = orderMapper.toOrderItemEntities(kafkaMessage, order);

            // Сохраняем в одной транзакции
            orderProcessingService.processOrder(order, orderItems);

            log.info("Successfully processed order: {}", orderId);

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
            // Можно добавить retry логику или dead letter queue
        }
    }
}