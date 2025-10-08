package com.example.NotificationService.controller;

import com.example.NotificationService.dto.OrderDto;
import com.example.NotificationService.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для OrderController Notification Service.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getAllOrders_WithOrders_ShouldReturnOrderList() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        when(orderService.getAllOrders()).thenReturn(List.of(orderDto));

        // Act & Assert
        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value("order-123"))
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    void getAllOrders_WithNoOrders_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOrderById_WithExistingOrder_ShouldReturnOrder() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        when(orderService.getOrdersByOrderId("order-123")).thenReturn(List.of(orderDto));

        // Act & Assert
        mockMvc.perform(get("/api/orders/order-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void getOrderById_WithNonExistingOrder_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.getOrdersByOrderId("unknown-order")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/orders/unknown-order"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByUserId_WithExistingUser_ShouldReturnOrders() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(orderDto));

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value("order-123"));
    }

    @Test
    void getOrdersByUserId_WithNoOrders_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(orderService.getOrdersByUserId(999L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private OrderDto createTestOrderDto() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setOrderId("order-123");
        orderDto.setUserId(1L);
        orderDto.setTotalPrice(BigDecimal.valueOf(150.0));
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setItems(Collections.emptyList());
        return orderDto;
    }
}