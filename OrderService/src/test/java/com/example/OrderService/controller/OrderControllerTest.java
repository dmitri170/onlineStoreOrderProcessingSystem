package com.example.OrderService.controller;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для OrderController.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_WithValidData_ShouldReturnSuccess() throws Exception {
        // Arrange
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(1L);
        item.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(item));

        when(orderService.processOrder(any(OrderRequest.class), anyString()))
                .thenReturn("order-123");

        // Act & Assert
        mockMvc.perform(post("/api/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Заказ успешно создан"))
                .andExpect(jsonPath("$.orderId").value("order-123"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of()); // Empty items list

        // Act & Assert
        mockMvc.perform(post("/api/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(1L);
        item.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(item));

        // Act & Assert
        mockMvc.perform(post("/api/order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnauthorized());
    }
}