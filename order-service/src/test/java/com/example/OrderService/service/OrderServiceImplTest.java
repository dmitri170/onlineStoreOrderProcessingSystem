package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.entity.User;
import com.example.OrderService.exception.ProductsUnavailableException;
import com.example.OrderService.exception.UserNotFoundException;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.BulkProductResponse;
import com.example.inventory.ProductResponseItem;
import dto.OrderMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Юнит тесты для сервиса заказов.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private OrderProducer orderProducer;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private User testUser;
    private OrderRequest validOrderRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");

        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(1L);
        item.setQuantity(2);

        validOrderRequest = new OrderRequest();
        validOrderRequest.setItems(List.of(item));
    }

    @Test
    void processOrder_WithValidData_ShouldSuccess() {
        // Arrange
        BulkProductResponse bulkResponse = BulkProductResponse.newBuilder()
                .setRqUid("test-uuid")
                .addAvailableItems(ProductResponseItem.newBuilder()
                        .setProductId(1L)
                        .setName("Test Product")
                        .setAvailableQuantity(10)
                        .setRequestedQuantity(2)
                        .setPrice(100.0)
                        .setSale(0.1)
                        .setIsAvailable(true)
                        .build())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryClient.checkBulkAvailability(anyList(), anyString())).thenReturn(bulkResponse);
        when(inventoryClient.reserveProducts(anyString(), anyList())).thenReturn(
                com.example.inventory.ReserveProductsResponse.newBuilder()
                        .setSuccess(true)
                        .build()
        );
        doNothing().when(orderProducer).sendOrder(any(OrderMessage.class));

        // Act
        String orderId = orderServiceImpl.processOrder(validOrderRequest, "testuser");

        // Assert
        assertNotNull(orderId);
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(inventoryClient, times(1)).checkBulkAvailability(anyList(), anyString());
        verify(inventoryClient, times(1)).reserveProducts(anyString(), anyList());
        verify(orderProducer, times(1)).sendOrder(any(OrderMessage.class));
    }

    @Test
    void processOrder_WithUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            orderServiceImpl.processOrder(validOrderRequest, "unknown");
        });

        verify(userRepository, times(1)).findByUsername("unknown");
        verify(inventoryClient, never()).checkBulkAvailability(anyList(), anyString());
        verify(orderProducer, never()).sendOrder(any());
    }

    @Test
    void processOrder_WithUnavailableProducts_ShouldThrowException() {
        // Arrange
        BulkProductResponse bulkResponse = BulkProductResponse.newBuilder()
                .setRqUid("test-uuid")
                .addUnavailableItems(ProductResponseItem.newBuilder()
                        .setProductId(1L)
                        .setName("Test Product")
                        .setAvailableQuantity(1)
                        .setRequestedQuantity(2)
                        .setPrice(100.0)
                        .setSale(0.1)
                        .setIsAvailable(false)
                        .build())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryClient.checkBulkAvailability(anyList(), anyString())).thenReturn(bulkResponse);

        // Act & Assert
        assertThrows(ProductsUnavailableException.class, () -> {
            orderServiceImpl.processOrder(validOrderRequest, "testuser");
        });

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(inventoryClient, times(1)).checkBulkAvailability(anyList(), anyString());
        verify(inventoryClient, never()).reserveProducts(anyString(), anyList());
        verify(orderProducer, never()).sendOrder(any());
    }

    @Test
    void processOrder_WithReservationFailure_ShouldThrowException() {
        // Arrange
        BulkProductResponse bulkResponse = BulkProductResponse.newBuilder()
                .setRqUid("test-uuid")
                .addAvailableItems(ProductResponseItem.newBuilder()
                        .setProductId(1L)
                        .setName("Test Product")
                        .setAvailableQuantity(10)
                        .setRequestedQuantity(2)
                        .setPrice(100.0)
                        .setSale(0.1)
                        .setIsAvailable(true)
                        .build())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryClient.checkBulkAvailability(anyList(), anyString())).thenReturn(bulkResponse);
        when(inventoryClient.reserveProducts(anyString(), anyList())).thenReturn(
                com.example.inventory.ReserveProductsResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Reservation failed")
                        .build()
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderServiceImpl.processOrder(validOrderRequest, "testuser");
        });

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(inventoryClient, times(1)).checkBulkAvailability(anyList(), anyString());
        verify(inventoryClient, times(1)).reserveProducts(anyString(), anyList());
        verify(orderProducer, never()).sendOrder(any());
    }
}