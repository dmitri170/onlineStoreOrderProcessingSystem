package com.example.InventoryService.dto;

import lombok.*;

@Getter
@Setter
@Data

public class ProductDto {
    private Long id;
    private String name;
    private Integer quantity;
    private Double price;
    private Double sale;

    public ProductDto() {
    }

    public ProductDto(Long id, String name, Integer quantity, Double price, Double sale) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.sale = sale;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
}
