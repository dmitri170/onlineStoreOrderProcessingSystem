package com.example.OrderService.exception;

/**
 * Исключение, выбрасываемое когда товара недостаточно на складе для выполнения заказа.
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * Создает новое исключение с сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}