package com.example.NotificationService.repository;

import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.entity.dto.OrderDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderDto,Long> {
    List<OrderItem> findByOrderOrderId(String orderId);
}
