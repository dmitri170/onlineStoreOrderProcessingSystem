package com.example.InventoryService.mapper;

import com.example.InventoryService.entity.ProductEntity;
import com.example.inventory.ProductResponseItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Маппер для преобразования между сущностями ProductEntity и gRPC сообщениями.
 * Обрабатывает преобразование данных и установку значений по умолчанию.
 */
@Component
public class GrpcMapper {

    /**
     * Преобразует сущность ProductEntity в gRPC ответ ProductResponseItem.
     *
     * @param product сущность товара
     * @return gRPC ответ с информацией о товаре
     */
    public ProductResponseItem toProductResponseItem(ProductEntity product) {
        return ProductResponseItem.newBuilder()
                .setProductId(product.getId())
                .setName(getSafeString(product.getName()))
                .setAvailableQuantity(getSafeInteger(product.getQuantity()))
                .setPrice(getSafeDoubleFromBigDecimal(product.getPrice()))
                .setSale(getSafeDoubleFromBigDecimal(product.getSale()))
                .setIsAvailable(isProductAvailable(product))
                .build();
    }

    /**
     * Преобразует сущность ProductEntity в gRPC ответ ProductResponseItem с указанием запрошенного количества.
     *
     * @param product сущность товара
     * @param requestedQuantity запрошенное количество
     * @return gRPC ответ с информацией о товаре
     */
    public ProductResponseItem toProductResponseItem(ProductEntity product, int requestedQuantity) {
        return ProductResponseItem.newBuilder()
                .setProductId(product.getId())
                .setName(getSafeString(product.getName()))
                .setAvailableQuantity(getSafeInteger(product.getQuantity()))
                .setRequestedQuantity(requestedQuantity)
                .setPrice(getSafeDoubleFromBigDecimal(product.getPrice()))
                .setSale(getSafeDoubleFromBigDecimal(product.getSale()))
                .setIsAvailable(isProductAvailable(product, requestedQuantity))
                .build();
    }

    /**
     * Проверяет доступность товара для заказа.
     *
     * @param product товар для проверки
     * @return true если товар доступен, false в противном случае
     */
    private boolean isProductAvailable(ProductEntity product) {
        return product.getQuantity() != null && product.getQuantity() > 0;
    }

    /**
     * Проверяет доступность товара в нужном количестве.
     *
     * @param product товар для проверки
     * @param requestedQuantity запрошенное количество
     * @return true если товар доступен в нужном количестве, false в противном случае
     */
    private boolean isProductAvailable(ProductEntity product, int requestedQuantity) {
        return product.getQuantity() != null && product.getQuantity() >= requestedQuantity;
    }

    /**
     * Возвращает безопасное строковое значение.
     *
     * @param value исходное строковое значение
     * @return исходное значение или пустую строку если null
     */
    private String getSafeString(String value) {
        return value != null ? value : "";
    }

    /**
     * Возвращает безопасное целочисленное значение.
     *
     * @param value исходное целочисленное значение
     * @return исходное значение или 0 если null
     */
    private int getSafeInteger(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * Возвращает безопасное значение с плавающей точкой.
     *
     * @param value исходное значение с плавающей точкой
     * @return исходное значение или 0.0 если null
     */
    private double getSafeDoubleFromBigDecimal(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}