package com.example.NotificationService.entity.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private String orderId;           // ID всего заказа
    private Long userId;
    private BigDecimal totalPrice;    // Общая сумма всего заказа
    private LocalDateTime orderDate;
    private List<OrderItemDto> items; // Список товаров в заказе

    @Data
    public static class OrderItemDto {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal sale;
        private BigDecimal itemTotal; // Сумма для этой позиции
    }
}
