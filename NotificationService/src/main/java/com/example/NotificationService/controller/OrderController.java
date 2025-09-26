package com.example.NotificationService.controller;

import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.entity.dto.OrderDto;
import com.example.NotificationService.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/all")
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public List<OrderDto> getOrdersByOrderId(@PathVariable String orderId) {
        return orderService.getOrdersByOrderId(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<OrderDto> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable String orderId) {
        return orderService.getOrderItemsByOrderId(orderId);
    }
}