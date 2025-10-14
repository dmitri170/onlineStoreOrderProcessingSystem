package com.example.OrderService.exception;

/**
 * Исключение, связанное с аутентификацией JWT токенов.
 * Выбрасывается при невалидных, просроченных или поврежденных токенах.
 */
public class JwtAuthenticationException extends RuntimeException {

    /**
     * Создает новое исключение аутентификации с сообщением.
     *
     * @param message сообщение об ошибке аутентификации
     */
    public JwtAuthenticationException(String message) {
        super(message);
    }
} 
