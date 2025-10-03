package com.example.NotificationService.service;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.mapper.OrderMapper;
import com.example.NotificationService.repository.OrderRepository;
import com.example.NotificationService.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public void processOrder(Order order, List<OrderItem> orderItems) {
        // Сохраняем заказ и items в ОДНОЙ транзакции
        Order savedOrder = orderRepository.save(order);

        // Устанавливаем связь с сохраненным заказом
        orderItems.forEach(item -> item.setOrder(savedOrder));

        orderItemRepository.saveAll(orderItems);

        log.info("Successfully saved order {} with {} items",
                savedOrder.getOrderId(), orderItems.size());
    }

    public boolean orderExists(String orderId) {
        return orderRepository.findByOrderId(orderId).isPresent();
    }
}