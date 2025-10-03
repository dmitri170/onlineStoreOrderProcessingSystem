package com.example.NotificationService.controller;

import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.dto.OrderDto;
import com.example.NotificationService.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderDto>> getOrdersByOrderId(@PathVariable String orderId) {
        List<OrderDto> orders = orderService.getOrdersByOrderId(orderId);
        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable String orderId) {
        List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);
        if (items.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(items);
    }
}