package com.example.NotificationService.mapper;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между Kafka сообщениями и сущностями базы данных.
 * Обрабатывает преобразование данных и установку значений по умолчанию.
 */
@Component
public class OrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Преобразует Kafka сообщение в сущность Order.
     *
     * @param kafkaMessage сообщение из Kafka
     * @return сущность заказа
     */
    public Order toOrderEntity(KafkaOrderMessage kafkaMessage) {
        Order order = new Order();
        order.setOrderId(kafkaMessage.getOrderId());
        order.setUserId(kafkaMessage.getUserId());
        order.setTotalPrice(getSafeBigDecimal(kafkaMessage.getTotalPrice()));
        order.setOrderDate(parseOrderDate(kafkaMessage.getOrderDate()));
        return order;
    }

    /**
     * Преобразует Kafka сообщение в список сущностей OrderItem.
     *
     * @param kafkaMessage сообщение из Kafka
     * @param order родительский заказ
     * @return список сущностей товаров заказа
     */
    public List<OrderItem> toOrderItemEntities(KafkaOrderMessage kafkaMessage, Order order) {
        if (kafkaMessage.getItems() == null) {
            return List.of();
        }

        return kafkaMessage.getItems().stream()
                .map(itemMessage -> toOrderItemEntity(itemMessage, order))
                .collect(Collectors.toList());
    }

    /**
     * Преобразует сообщение о товаре в сущность OrderItem.
     *
     * @param itemMessage сообщение о товаре
     * @param order родительский заказ
     * @return сущность товара заказа
     */
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

    /**
     * Парсит строку даты в LocalDateTime.
     *
     * @param orderDate строка с датой
     * @return LocalDateTime или текущее время при ошибке парсинга
     */
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

    /**
     * Возвращает безопасное Long значение.
     *
     * @param value исходное значение
     * @return исходное значение или 0L если null
     */
    private Long getSafeLong(Long value) {
        return value != null ? value : 0L;
    }

    /**
     * Возвращает безопасное Integer значение.
     *
     * @param value исходное значение
     * @return исходное значение или 0 если null
     */
    private Integer getSafeInteger(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * Возвращает безопасное BigDecimal значение.
     *
     * @param value исходное значение
     * @return исходное значение или BigDecimal.ZERO если null
     */
    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
} 
