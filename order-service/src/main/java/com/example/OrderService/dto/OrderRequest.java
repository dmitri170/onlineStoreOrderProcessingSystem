package com.example.OrderService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
/**
 * DTO для запроса создания нового заказа.
 * Содержит список товаров для заказа.
 */
@Data
public class OrderRequest {
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDTO> items;
} 
