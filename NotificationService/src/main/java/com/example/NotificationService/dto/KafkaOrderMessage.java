package com.example.NotificationService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO для сообщений Kafka от Order Service.
 * Содержит полную информацию о заказе для сохранения в базу данных.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    /**
     * DTO для элемента заказа в сообщении Kafka.
     * Содержит информацию об отдельном товаре в заказе.
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
