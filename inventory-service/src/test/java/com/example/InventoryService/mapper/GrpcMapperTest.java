package com.example.InventoryService.mapper;

import com.example.InventoryService.entity.ProductEntity;
import com.example.inventory.ProductResponse;
import com.example.inventory.ProductResponseItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Юнит тесты для GrpcMapper.
 */
class GrpcMapperTest {

    private GrpcMapper grpcMapper;

    @BeforeEach
    void setUp() {
        grpcMapper = new GrpcMapper();
    }

    @Test
    void toProductResponse_WithValidProduct_ShouldReturnCorrectResponse() {
        // Arrange
        ProductEntity product = new ProductEntity("Test Product", 10, BigDecimal.valueOf(100.0), BigDecimal.valueOf(0.1));
        product.setId(1L);

        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(product);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals("Test Product", response.getName());
        assertEquals(10, response.getQuantity());
        assertEquals(100.0, response.getPrice());
        assertEquals(0.1, response.getSale());
        assertTrue(response.getAvailable());
    }

    @Test
    void toProductResponse_WithZeroQuantity_ShouldReturnNotAvailable() {
        // Arrange
        ProductEntity product = new ProductEntity("Out of Stock", 0, BigDecimal.valueOf(50.0), BigDecimal.valueOf(0.0));
        product.setId(2L);

        // Act
        ProductResponse response = grpcMapper.toProductResponse(product);

        // Assert
        assertNotNull(response);
        assertFalse(response.getAvailable());
        assertEquals(0, response.getQuantity());
    }

    @Test
    void toProductResponse_WithNullValues_ShouldUseDefaults() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(3L);
        // name, quantity, price, sale are null

        // Act
        ProductResponse response = grpcMapper.toProductResponse(product);

        // Assert
        assertNotNull(response);
        assertEquals("", response.getName());
        assertEquals(0, response.getQuantity());
        assertEquals(0.0, response.getPrice());
        assertEquals(0.0, response.getSale());
        assertFalse(response.getAvailable());
    }
}