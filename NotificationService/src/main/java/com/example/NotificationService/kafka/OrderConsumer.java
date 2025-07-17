package com.example.NotificationService.kafka;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderEntity;
import com.example.NotificationService.repository.OrderItemRepository;
import com.example.NotificationService.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String externalOrderId = node.get("orderId").asText();
            Long userId = node.get("userId").asLong();

            Order order = Order.builder()
                    .externalOrderId(externalOrderId)
                    .build();
            order = orderRepository.save(order);

            JsonNode items = node.get("items");
            List<OrderEntity> orderItems = new ArrayList<>();
            for (JsonNode item : items) {
                OrderEntity entity = OrderEntity.builder()
                        .order(order)
                        .userId(userId) // ✅ просто ID, без создания объекта User
                        .productId(item.get("productId").asLong())
                        .quantity(item.get("quantity").asInt())
                        .price(item.get("price").asDouble())
                        .sale(item.get("sale").asDouble())
                        .totalPrice(item.get("price").asDouble() * item.get("quantity").asInt() * (1 - item.get("sale").asDouble()))
                        .build();
                orderItems.add(entity);
            }

            orderItemRepository.saveAll(orderItems);
            log.info("Saved {} items for order {}", orderItems.size(), externalOrderId);

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }
    /*@KafkaListener(topics = "orders")
    public void listen(OrderEntity event) {
        try {
            notificationService.processOrder(event);
        } catch (Exception e) {
            log.error("Error processing order {}", event.getOrderId(), e);
            // Можно добавить dead-letter queue
        }
    }*/
}
