package com.example.NotificationService.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class KafkaOrderMessage {
    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;

    @JsonProperty("orderDate")
    private String orderDate;

    @JsonProperty("items")
    private List<OrderItemMessage> items;

    // Геттеры и сеттеры
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public List<OrderItemMessage> getItems() { return items; }
    public void setItems(List<OrderItemMessage> items) { this.items = items; }

    public static class OrderItemMessage {
        @JsonProperty("productId")
        private Long productId;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("price")
        private BigDecimal price;

        @JsonProperty("discount")
        private BigDecimal discount;

        @JsonProperty("itemTotal")
        private BigDecimal itemTotal;

        // Геттеры и сеттеры
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }

        public BigDecimal getItemTotal() { return itemTotal; }
        public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }
    }
}