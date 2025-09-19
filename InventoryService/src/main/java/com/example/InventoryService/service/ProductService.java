package com.example.InventoryService.service;

import com.example.InventoryService.dto.ProductAvailability;
import com.example.InventoryService.dto.ProductDto;
import com.example.InventoryService.model.Product;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDto(product);
    }

    public Optional<Product> getProductInfoForOrder(Long productId) {
        return productRepository.findById(productId);
    }

    public ProductDto createProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existingProduct.setName(productDto.getName());
        existingProduct.setQuantity(productDto.getQuantity());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setSale(productDto.getSale());

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public List<ProductAvailability> checkProductsAvailability(List<Long> productIds) {
        return productRepository.findAllById(productIds).stream()
                .map(product -> new ProductAvailability(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getSale(),
                        product.getQuantity(),
                        product.getQuantity() > 0
                ))
                .collect(Collectors.toList());
    }

    private ProductDto convertToDto(Product product) {
        return modelMapper.map(product, ProductDto.class);
    }

    private Product convertToEntity(ProductDto productDto) {
        return modelMapper.map(productDto, Product.class);
    }
}