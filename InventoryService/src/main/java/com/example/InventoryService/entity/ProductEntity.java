package com.example.InventoryService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Сущность товара в системе инвентаризации.
 * Хранит информацию о товаре, его количестве, цене и скидках.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal sale;


    public ProductEntity(String name, Integer quantity, BigDecimal price, BigDecimal sale) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.sale = sale;
    }
}