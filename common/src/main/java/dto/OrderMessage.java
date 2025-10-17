package dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Общее сообщение для Kafka, используемое Order Service и Notification Service
 */

public class OrderMessage {
    private String orderId;
    private Long userId;
    private String username;
    private BigDecimal totalPrice;
    private String orderDate;
    private List<OrderItemMessage> items;

    public OrderMessage() {
    }

    public OrderMessage(String orderId, Long userId, String username, BigDecimal totalPrice, String orderDate, List<OrderItemMessage> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.username = username;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItemMessage> getItems() {
        return items;
    }

    public void setItems(List<OrderItemMessage> items) {
        this.items = items;
    }

    public static class OrderItemMessage {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal discount;
        private BigDecimal itemTotal;

        public OrderItemMessage(Long productId, Integer quantity, BigDecimal price, BigDecimal discount, BigDecimal itemTotal) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
            this.discount = discount;
            this.itemTotal = itemTotal;
        }

        public OrderItemMessage() {
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public void setDiscount(BigDecimal discount) {
            this.discount = discount;
        }

        public BigDecimal getItemTotal() {
            return itemTotal;
        }

        public void setItemTotal(BigDecimal itemTotal) {
            this.itemTotal = itemTotal;
        }
    }
}