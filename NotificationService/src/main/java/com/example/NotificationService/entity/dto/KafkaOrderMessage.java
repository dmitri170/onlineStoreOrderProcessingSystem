package com.example.NotificationService.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KafkaOrderMessage {
    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("items")
    private List<OrderItemMessage> items;

    @Data
    public static class OrderItemMessage {
        @JsonProperty("productId")
        private Long productId;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("price")
        private Double price;

        @JsonProperty("sale")
        private Double sale;
    }
}