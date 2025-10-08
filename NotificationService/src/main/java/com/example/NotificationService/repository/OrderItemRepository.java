package com.example.NotificationService.repository;

import com.example.NotificationService.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Репозиторий для работы с сущностью OrderItem в базе данных.
 * Предоставляет методы для доступа к данным о товарах в заказах.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Находит все товары конкретного заказа.
     *
     * @param orderId идентификатор заказа
     * @return список товаров в заказе
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Находит все товары заказа по orderId (UUID).
     *
     * @param orderId уникальный идентификатор заказа
     * @return список товаров в заказе
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    List<OrderItem> findByOrderOrderId(@Param("orderId") String orderId);

    /**
     * Находит все товары по идентификатору продукта.
     * Полезно для аналитики популярности товаров.
     *
     * @param productId идентификатор товара
     * @return список позиций заказов с этим товаром
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * Рассчитывает общее количество проданных единиц товара.
     *
     * @param productId идентификатор товара
     * @return общее количество проданных единиц
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Integer getTotalSoldQuantityByProductId(@Param("productId") Long productId);
}