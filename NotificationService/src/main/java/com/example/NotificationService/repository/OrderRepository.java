package com.example.NotificationService.repository;

import com.example.NotificationService.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);
    List<Order> findByUserId(Long userId);
}
