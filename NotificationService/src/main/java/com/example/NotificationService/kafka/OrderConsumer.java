package com.example.NotificationService.kafka;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.entity.dto.KafkaOrderMessage;
import com.example.NotificationService.repository.OrderRepository;
import com.example.NotificationService.repository.OrderItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ObjectMapper objectMapper;

    public OrderConsumer(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "orders", groupId = "notification-group")
    @Transactional
    public void consume(String message) {
        try {
            log.info("Received message from Kafka: {}", message);

            KafkaOrderMessage kafkaMessage = objectMapper.readValue(message, KafkaOrderMessage.class);

            // Проверяем, не существует ли уже заказ с таким orderId
            if (orderRepository.findByOrderId(kafkaMessage.getOrderId()).isPresent()) {
                log.warn("Order with id {} already exists", kafkaMessage.getOrderId());
                return;
            }

            // Создаем и сохраняем заказ
            Order order = new Order();
            order.setOrderId(kafkaMessage.getOrderId());
            order.setUserId(kafkaMessage.getUserId());
            order.setTotalPrice(kafkaMessage.getTotalPrice() != null ? kafkaMessage.getTotalPrice() : BigDecimal.ZERO);

            // Парсим дату
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime orderDate = kafkaMessage.getOrderDate() != null ?
                    LocalDateTime.parse(kafkaMessage.getOrderDate(), formatter) : LocalDateTime.now();
            order.setOrderDate(orderDate);

            Order savedOrder = orderRepository.save(order);

            // Создаем и сохраняем элементы заказа
            List<OrderItem> orderItems = new ArrayList<>();
            if (kafkaMessage.getItems() != null) {
                for (KafkaOrderMessage.OrderItemMessage itemMessage : kafkaMessage.getItems()) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setProductId(itemMessage.getProductId() != null ? itemMessage.getProductId() : 0L);
                    orderItem.setQuantity(itemMessage.getQuantity() != null ? itemMessage.getQuantity() : 0);
                    orderItem.setPrice(itemMessage.getPrice() != null ? itemMessage.getPrice() : BigDecimal.ZERO);
                    orderItem.setDiscount(itemMessage.getDiscount() != null ? itemMessage.getDiscount() : BigDecimal.ZERO);
                    orderItem.setItemTotal(itemMessage.getItemTotal() != null ? itemMessage.getItemTotal() : BigDecimal.ZERO);
                    orderItems.add(orderItem);
                }
                orderItemRepository.saveAll(orderItems);
            }

            log.info("Successfully saved order {} with {} items", savedOrder.getOrderId(), orderItems.size());

        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }
}