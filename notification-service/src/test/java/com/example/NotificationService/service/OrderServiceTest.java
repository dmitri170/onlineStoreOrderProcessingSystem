package com.example.NotificationService.service;

import com.example.NotificationService.dto.KafkaOrderMessage;
import com.example.NotificationService.dto.OrderDto;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.mapper.OrderMapper;
import com.example.NotificationService.repository.OrderRepository;
import com.example.NotificationService.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Юнит тесты для сервиса заказов Notification Service.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderItem testOrderItem;
    private KafkaOrderMessage testKafkaMessage;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderId("order-123");
        testOrder.setUserId(1L);
        testOrder.setTotalPrice(BigDecimal.valueOf(150.0));
        testOrder.setOrderDate(LocalDateTime.now());

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProductId(1L);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(BigDecimal.valueOf(50.0));
        testOrderItem.setDiscount(BigDecimal.valueOf(0.1));
        testOrderItem.setItemTotal(BigDecimal.valueOf(90.0));

        testKafkaMessage = new KafkaOrderMessage();
        testKafkaMessage.setOrderId("order-123");
        testKafkaMessage.setUserId(1L);
        testKafkaMessage.setTotalPrice(BigDecimal.valueOf(150.0));
        testKafkaMessage.setOrderDate(LocalDateTime.now().toString());
    }

    @Test
    void processOrder_WithValidData_ShouldSaveOrderAndItems() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.saveAll(any())).thenReturn(List.of(testOrderItem));

        // Act
        orderService.processOrder(testOrder, List.of(testOrderItem));

        // Assert
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderItemRepository, times(1)).saveAll(any());
        assertEquals(testOrder, testOrderItem.getOrder()); // Verify relationship was set
    }

    @Test
    void orderExists_WithExistingOrder_ShouldReturnTrue() {
        // Arrange
        when(orderRepository.findByOrderId("order-123")).thenReturn(Optional.of(testOrder));

        // Act
        boolean exists = orderService.orderExists("order-123");

        // Assert
        assertTrue(exists);
        verify(orderRepository, times(1)).findByOrderId("order-123");
    }

    @Test
    void orderExists_WithNonExistingOrder_ShouldReturnFalse() {
        // Arrange
        when(orderRepository.findByOrderId("unknown-order")).thenReturn(Optional.empty());

        // Act
        boolean exists = orderService.orderExists("unknown-order");

        // Assert
        assertFalse(exists);
        verify(orderRepository, times(1)).findByOrderId("unknown-order");
    }

    @Test
    void getAllOrders_ShouldReturnOrderList() {
        // Arrange
        when(orderRepository.findAllByOrderByOrderDateDesc()).thenReturn(List.of(testOrder));
        when(orderItemRepository.findByOrderOrderId("order-123")).thenReturn(List.of(testOrderItem));

        // Act
        List<OrderDto> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAllByOrderByOrderDateDesc();
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-123");
    }

    @Test
    void getOrdersByOrderId_WithExistingOrder_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findByOrderId("order-123")).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderOrderId("order-123")).thenReturn(List.of(testOrderItem));

        // Act
        List<OrderDto> result = orderService.getOrdersByOrderId("order-123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByOrderId("order-123");
    }

    @Test
    void getOrdersByOrderId_WithNonExistingOrder_ShouldReturnEmptyList() {
        // Arrange
        when(orderRepository.findByOrderId("unknown-order")).thenReturn(Optional.empty());

        // Act
        List<OrderDto> result = orderService.getOrdersByOrderId("unknown-order");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByOrderId("unknown-order");
        verify(orderItemRepository, never()).findByOrderOrderId(anyString());
    }

    @Test
    void getOrdersByUserId_ShouldReturnUserOrders() {
        // Arrange
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(testOrder));
        when(orderItemRepository.findByOrderOrderId("order-123")).thenReturn(List.of(testOrderItem));

        // Act
        List<OrderDto> result = orderService.getOrdersByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByUserId(1L);
    }

    @Test
    void findOrderWithItems_WithExistingOrder_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findByOrderId("order-123")).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.findOrderWithItems("order-123");

        // Assert
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(orderRepository, times(1)).findByOrderId("order-123");
    }

    @Test
    void findOrderWithItems_WithNonExistingOrder_ShouldReturnNull() {
        // Arrange
        when(orderRepository.findByOrderId("unknown-order")).thenReturn(Optional.empty());

        // Act
        Order result = orderService.findOrderWithItems("unknown-order");

        // Assert
        assertNull(result);
        verify(orderRepository, times(1)).findByOrderId("unknown-order");
    }
}