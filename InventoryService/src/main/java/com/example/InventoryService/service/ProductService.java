package com.example.InventoryService.service;

import com.example.InventoryService.dto.ProductAvailability;
import com.example.InventoryService.dto.ProductDto;
import com.example.InventoryService.entity.ProductEntity;
import com.example.InventoryService.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления товарами в инвентаризации.
 * Работает с DTO для внешнего API, внутренне использует Entity для БД.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    // === Публичные методы для REST API (работают с DTO) ===

    /**
     * Возвращает список всех товаров.
     *
     * @return список DTO товаров
     */
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Находит товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return DTO товара
     * @throws RuntimeException если товар не найден
     */
    public ProductDto getProductById(Long id) {
        ProductEntity productEntity = findProductEntityById(id);
        return convertToDto(productEntity);
    }

    /**
     * Создает новый товар.
     *
     * @param productDto DTO с данными товара
     * @return созданный товар в виде DTO
     */
    public ProductDto createProduct(ProductDto productDto) {
        ProductEntity productEntity = convertToEntity(productDto);
        ProductEntity savedProductEntity = productRepository.save(productEntity);
        return convertToDto(savedProductEntity);
    }

    /**
     * Обновляет существующий товар.
     *
     * @param id идентификатор товара
     * @param productDto новые данные товара
     * @return обновленный товар в виде DTO
     * @throws RuntimeException если товар не найден
     */
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        ProductEntity existingProductEntity = findProductEntityById(id);

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

    /**
     * Удаляет товар по идентификатору.
     *
     * @param id идентификатор товара
     * @throws RuntimeException если товар не найден
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Товар не найден с id: " + id);
        }
        productRepository.deleteById(id);
    }

    // === Внутренние методы для gRPC сервиса ===

    /**
     * Находит сущность товара по идентификатору (для gRPC сервиса).
     *
     * @param productId идентификатор товара
     * @return сущность товара
     * @throws RuntimeException если товар не найден
     */
    ProductEntity findProductEntityById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден с id: " + productId));
    }

    /**
     * Проверяет доступность нескольких товаров (для gRPC сервиса).
     *
     * @param productIds список идентификаторов товаров
     * @return список информации о доступности товаров
     */
    List<ProductAvailability> checkProductsAvailability(List<Long> productIds) {
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

    // === Вспомогательные методы для конвертации ===

    /**
     * Преобразует сущность товара в DTO.
     *
     * @param productEntity сущность товара
     * @return DTO товара
     */
    private ProductDto convertToDto(ProductEntity productEntity) {
        return modelMapper.map(productEntity, ProductDto.class);
    }

    /**
     * Преобразует DTO товара в сущность.
     *
     * @param productDto DTO товара
     * @return сущность товара
     */
    private ProductEntity convertToEntity(ProductDto productDto) {
        return modelMapper.map(productDto, ProductEntity.class);
    }
}