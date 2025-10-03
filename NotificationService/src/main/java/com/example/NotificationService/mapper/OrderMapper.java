package com.example.NotificationService.mapper;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.dto.KafkaOrderMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Order toOrderEntity(KafkaOrderMessage kafkaMessage) {
        Order order = new Order();
        order.setOrderId(kafkaMessage.getOrderId());
        order.setUserId(kafkaMessage.getUserId());
        order.setTotalPrice(getSafeBigDecimal(kafkaMessage.getTotalPrice()));
        order.setOrderDate(parseOrderDate(kafkaMessage.getOrderDate()));
        return order;
    }

    public List<OrderItem> toOrderItemEntities(KafkaOrderMessage kafkaMessage, Order order) {
        if (kafkaMessage.getItems() == null) {
            return List.of();
        }

        return kafkaMessage.getItems().stream()
                .map(itemMessage -> toOrderItemEntity(itemMessage, order))
                .collect(Collectors.toList());
    }

    private OrderItem toOrderItemEntity(KafkaOrderMessage.OrderItemMessage itemMessage, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(getSafeLong(itemMessage.getProductId()));
        orderItem.setQuantity(getSafeInteger(itemMessage.getQuantity()));
        orderItem.setPrice(getSafeBigDecimal(itemMessage.getPrice()));
        orderItem.setDiscount(getSafeBigDecimal(itemMessage.getDiscount()));
        orderItem.setItemTotal(getSafeBigDecimal(itemMessage.getItemTotal()));
        return orderItem;
    }

    private LocalDateTime parseOrderDate(String orderDate) {
        if (orderDate == null) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(orderDate, DATE_FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private Long getSafeLong(Long value) {
        return value != null ? value : 0L;
    }

    private Integer getSafeInteger(Integer value) {
        return value != null ? value : 0;
    }

    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}