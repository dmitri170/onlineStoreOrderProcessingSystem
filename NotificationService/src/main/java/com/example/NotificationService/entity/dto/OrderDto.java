package com.example.NotificationService.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private List<OrderItemDto> items;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public static class OrderItemDto {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal sale;
        private BigDecimal itemTotal;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getSale() { return sale; }
        public void setSale(BigDecimal sale) { this.sale = sale; }
        public BigDecimal getItemTotal() { return itemTotal; }
        public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }
    }
}