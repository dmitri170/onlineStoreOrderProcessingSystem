package com.example.InventoryService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для информации о доступности товара.
 * Используется для проверки наличия товаров перед созданием заказа.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailability {
    /** Идентификатор товара */
    private Long productId;

    /** Название товара */
    private String name;

    /** Цена товара */
    private BigDecimal price;

    /** Скидка на товар */
    private BigDecimal sale;

    /** Количество товара на складе */
    private Integer quantity;

    /** Доступен ли товар для заказа */
    private Boolean isAvailable;
} 
