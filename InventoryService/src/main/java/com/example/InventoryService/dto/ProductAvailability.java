package com.example.InventoryService.dto;

import lombok.*;

@Getter
@Setter
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
