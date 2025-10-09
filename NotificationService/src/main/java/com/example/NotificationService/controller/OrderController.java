package com.example.NotificationService.controller;

import com.example.NotificationService.dto.OrderDto;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для предоставления read-only доступа к данным заказов.
 * Используется для аналитики и отчетности.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Конструктор контроллера заказов.
     *
     * @param orderService сервис для работы с заказами
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Возвращает все заказы с информацией о товарах в виде DTO.
     *
     * @return список всех заказов
     */
    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Находит заказ по уникальному идентификатору с информацией о товарах.
     *
     * @param orderId уникальный идентификатор заказа
     * @return заказ с товарами или 404 если не найден
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String orderId) {
        List<OrderDto> orders = orderService.getOrdersByOrderId(orderId);
        if (!orders.isEmpty()) {
            return ResponseEntity.ok(orders.get(0));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Находит все заказы конкретного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список заказов пользователя
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
} 
