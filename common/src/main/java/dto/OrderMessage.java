package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Общее сообщение для Kafka, используемое Order Service и Notification Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {
    private String orderId;
    private Long userId;
    private String username;
    private BigDecimal totalPrice;
    private String orderDate;
    private List<OrderItemMessage> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemMessage {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal discount;
        private BigDecimal itemTotal;
    }
}