package com.example.NotificationService.repository;

import com.example.NotificationService.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderEntity,Long> {

}
