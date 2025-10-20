package com.example.NotificationService.kafka;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.mapper.OrderMapper;
import com.example.NotificationService.service.OrderService;
import dto.OrderMessage;
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

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    /**
     * Обрабатывает сообщения из Kafka топика orders.
     * Получает сообщение в формате OrderMessage благодаря JsonDeserializer.
     *
     * @param message объект OrderMessage с данными заказа
     */
    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(OrderMessage message) {
        String orderId = "unknown";

        try {
            orderId = message.getOrderId();
            log.info("=== KAFKA MESSAGE RECEIVED ===");
            log.info("Order ID: {}", orderId);
            log.info("User ID: {}", message.getUserId());
            log.info("Username: {}", message.getUsername());
            log.info("Total Price: {}", message.getTotalPrice());
            log.info("Order Date: {}", message.getOrderDate());
            log.info("Items count: {}", message.getItems() != null ? message.getItems().size() : 0);

            // Проверяем существование заказа
            if (orderService.orderExists(orderId)) {
                log.warn("Заказ с id {} уже существует", orderId);
                return;
            }

            // Преобразуем в сущности
            Order order = orderMapper.toOrderEntity(message);
            List<OrderItem> orderItems = orderMapper.toOrderItemEntities(message, order);

            log.info("Преобразование в сущности завершено. Order: {}, Items: {}",
                    order != null ? order.getId() : "null",
                    orderItems != null ? orderItems.size() : 0);

            // Сохраняем в одной транзакции
            orderService.processOrder(order, orderItems);

            log.info("=== УСПЕШНО СОХРАНЕНО В БАЗУ ДАННЫХ ===");
            log.info("Заказ {} успешно обработан и сохранен", orderId);

        } catch (Exception e) {
            log.error("=== ОШИБКА ОБРАБОТКИ СООБЩЕНИЯ ===");
            log.error("Order ID: {}", orderId);
            log.error("Тип ошибки: {}", e.getClass().getSimpleName());
            log.error("Сообщение ошибки: {}", e.getMessage());
            log.error("Stack trace: ", e);
            // Можно добавить логику retry или отправку в dead letter queue
        }
    }
}