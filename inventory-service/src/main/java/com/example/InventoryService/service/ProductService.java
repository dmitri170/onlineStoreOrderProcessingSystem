package com.example.InventoryService.service;

import com.example.InventoryService.dto.ProductAvailability;
import com.example.InventoryService.dto.ProductDto;
import com.example.InventoryService.entity.ProductEntity;
import com.example.InventoryService.repository.ProductRepository;
import com.example.inventory.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления товарами в инвентаризации.
 * Работает с DTO для внешнего API, внутренне использует Entity для БД.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    /**
     * Возвращает список всех товаров.
     *
     * @return список DTO товаров
     */
    //@Transactional(readOnly = true)
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
    //@Transactional(readOnly = true)
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
     * Проверяет доступность нескольких товаров в рамках одного запроса (для gRPC сервиса).
     *
     * @param request bulk запрос с товарами
     * @param rqUid   идентификатор запроса для логирования
     * @return bulk ответ с доступными и недоступными товарами
     */
    public BulkProductResponse checkBulkAvailability(BulkProductRequest request, String rqUid) {
        log.info("[Inventory: RqUid {}] Начало bulk проверки доступности {} товаров",
                rqUid, request.getItemsCount());

        BulkProductResponse.Builder responseBuilder = BulkProductResponse.newBuilder()
                .setRqUid(rqUid);  // Устанавливаем rqUid в ответ

        for (ProductRequestItem requestItem : request.getItemsList()) {
            ProductResponseItem responseItem = checkProductAvailability(requestItem, rqUid);

            if (responseItem.getIsAvailable()) {
                responseBuilder.addAvailableItems(responseItem);
            } else {
                responseBuilder.addUnavailableItems(responseItem);
            }
        }

        log.info("[Inventory: RqUid {}] Bulk проверка завершена: доступно {}, недоступно {}",
                rqUid, responseBuilder.getAvailableItemsCount(), responseBuilder.getUnavailableItemsCount());

        return responseBuilder.build();
    }

    /**
     * Проверяет доступность одного товара
     */
    private ProductResponseItem checkProductAvailability(ProductRequestItem requestItem, String rqUid) {
        Long productId = requestItem.getProductId();
        Integer requestedQuantity = requestItem.getRequestedQuantity();

        log.debug("[Inventory: RqUid {}] Проверка товара ID: {}, запрошено: {}",
                rqUid, productId, requestedQuantity);

        Optional<ProductEntity> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            log.warn("[Inventory: RqUid {}] Товар не найден: ID {}", rqUid, productId);
            return createUnavailableResponse(requestItem, "Товар не найден");
        }

        ProductEntity product = productOpt.get();

        if (product.getQuantity() < requestedQuantity) {
            log.warn("[Inventory: RqUid {}] Недостаточно товара: ID {} (доступно: {}, запрошено: {})",
                    rqUid, productId, product.getQuantity(), requestedQuantity);
            return createUnavailableResponse(requestItem, product, "Недостаточно товара");
        }

        log.debug("[Inventory: RqUid {}] Товар доступен: ID {}, количество: {}",
                rqUid, productId, product.getQuantity());

        return createAvailableResponse(requestItem, product);
    }

    /**
     * Создает ответ для доступного товара
     */
    private ProductResponseItem createAvailableResponse(ProductRequestItem requestItem, ProductEntity product) {
        return ProductResponseItem.newBuilder()
                .setProductId(product.getId())
                .setName(product.getName())
                .setAvailableQuantity(product.getQuantity())
                .setRequestedQuantity(requestItem.getRequestedQuantity())
                .setPrice(getSafeDoubleFromBigDecimal(product.getPrice()))
                .setSale(getSafeDoubleFromBigDecimal(product.getSale()))
                .setIsAvailable(true)
                .build();
    }

    /**
     * Создает ответ для недоступного товара
     */
    private ProductResponseItem createUnavailableResponse(ProductRequestItem requestItem, ProductEntity product, String reason) {
        return ProductResponseItem.newBuilder()
                .setProductId(product != null ? product.getId() : requestItem.getProductId())
                .setName(product != null ? product.getName() : "Неизвестный товар")
                .setAvailableQuantity(product != null ? product.getQuantity() : 0)
                .setRequestedQuantity(requestItem.getRequestedQuantity())
                .setPrice(product != null ? getSafeDoubleFromBigDecimal(product.getPrice()) : 0.0)
                .setSale(product != null ? getSafeDoubleFromBigDecimal(product.getSale()) : 0.0)
                .setIsAvailable(false)
                .build();
    }

    private ProductResponseItem createUnavailableResponse(ProductRequestItem requestItem, String reason) {
        return createUnavailableResponse(requestItem, null, reason);
    }

    /**
     * Проверяет доступность нескольких товаров (для REST API).
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
    /**
     * Вспомогательный метод для безопасного преобразования BigDecimal в double
     */
    private double getSafeDoubleFromBigDecimal(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
    /**
     * Резервирует товары для заказа
     */
    @Transactional
    public ReserveProductsResponse reserveProducts(ReserveProductsRequest request) {
        String orderId = request.getOrderId();
        log.info("[Inventory] Резервирование товаров для заказа: {}", orderId);

        ReserveProductsResponse.Builder responseBuilder = ReserveProductsResponse.newBuilder()
                .setOrderId(orderId);

        List<ProductResponseItem> reservedItems = new ArrayList<>();
        List<ProductResponseItem> failedItems = new ArrayList<>();

        for (ProductRequestItem requestItem : request.getItemsList()) {
            try {
                ProductResponseItem reservedItem = reserveProduct(requestItem, orderId);
                if (reservedItem.getIsAvailable()) {
                    reservedItems.add(reservedItem);
                } else {
                    failedItems.add(reservedItem);
                }
            } catch (Exception e) {
                log.error("[Inventory] Ошибка резервирования товара {} для заказа {}: {}",
                        requestItem.getProductId(), orderId, e.getMessage());
                failedItems.add(createFailedReservationResponse(requestItem, e.getMessage()));
            }
        }

        // Добавляем результаты в ответ
        reservedItems.forEach(responseBuilder::addReservedItems);
        failedItems.forEach(responseBuilder::addFailedItems);

        boolean success = failedItems.isEmpty();
        responseBuilder.setSuccess(success);
        responseBuilder.setMessage(success ? "Товары успешно зарезервированы" : "Некоторые товары не удалось зарезервировать");

        log.info("[Inventory] Резервирование для заказа {} завершено: успешно {}, неудачно {}",
                orderId, reservedItems.size(), failedItems.size());

        return responseBuilder.build();
    }

    /**
     * Резервирует один товар
     */
    private ProductResponseItem reserveProduct(ProductRequestItem requestItem, String orderId) {
        Long productId = requestItem.getProductId();
        Integer requestedQuantity = requestItem.getRequestedQuantity();

        log.debug("[Inventory] Резервирование товара ID: {} для заказа {}, количество: {}",
                productId, orderId, requestedQuantity);

        Optional<ProductEntity> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            log.warn("[Inventory] Товар не найден при резервировании: ID {} для заказа {}", productId, orderId);
            return createFailedReservationResponse(requestItem, "Товар не найден");
        }

        ProductEntity product = productOpt.get();

        // Проверяем доступность еще раз (на случай параллельных заказов)
        if (product.getQuantity() < requestedQuantity) {
            log.warn("[Inventory] Недостаточно товара при резервировании: ID {} (доступно: {}, запрошено: {}) для заказа {}",
                    productId, product.getQuantity(), requestedQuantity, orderId);
            return createFailedReservationResponse(requestItem, product, "Недостаточно товара");
        }

        // Уменьшаем количество товара
        int newQuantity = product.getQuantity() - requestedQuantity;
        product.setQuantity(newQuantity);
        productRepository.save(product);

        log.info("[Inventory] Товар ID {} зарезервирован для заказа {}. Новое количество: {}",
                productId, orderId, newQuantity);

        return createReservedResponse(requestItem, product, newQuantity);
    }

    /**
     * Создает ответ для успешно зарезервированного товара
     */
    private ProductResponseItem createReservedResponse(ProductRequestItem requestItem, ProductEntity product, int newQuantity) {
        return ProductResponseItem.newBuilder()
                .setProductId(product.getId())
                .setName(product.getName())
                .setAvailableQuantity(newQuantity)
                .setRequestedQuantity(requestItem.getRequestedQuantity())
                .setPrice(getSafeDoubleFromBigDecimal(product.getPrice()))
                .setSale(getSafeDoubleFromBigDecimal(product.getSale()))
                .setIsAvailable(true)
                .build();
    }

    /**
     * Создает ответ для неудачного резервирования
     */
    private ProductResponseItem createFailedReservationResponse(ProductRequestItem requestItem, ProductEntity product, String reason) {
        return ProductResponseItem.newBuilder()
                .setProductId(product != null ? product.getId() : requestItem.getProductId())
                .setName(product != null ? product.getName() : "Неизвестный товар")
                .setAvailableQuantity(product != null ? product.getQuantity() : 0)
                .setRequestedQuantity(requestItem.getRequestedQuantity())
                .setPrice(product != null ? getSafeDoubleFromBigDecimal(product.getPrice()) : 0.0)
                .setSale(product != null ? getSafeDoubleFromBigDecimal(product.getSale()) : 0.0)
                .setIsAvailable(false)
                .build();
    }

    private ProductResponseItem createFailedReservationResponse(ProductRequestItem requestItem, String reason) {
        return createFailedReservationResponse(requestItem, null, reason);
    }
}