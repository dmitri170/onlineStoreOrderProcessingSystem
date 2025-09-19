package com.example.NotificationService.controller;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.repository.OrderRepository;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Добавить (отсутствует в вашем коде)
    @GetMapping("/{order_id}")
    public List<Order> getOrdersByOrderId(@PathVariable("order_id") @Min(1) Long orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    // Добавить (отсутствует в вашем коде)
    @GetMapping("/user/{user_id}")
    public List<Order> getOrdersByUserId(@PathVariable("user_id") Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
