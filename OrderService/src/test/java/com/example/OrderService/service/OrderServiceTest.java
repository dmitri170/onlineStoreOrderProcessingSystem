package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.entity.User;
import com.example.OrderService.exception.InsufficientStockException;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Юнит тесты для сервиса заказов.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private OrderProducer orderProducer;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private OrderRequest validOrderRequest;
    private ProductResponse availableProduct;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "test@example.com", com.example.OrderService.entity.Role.USER);
        testUser.setId(1L);

        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(1L);
        item.setQuantity(2);

        validOrderRequest = new OrderRequest();
        validOrderRequest.setItems(List.of(item));

        availableProduct = ProductResponse.newBuilder()
                .setProductId(1L)
                .setName("Test Product")
                .setQuantity(10)
                .setPrice(100.0)
                .setSale(0.1)
                .setAvailable(true)
                .build();
    }

    @Test
    void processOrder_WithValidData_ShouldSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryClient.checkAvailability(1L)).thenReturn(availableProduct);
        doNothing().when(orderProducer).sendOrder(any(OrderMessage.class));

        // Act
        String orderId = orderService.processOrder(validOrderRequest, "testuser");

        // Assert
        assertNotNull(orderId);
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(inventoryClient, times(1)).checkAvailability(1L);
        verify(orderProducer, times(1)).sendOrder(any(OrderMessage.class));
    }

    @Test
    void processOrder_WithUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.processOrder(validOrderRequest, "unknown");
        });

        verify(userRepository, times(1)).findByUsername("unknown");
        verify(inventoryClient, never()).checkAvailability(any());
        verify(orderProducer, never()).sendOrder(any());
    }

    @Test
    void processOrder_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        ProductResponse outOfStockProduct = ProductResponse.newBuilder()
                .setProductId(1L)
                .setQuantity(1) // Only 1 available, but requested 2
                .setPrice(100.0)
                .setSale(0.0)
                .setAvailable(true)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryClient.checkAvailability(1L)).thenReturn(outOfStockProduct);

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> {
            orderService.processOrder(validOrderRequest, "testuser");
        });

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(inventoryClient, times(1)).checkAvailability(1L);
        verify(orderProducer, never()).sendOrder(any());
    }

    @Test
    void processOrder_WithMultipleItems_ShouldProcessAllItems() {
        // Arrange
        OrderItemDTO item1 = new OrderItemDTO();
        item1.setProductId(1L);
        item1.setQuantity(1);

        OrderItemDTO item2 = new OrderItemDTO();
        item2.setProductId(2L);
        item2.setQuantity(3);

        OrderRequest multiItemRequest = new OrderRequest();
        multiItemRequest.setItems(List.of(item1, item2));

        ProductResponse product2 = ProductResponse.newBuilder()
                .setProductId(2L)
                .setQuantity(5)
                .setPrice(50.0)
                .setSale(0.0)
                .setAvailable(true)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryClient.checkAvailability(1L)).thenReturn(availableProduct);
        when(inventoryClient.checkAvailability(2L)).thenReturn(product2);
        doNothing().when(orderProducer).sendOrder(any(OrderMessage.class));

        // Act
        String orderId = orderService.processOrder(multiItemRequest, "testuser");

        // Assert
        assertNotNull(orderId);
        verify(inventoryClient, times(1)).checkAvailability(1L);
        verify(inventoryClient, times(1)).checkAvailability(2L);
        verify(orderProducer, times(1)).sendOrder(any(OrderMessage.class));
    }
}