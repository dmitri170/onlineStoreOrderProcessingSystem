package com.example.InventoryService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о товаре через REST API.
 * Содержит валидацию входных данных.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    /** Идентификатор товара */
    private Long id;

    /** Название товара (обязательное поле) */
    @NotBlank(message = "Название товара обязательно")
    private String name;

    /** Количество товара (не может быть отрицательным) */
    @NotNull(message = "Количество обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private Integer quantity;

    /** Цена товара (не может быть отрицательной) */
    @NotNull(message = "Цена обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Double price;

    /** Скидка на товар (не может быть отрицательной) */
    @NotNull(message = "Скидка обязательна")
    @Min(value = 0, message = "Скидка не может быть отрицательной")
    private Double sale;

    /**
     * @return название товара
     */
    public @NotBlank(message = "Название товара обязательно") String getName() {
        return name;
    }

    /**
     * @param name название товара
     */
    public void setName(@NotBlank(message = "Название товара обязательно") String name) {
        this.name = name;
    }

    /**
     * @return количество товара
     */
    public @NotNull(message = "Количество обязательно") @Min(value = 0, message = "Количество не может быть отрицательным") Integer getQuantity() {
        return quantity;
    }

    /**
     * @param quantity количество товара
     */
    public void setQuantity(@NotNull(message = "Количество обязательно") @Min(value = 0, message = "Количество не может быть отрицательным") Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * @return цена товара
     */
    public @NotNull(message = "Цена обязательна") @Min(value = 0, message = "Цена не может быть отрицательной") Double getPrice() {
        return price;
    }

    /**
     * @param price цена товара
     */
    public void setPrice(@NotNull(message = "Цена обязательна") @Min(value = 0, message = "Цена не может быть отрицательной") Double price) {
        this.price = price;
    }

    /**
     * @return скидка на товар
     */
    public @NotNull(message = "Скидка обязательна") @Min(value = 0, message = "Скидка не может быть отрицательной") Double getSale() {
        return sale;
    }

    /**
     * @param sale скидка на товар
     */
    public void setSale(@NotNull(message = "Скидка обязательна") @Min(value = 0, message = "Скидка не может быть отрицательной") Double sale) {
        this.sale = sale;
    }
}