package com.example.NotificationService.entity.dto;


import com.example.NotificationService.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private String orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal sale;
    private BigDecimal totalPrice;
    private Long userId;
}
