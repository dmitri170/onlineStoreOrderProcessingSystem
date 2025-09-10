package com.example.InventoryService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAvailability {
    private Long productId;
    private String name;
    private Double price;
    private Double sale;
    private Integer quantity;
    private Boolean isAvailable;
}
