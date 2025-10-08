package com.example.OrderService.message.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
/**
 * Модель сообщения для Kafka, представляющая заказ.
 * Содержит все необходимые данные для обработки заказа в Notification Service.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage {
    private String orderId;
    private Long userId;
    private String username;
    private BigDecimal totalPrice;
    private String orderDate;
    private List<OrderItemMessage> items;


    /**
     * Модель сообщения для одного товара в заказе.
     * Содержит детальную информацию о товаре и его стоимости.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
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

    }
}