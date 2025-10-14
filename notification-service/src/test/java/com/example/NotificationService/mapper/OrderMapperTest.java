package com.example.NotificationService.mapper;

import com.example.NotificationService.dto.KafkaOrderMessage;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юнит тесты для OrderMapper.
 */
class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Test
    void toOrderEntity_WithValidKafkaMessage_ShouldReturnOrder() {
        // Arrange
        KafkaOrderMessage message = new KafkaOrderMessage();
        message.setOrderId("order-123");
        message.setUserId(1L);
        message.setTotalPrice(BigDecimal.valueOf(150.0));
        message.setOrderDate(LocalDateTime.now().toString());

        // Act
        Order order = orderMapper.toOrderEntity(message);

        // Assert
        assertNotNull(order);
        assertEquals("order-123", order.getOrderId());
        assertEquals(1L, order.getUserId());
        assertEquals(BigDecimal.valueOf(150.0), order.getTotalPrice());
        assertNotNull(order.getOrderDate());
    }

    @Test
    void toOrderEntity_WithNullDate_ShouldUseCurrentDate() {
        // Arrange
        KafkaOrderMessage message = new KafkaOrderMessage();
        message.setOrderId("order-123");
        message.setUserId(1L);
        message.setTotalPrice(BigDecimal.valueOf(150.0));
        message.setOrderDate(null);

        // Act
        Order order = orderMapper.toOrderEntity(message);

        // Assert
        assertNotNull(order);
        assertNotNull(order.getOrderDate());
    }

    @Test
    void toOrderItemEntities_WithValidItems_ShouldReturnOrderItems() {
        // Arrange
        KafkaOrderMessage message = new KafkaOrderMessage();
        message.setOrderId("order-123");

        KafkaOrderMessage.OrderItemMessage itemMessage = new KafkaOrderMessage.OrderItemMessage();
        itemMessage.setProductId(1L);
        itemMessage.setQuantity(2);
        itemMessage.setPrice(BigDecimal.valueOf(50.0));
        itemMessage.setDiscount(BigDecimal.valueOf(0.1));
        itemMessage.setItemTotal(BigDecimal.valueOf(90.0));

        message.setItems(List.of(itemMessage));

        Order order = new Order();
        order.setOrderId("order-123");

        // Act
        List<OrderItem> orderItems = orderMapper.toOrderItemEntities(message, order);

        // Assert
        assertNotNull(orderItems);
        assertEquals(1, orderItems.size());

        OrderItem orderItem = orderItems.get(0);
        assertEquals(order, orderItem.getOrder());
        assertEquals(1L, orderItem.getProductId());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(BigDecimal.valueOf(50.0), orderItem.getPrice());
        assertEquals(BigDecimal.valueOf(0.1), orderItem.getDiscount());
        assertEquals(BigDecimal.valueOf(90.0), orderItem.getItemTotal());
    }

    @Test
    void toOrderItemEntities_WithNullItems_ShouldReturnEmptyList() {
        // Arrange
        KafkaOrderMessage message = new KafkaOrderMessage();
        message.setItems(null);

        Order order = new Order();

        // Act
        List<OrderItem> orderItems = orderMapper.toOrderItemEntities(message, order);

        // Assert
        assertNotNull(orderItems);
        assertTrue(orderItems.isEmpty());
    }

    @Test
    void toOrderItemEntities_WithNullValues_ShouldUseDefaults() {
        // Arrange
        KafkaOrderMessage message = new KafkaOrderMessage();

        KafkaOrderMessage.OrderItemMessage itemMessage = new KafkaOrderMessage.OrderItemMessage();
        // All fields are null

        message.setItems(List.of(itemMessage));

        Order order = new Order();

        // Act
        List<OrderItem> orderItems = orderMapper.toOrderItemEntities(message, order);

        // Assert
        assertNotNull(orderItems);
        assertEquals(1, orderItems.size());

        OrderItem orderItem = orderItems.get(0);
        assertEquals(0L, orderItem.getProductId());
        assertEquals(0, orderItem.getQuantity());
        assertEquals(BigDecimal.ZERO, orderItem.getPrice());
        assertEquals(BigDecimal.ZERO, orderItem.getDiscount());
        assertEquals(BigDecimal.ZERO, orderItem.getItemTotal());
    }
}