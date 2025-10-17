package com.example.InventoryService.service;

import com.example.InventoryService.dto.ProductDto;
import com.example.InventoryService.entity.ProductEntity;
import com.example.InventoryService.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Юнит тесты для сервиса товаров.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductService productService;

    private ProductEntity testProduct;
    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProduct = new ProductEntity();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setQuantity(10);
        testProduct.setPrice(BigDecimal.valueOf(100.0));
        testProduct.setSale(BigDecimal.valueOf(0.1));

        testProductDto = new ProductDto();
        testProductDto.setId(1L);
        testProductDto.setName("Test Product");
        testProductDto.setQuantity(10);
        testProductDto.setPrice(BigDecimal.valueOf(100.0));
        testProductDto.setSale(BigDecimal.valueOf(0.1));
    }

    @Test
    void getAllProducts_ShouldReturnProductList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(testProduct));
        when(modelMapper.map(testProduct, ProductDto.class)).thenReturn(testProductDto);

        // Act
        List<ProductDto> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDto, result.get(0));
        verify(productRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(testProduct, ProductDto.class);
    }

    @Test
    void getAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of());

        // Act
        List<ProductDto> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
        verify(modelMapper, never()).map(any(), eq(ProductDto.class));
    }

    @Test
    void getProductById_WithExistingId_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(modelMapper.map(testProduct, ProductDto.class)).thenReturn(testProductDto);

        // Act
        ProductDto result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDto, result);
        verify(productRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(testProduct, ProductDto.class);
    }

    @Test
    void getProductById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(999L);
        });

        assertEquals("Product not found with id: 999", exception.getMessage());
        verify(productRepository, times(1)).findById(999L);
        verify(modelMapper, never()).map(any(), eq(ProductDto.class));
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreatedProduct() {
        // Arrange
        when(modelMapper.map(testProductDto, ProductEntity.class)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(modelMapper.map(testProduct, ProductDto.class)).thenReturn(testProductDto);

        // Act
        ProductDto result = productService.createProduct(testProductDto);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDto, result);
        verify(modelMapper, times(1)).map(testProductDto, ProductEntity.class);
        verify(productRepository, times(1)).save(testProduct);
        verify(modelMapper, times(1)).map(testProduct, ProductDto.class);
    }

    @Test
    void updateProduct_WithValidData_ShouldReturnUpdatedProduct() {
        // Arrange
        ProductDto updateDto = new ProductDto();
        updateDto.setName("Updated Product");
        updateDto.setQuantity(20);
        updateDto.setPrice(BigDecimal.valueOf(150.0));
        updateDto.setSale(BigDecimal.valueOf(0.15));

        ProductEntity updatedProduct = new ProductEntity();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setQuantity(20);
        updatedProduct.setPrice(BigDecimal.valueOf(150.0));
        updatedProduct.setSale(BigDecimal.valueOf(0.15));

        ProductDto expectedDto = new ProductDto();
        expectedDto.setId(1L);
        expectedDto.setName("Updated Product");
        expectedDto.setQuantity(20);
        expectedDto.setPrice(BigDecimal.valueOf(150.0));
        expectedDto.setSale(BigDecimal.valueOf(0.15));

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(updatedProduct);
        when(modelMapper.map(updatedProduct, ProductDto.class)).thenReturn(expectedDto);

        // Act
        ProductDto result = productService.updateProduct(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(20, result.getQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(ProductEntity.class));
        verify(modelMapper, times(1)).map(updatedProduct, ProductDto.class);
    }

    @Test
    void updateProduct_WithNonExistingId_ShouldThrowException() {
        // Arrange
        ProductDto updateDto = new ProductDto();
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(999L, updateDto);
        });

        assertEquals("Product not found with id: 999", exception.getMessage());
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void deleteProduct_WithExistingId_ShouldSuccess() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(999L);
        });

        assertEquals("Product not found with id: 999", exception.getMessage());
        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}