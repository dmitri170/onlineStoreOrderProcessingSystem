package com.example.InventoryService.mapper;

import com.example.InventoryService.entity.ProductEntity;
import com.example.inventory.ProductResponse;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования между сущностями ProductEntity и gRPC сообщениями.
 * Обрабатывает преобразование данных и установку значений по умолчанию.
 */
@Component
public class GrpcMapper {

    /**
     * Преобразует сущность ProductEntity в gRPC ответ ProductResponse.
     *
     * @param product сущность товара
     * @return gRPC ответ с информацией о товаре
     */
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
    private double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }
}