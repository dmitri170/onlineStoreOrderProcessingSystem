package com.example.NotificationService.kafka;

import com.example.NotificationService.dto.KafkaOrderMessage;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.mapper.OrderMapper;
import com.example.NotificationService.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka Consumer для обработки сообщений о заказах от Order Service.
 * Сохраняет заказы в базу данных для аналитики и уведомлений.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    /**
     * Обрабатывает сообщения из Kafka топика orders.
     *
     * @param message JSON строка с данными заказа
     */
    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(String message) {
        try {
            log.info("Получено сообщение из Kafka: {}", message);

            KafkaOrderMessage kafkaMessage = objectMapper.readValue(message, KafkaOrderMessage.class);
            String orderId = kafkaMessage.getOrderId();

            // Проверяем существование заказа
            if (orderService.orderExists(orderId)) {
                log.warn("Заказ с id {} уже существует", orderId);
                return;
            }

            // Преобразуем в сущности
            Order order = orderMapper.toOrderEntity(kafkaMessage);
            List<OrderItem> orderItems = orderMapper.toOrderItemEntities(kafkaMessage, order);

            // Сохраняем в одной транзакции
            orderService.processOrder(order, orderItems);

            log.info("Успешно обработан заказ: {}", orderId);

        } catch (Exception e) {
            log.error("Ошибка обработки сообщения: {}", message, e);
            // Можно добавить логику retry или отправку в dead letter queue
        }
    }
}