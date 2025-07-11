package com.example.OrderService.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemDTO> items;
}
