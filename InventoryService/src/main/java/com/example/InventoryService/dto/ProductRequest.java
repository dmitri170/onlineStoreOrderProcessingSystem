package com.example.InventoryService.dto;

import jakarta.validation.constraints.*;

public record ProductRequest(
        @NotBlank String name,
        @PositiveOrZero int quantity,
        @Positive double price,
        @DecimalMin("0.0") @DecimalMax("1.0") double sale
) {}
