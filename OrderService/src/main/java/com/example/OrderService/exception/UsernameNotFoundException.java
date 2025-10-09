package com.example.OrderService.exception;

/**
 * Исключение, выбрасываемое когда пользователь не найден в системе.
 * Используется в процессах аутентификации и авторизации.
 */
public class UsernameNotFoundException extends Exception {

    /**
     * Создает новое исключение с сообщением о ненайденном пользователе.
     *
     * @param message сообщение об ошибке
     */
    public UsernameNotFoundException(String message) {
        super(message);
    }
} 
