package com.example.InventoryService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductDto {
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    @NotNull(message = "Sale is required")
    @Min(value = 0, message = "Sale cannot be negative")
    private Double sale;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Product name is required") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Product name is required") String name) {
        this.name = name;
    }

    public @NotNull(message = "Quantity is required") @Min(value = 0, message = "Quantity cannot be negative") Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(@NotNull(message = "Quantity is required") @Min(value = 0, message = "Quantity cannot be negative") Integer quantity) {
        this.quantity = quantity;
    }

    public @NotNull(message = "Price is required") @Min(value = 0, message = "Price cannot be negative") Double getPrice() {
        return price;
    }

    public void setPrice(@NotNull(message = "Price is required") @Min(value = 0, message = "Price cannot be negative") Double price) {
        this.price = price;
    }

    public @NotNull(message = "Sale is required") @Min(value = 0, message = "Sale cannot be negative") Double getSale() {
        return sale;
    }

    public void setSale(@NotNull(message = "Sale is required") @Min(value = 0, message = "Sale cannot be negative") Double sale) {
        this.sale = sale;
    }
}