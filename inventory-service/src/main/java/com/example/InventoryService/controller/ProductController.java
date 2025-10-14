package com.example.InventoryService.controller;

import com.example.InventoryService.dto.ProductDto;
import com.example.InventoryService.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления товарами через HTTP API.
 * Работает исключительно с DTO.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * Конструктор контроллера товаров.
     *
     * @param productService сервис для работы с товарами
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Возвращает список всех товаров.
     *
     * @return список DTO товаров
     */
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Находит товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return DTO товара или 404 если не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        try {
            ProductDto product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Создает новый товар.
     *
     * @param productDto DTO с данными товара
     * @return созданный товар в виде DTO
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return ResponseEntity.ok(createdProduct);
    }

    /**
     * Обновляет существующий товар.
     *
     * @param id идентификатор товара
     * @param productDto новые данные товара
     * @return обновленный товар в виде DTO или 404 если не найден
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto) {
        try {
            ProductDto updatedProduct = productService.updateProduct(id, productDto);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удаляет товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return 200 OK при успешном удалении или 404 если не найден
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 
