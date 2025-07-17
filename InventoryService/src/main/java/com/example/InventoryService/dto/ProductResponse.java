package com.example.InventoryService.dto;

public record ProductResponse(
        Long id,
        String name,
        int quantity,
        double price,
        double sale
) {}
