package com.example.NotificationService.kafka;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.repository.OrderItemRepository;
import com.example.NotificationService.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    public OrderConsumer(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void consume(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            Long orderId = node.get("orderId").asLong();
            Long userId = node.get("userId").asLong();
            BigDecimal totalPrice = BigDecimal.valueOf(node.get("totalPrice").asDouble());

            // Создаем Order
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setTotalPrice(totalPrice);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("CREATED");

            order = orderRepository.save(order);

            // Обрабатываем items
            JsonNode items = node.get("items");
            List<OrderItem> orderItems = new ArrayList<>();

            for (JsonNode item : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(item.get("productId").asLong());
                orderItem.setQuantity(item.get("quantity").asInt());
                orderItem.setPrice(BigDecimal.valueOf(item.get("price").asDouble()));
                orderItem.setSale(BigDecimal.valueOf(item.get("sale").asDouble()));

                // Вычисляем itemTotal
                BigDecimal price = BigDecimal.valueOf(item.get("price").asDouble());
                BigDecimal quantity = BigDecimal.valueOf(item.get("quantity").asInt());
                BigDecimal sale = BigDecimal.valueOf(item.get("sale").asDouble());
                BigDecimal itemTotal = price.multiply(quantity).multiply(BigDecimal.ONE.subtract(sale));

                orderItem.setItemTotal(itemTotal);
                orderItems.add(orderItem);
            }

            orderItemRepository.saveAll(orderItems);
            log.info("Saved {} items for order {}", orderItems.size(), orderId);

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }
}