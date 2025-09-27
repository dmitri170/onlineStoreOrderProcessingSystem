package com.example.OrderService.controller;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        String orderId = orderService.processOrder(request, username);
        return ResponseEntity.ok(Map.of(
                "message", "Order created successfully",
                "orderId", orderId
        ));
    }
}