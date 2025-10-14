package com.example.OrderService.exception;

import java.util.List;

public class ProductsUnavailableException extends RuntimeException {
    private final List<String> unavailableProducts;

    public ProductsUnavailableException(String message, List<String> unavailableProducts) {
        super(message);
        this.unavailableProducts = unavailableProducts;
    }

    public List<String> getUnavailableProducts() {
        return unavailableProducts;
    }
}
