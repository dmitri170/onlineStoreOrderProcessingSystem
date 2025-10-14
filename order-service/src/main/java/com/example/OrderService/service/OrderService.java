package com.example.OrderService.service;

import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.dto.OrderRequest;

public interface OrderService {
    public String processOrder(OrderRequest request, String username);
}
