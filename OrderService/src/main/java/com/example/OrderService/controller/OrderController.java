package com.example.OrderService.controller;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        String orderId = orderService.processOrder(request, username);
        return ResponseEntity.ok("Order created successfully with ID: " + orderId);
    }
}