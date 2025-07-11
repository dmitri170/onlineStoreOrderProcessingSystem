package com.example.NotificationService.controller;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ReadOrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/order/{orderId}")
    public List<Order> getByOrderId(@PathVariable Long orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getByUserId(@PathVariable Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
