package com.example.NotificationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private String orderId;
    private Long userId;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private List<OrderItemDto> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemDto {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal discount;
        private BigDecimal itemTotal;

    }
}