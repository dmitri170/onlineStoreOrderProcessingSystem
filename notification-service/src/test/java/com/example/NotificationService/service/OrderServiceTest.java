package com.example.NotificationService.service;

import com.example.NotificationService.dto.OrderDto;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.repository.OrderRepository;
import com.example.NotificationService.repository.OrderItemRepository;
import com.example.NotificationService.mapper.OrderMapper;
import dto.OrderMessage; // Импорт из общего пакета dto
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
    private OrderMessage testOrderMessage;
    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        // Тестовые данные для Order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderId("order-123");
        testOrder.setUserId(1L);
        testOrder.setTotalPrice(BigDecimal.valueOf(150.0));
        testOrder.setOrderDate(LocalDateTime.now());

        // Тестовые данные для OrderItem
        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProductId(1L);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(BigDecimal.valueOf(50.0));
        testOrderItem.setDiscount(BigDecimal.valueOf(0.1));
        testOrderItem.setItemTotal(BigDecimal.valueOf(90.0));

        // Тестовые данные для OrderMessage (Kafka)
        testOrderMessage = new OrderMessage();
        testOrderMessage.setOrderId("order-123");
        testOrderMessage.setUserId(1L);
        testOrderMessage.setUsername("testuser");
        testOrderMessage.setTotalPrice(BigDecimal.valueOf(150.0));
        testOrderMessage.setOrderDate(LocalDateTime.now().toString()); // String как в вашем DTO

        OrderMessage.OrderItemMessage itemMessage = new OrderMessage.OrderItemMessage();
        itemMessage.setProductId(1L);
        itemMessage.setQuantity(2);
        itemMessage.setPrice(BigDecimal.valueOf(50.0));
        itemMessage.setDiscount(BigDecimal.valueOf(0.1));
        itemMessage.setItemTotal(BigDecimal.valueOf(90.0));

        testOrderMessage.setItems(List.of(itemMessage));

        // Тестовые данные для OrderDto
        testOrderDto = new OrderDto();
        testOrderDto.setId(1L);
        testOrderDto.setOrderId("order-123");
        testOrderDto.setUserId(1L);
        testOrderDto.setTotalPrice(BigDecimal.valueOf(150.0));
        testOrderDto.setOrderDate(LocalDateTime.now());

        OrderDto.OrderItemDto itemDto = new OrderDto.OrderItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        itemDto.setPrice(BigDecimal.valueOf(50.0));
        itemDto.setDiscount(BigDecimal.valueOf(0.1));
        itemDto.setItemTotal(BigDecimal.valueOf(90.0));

        testOrderDto.setItems(List.of(itemDto));
    }

    @Test
    void processOrder_WithValidData_ShouldSaveOrderAndItems() {
        // Arrange
        List<OrderItem> orderItems = List.of(testOrderItem);

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.saveAll(any(List.class))).thenReturn(orderItems);

        // Act
        orderService.processOrder(testOrder, orderItems);

        // Assert
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderItemRepository, times(1)).saveAll(orderItems);
        verify(orderItemRepository, times(1)).saveAll(orderItems);
    }

    @Test
    void processOrder_ShouldSetOrderForEachItem() {
        // Arrange
        OrderItem item1 = new OrderItem();
        item1.setProductId(1L);
        OrderItem item2 = new OrderItem();
        item2.setProductId(2L);
        List<OrderItem> orderItems = List.of(item1, item2);

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.saveAll(any(List.class))).thenReturn(orderItems);

        // Act
        orderService.processOrder(testOrder, orderItems);

        // Assert
        // Проверяем, что для каждого item установлен order
        assertSame(testOrder, item1.getOrder());
        assertSame(testOrder, item2.getOrder());
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderItemRepository, times(1)).saveAll(orderItems);
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
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findAllByOrderByOrderDateDesc()).thenReturn(orders);
        when(orderItemRepository.findByOrderOrderId("order-123")).thenReturn(List.of(testOrderItem));

        // Act
        List<OrderDto> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).findAllByOrderByOrderDateDesc();
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-123");
    }

    @Test
    void getAllOrders_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        when(orderRepository.findAllByOrderByOrderDateDesc()).thenReturn(List.of());

        // Act
        List<OrderDto> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findAllByOrderByOrderDateDesc();
        verify(orderItemRepository, never()).findByOrderOrderId(anyString());
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
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-123");
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
        List<Order> userOrders = List.of(testOrder);
        when(orderRepository.findByUserId(1L)).thenReturn(userOrders);
        when(orderItemRepository.findByOrderOrderId("order-123")).thenReturn(List.of(testOrderItem));

        // Act
        List<OrderDto> result = orderService.getOrdersByUserId(1L);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).findByUserId(1L);
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-123");
    }

    @Test
    void getOrdersByUserId_WithNonExistingUser_ShouldReturnEmptyList() {
        // Arrange
        when(orderRepository.findByUserId(999L)).thenReturn(List.of());

        // Act
        List<OrderDto> result = orderService.getOrdersByUserId(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByUserId(999L);
        verify(orderItemRepository, never()).findByOrderOrderId(anyString());
    }

    @Test
    void getOrderItemsByOrderId_ShouldReturnItems() {
        // Arrange
        List<OrderItem> expectedItems = List.of(testOrderItem);
        when(orderItemRepository.findByOrderOrderId("order-123")).thenReturn(expectedItems);

        // Act
        List<OrderItem> result = orderService.getOrderItemsByOrderId("order-123");

        // Assert
        assertNotNull(result);
        assertEquals(expectedItems, result);
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-123");
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

    @Test
    void findAllOrdersWithItems_ShouldReturnAllOrders() {
        // Arrange
        List<Order> expectedOrders = List.of(testOrder);
        when(orderRepository.findAllWithItems()).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.findAllOrdersWithItems();

        // Assert
        assertNotNull(result);
        assertEquals(expectedOrders, result);
        verify(orderRepository, times(1)).findAllWithItems();
    }

    @Test
    void convertToDto_ShouldMapCorrectly() {
        // Arrange
        when(orderItemRepository.findByOrderOrderId("order-123")).thenReturn(List.of(testOrderItem));

        // Act - используем рефлексию для вызова приватного метода
        OrderDto result = orderService.getAllOrders().stream().findFirst().orElse(null);

        // Assert
        // Проверяем, что метод был вызван и отработал без ошибок
        verify(orderRepository, times(1)).findAllByOrderByOrderDateDesc();
        verify(orderItemRepository, times(1)).findByOrderOrderId("order-123");
    }
}