package com.example.InventoryService.mapper;

import com.example.InventoryService.model.ProductEntity;
import com.example.inventory.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class GrpcMapper {

    public ProductResponse toProductResponse(ProductEntity product) {
        return ProductResponse.newBuilder()
                .setProductId(product.getId())
                .setName(getSafeString(product.getName()))
                .setQuantity(getSafeInteger(product.getQuantity()))
                .setPrice(getSafeDouble(product.getPrice()))
                .setSale(getSafeDouble(product.getSale()))
                .setAvailable(isProductAvailable(product))
                .build();
    }

    private String getSafeString(String value) {
        return value != null ? value : "";
    }

    private int getSafeInteger(Integer value) {
        return value != null ? value : 0;
    }

    private double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private boolean isProductAvailable(ProductEntity product) {
        return product.getQuantity() != null && product.getQuantity() > 0;
    }
}