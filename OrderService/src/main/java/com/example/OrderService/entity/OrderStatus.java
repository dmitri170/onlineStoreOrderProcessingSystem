package com.example.OrderService.entity;

/**
 * Перечисление статусов заказа в системе.
 * Определяет все возможные состояния заказа в процессе его обработки.
 */
public enum OrderStatus {
    CREATED("Заказ создан"),

    PROCESSING("Проверка наличия товаров"),

    INSUFFICIENT_STOCK("Недостаточно товаров на складе"),

    CALCULATED("Заказ рассчитан"),

    SAVED("Заказ сохранен в базу данных"),

    SENT_TO_KAFKA("Заказ отправлен в Kafka"),

    COMPLETED("Заказ завершен"),

    FAILED("Ошибка при обработке заказа");

    private final String description;

    /**
     * Конструктор статуса заказа.
     *
     * @param description человеко-читаемое описание статуса
     */
    OrderStatus(String description) {
        this.description = description;
    }

    /**
     * Возвращает описание статуса заказа.
     *
     * @return человеко-читаемое описание статуса
     */
    public String getDescription() {
        return description;
    }
} 
