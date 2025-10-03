package com.example.InventoryService.service;

import com.example.InventoryService.dto.ProductAvailability;
import com.example.InventoryService.dto.ProductDto;
import com.example.InventoryService.model.ProductEntity;
import com.example.InventoryService.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDto(productEntity);
    }

    public Optional<ProductEntity> getProductEntityById(Long productId) {
        return productRepository.findById(productId);
    }

    public ProductDto createProduct(ProductDto productDto) {
        ProductEntity productEntity = convertToEntity(productDto);
        ProductEntity savedProductEntity = productRepository.save(productEntity);
        return convertToDto(savedProductEntity);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        ProductEntity existingProductEntity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Обновляем только не-null поля
        if (productDto.getName() != null) {
            existingProductEntity.setName(productDto.getName());
        }
        if (productDto.getQuantity() != null) {
            existingProductEntity.setQuantity(productDto.getQuantity());
        }
        if (productDto.getPrice() != null) {
            existingProductEntity.setPrice(productDto.getPrice());
        }
        if (productDto.getSale() != null) {
            existingProductEntity.setSale(productDto.getSale());
        }

        ProductEntity updatedProductEntity = productRepository.save(existingProductEntity);
        return convertToDto(updatedProductEntity);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }


    public List<ProductAvailability> checkProductsAvailability(List<Long> productIds) {
        return productRepository.findAllById(productIds).stream()
                .map(productEntity -> new ProductAvailability(
                        productEntity.getId(),
                        productEntity.getName(),
                        productEntity.getPrice(),
                        productEntity.getSale(),
                        productEntity.getQuantity(),
                        productEntity.getQuantity() != null && productEntity.getQuantity() > 0
                ))
                .collect(Collectors.toList());
    }

    // Вспомогательные методы для конвертации
    private ProductDto convertToDto(ProductEntity productEntity) {
        return modelMapper.map(productEntity, ProductDto.class);
    }

    private ProductEntity convertToEntity(ProductDto productDto) {
        return modelMapper.map(productDto, ProductEntity.class);
    }
}