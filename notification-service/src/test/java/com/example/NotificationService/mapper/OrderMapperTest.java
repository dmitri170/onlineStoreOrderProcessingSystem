package com.example.NotificationService.mapper;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import dto.OrderMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Test
    void toOrderEntity_WithValidOrderMessage_ShouldReturnOrder() {
        // Arrange
        OrderMessage message = new OrderMessage();
        message.setOrderId("order-123");
        message.setUserId(1L);
        message.setUsername("testuser");
        message.setTotalPrice(BigDecimal.valueOf(150.0));
        String dateString = LocalDateTime.now().toString();
        message.setOrderDate(dateString);

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
        OrderMessage message = new OrderMessage();
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
        OrderMessage message = new OrderMessage();
        message.setOrderId("order-123");

        OrderMessage.OrderItemMessage itemMessage = new OrderMessage.OrderItemMessage(
                1L, 2, BigDecimal.valueOf(50.0), BigDecimal.valueOf(0.1), BigDecimal.valueOf(90.0)
        );

        message.setItems(List.of(itemMessage));

        Order order = new Order();
        order.setId(1L);
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
    void toOrderItemEntities_WithEmptyItems_ShouldReturnEmptyList() {
        // Arrange
        OrderMessage message = new OrderMessage();
        message.setItems(List.of());

        Order order = new Order();

        // Act
        List<OrderItem> orderItems = orderMapper.toOrderItemEntities(message, order);

        // Assert
        assertNotNull(orderItems);
        assertTrue(orderItems.isEmpty());
    }

    @Test
    void toOrderItemEntities_WithNullItems_ShouldReturnEmptyList() {
        // Arrange
        OrderMessage message = new OrderMessage();
        message.setItems(null);

        Order order = new Order();

        // Act
        List<OrderItem> orderItems = orderMapper.toOrderItemEntities(message, order);

        // Assert
        assertNotNull(orderItems);
        assertTrue(orderItems.isEmpty());
    }
}