package com.example.InventoryService.dto;

import lombok.*;

@Getter
@Setter
@Data
public class ProductAvailability {
    private Long productId;
    private String name;
    private Double price;
    private Double sale;
    private Integer quantity;
    private Boolean isAvailable;

    public ProductAvailability() {
    }

    public ProductAvailability(Long productId, String name, Double price, Double sale, Integer quantity, Boolean isAvailable) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.sale = sale;
        this.quantity = quantity;
        this.isAvailable = isAvailable;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSale() {
        return sale;
    }

    public void setSale(Double sale) {
        this.sale = sale;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
