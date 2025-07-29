package com.example.NotificationService.repository;

import com.example.NotificationService.entity.dto.OrderDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderDto,Long> {

}
