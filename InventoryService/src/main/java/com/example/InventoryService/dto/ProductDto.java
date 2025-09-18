package com.example.InventoryService.dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private Integer quantity;
    private Double price;
    private Double sale;

}
