package com.example.InventoryService.mapper;

import com.example.InventoryService.entity.ProductEntity;
import com.example.inventory.ProductResponseItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
    void toProductResponseItem_WithValidProduct_ShouldReturnCorrectResponse() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setName("Test Product");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setSale(BigDecimal.valueOf(0.1));

        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(product);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals("Test Product", response.getName());
        assertEquals(10, response.getAvailableQuantity());
        assertEquals(0, response.getRequestedQuantity()); // По умолчанию 0
        assertEquals(100.0, response.getPrice());
        assertEquals(0.1, response.getSale());
        assertTrue(response.getIsAvailable());
    }

    @Test
    void toProductResponseItem_WithZeroQuantity_ShouldReturnNotAvailable() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(2L);
        product.setName("Out of Stock");
        product.setQuantity(0);
        product.setPrice(BigDecimal.valueOf(50.0));
        product.setSale(BigDecimal.valueOf(0.0));

        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(product);

        // Assert
        assertNotNull(response);
        assertFalse(response.getIsAvailable());
        assertEquals(0, response.getAvailableQuantity());
    }

    @Test
    void toProductResponseItem_WithNullProduct_ShouldReturnDefaultResponse() {
        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(null);

        // Assert
        assertNotNull(response);
        assertEquals(0L, response.getProductId());
        assertEquals("", response.getName());
        assertEquals(0, response.getAvailableQuantity());
        assertEquals(0, response.getRequestedQuantity());
        assertEquals(0.0, response.getPrice());
        assertEquals(0.0, response.getSale());
        assertFalse(response.getIsAvailable());
    }

    @Test
    void toProductResponseItem_WithNullValues_ShouldUseDefaults() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(3L);
        // name, quantity, price, sale are null

        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(product);

        // Assert
        assertNotNull(response);
        assertEquals(3L, response.getProductId());
        assertEquals("", response.getName());
        assertEquals(0, response.getAvailableQuantity());
        assertEquals(0.0, response.getPrice());
        assertEquals(0.0, response.getSale());
        assertFalse(response.getIsAvailable());
    }

    @Test
    void toProductResponseItem_WithRequestedQuantity_ShouldReturnCorrectResponse() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setName("Test Product");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setSale(BigDecimal.valueOf(0.1));

        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(product, 5);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals("Test Product", response.getName());
        assertEquals(10, response.getAvailableQuantity());
        assertEquals(5, response.getRequestedQuantity());
        assertEquals(100.0, response.getPrice());
        assertEquals(0.1, response.getSale());
        assertTrue(response.getIsAvailable());
    }

    @Test
    void toProductResponseItem_WithRequestedQuantityExceedingAvailable_ShouldReturnNotAvailable() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setName("Test Product");
        product.setQuantity(5);
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setSale(BigDecimal.valueOf(0.1));

        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(product, 10);

        // Assert
        assertNotNull(response);
        assertEquals(5, response.getAvailableQuantity());
        assertEquals(10, response.getRequestedQuantity());
        assertFalse(response.getIsAvailable());
    }

    @Test
    void toProductResponseItem_WithZeroRequestedQuantity_ShouldReturnAvailable() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setName("Test Product");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setSale(BigDecimal.valueOf(0.1));

        // Act
        ProductResponseItem response = grpcMapper.toProductResponseItem(product, 0);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getRequestedQuantity());
        assertTrue(response.getIsAvailable()); // Товар доступен, даже если запрошено 0
    }
}