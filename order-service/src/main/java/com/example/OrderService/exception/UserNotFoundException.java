package com.example.OrderService.exception;

/**
 * Исключение, выбрасываемое когда пользователь не найден в системе.
 * Используется в процессах аутентификации и авторизации.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с сообщением о ненайденном пользователе.
     *
     * @param message сообщение об ошибке
     */
    public UserNotFoundException(String message) {
        super(message);
    }
} 
