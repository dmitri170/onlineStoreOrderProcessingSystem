package com.example.NotificationService.repository;

import com.example.NotificationService.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Order в базе данных.
 * Предоставляет методы для доступа к данным о заказах.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Находит заказ по уникальному orderId (UUID из Order Service).
     *
     * @param orderId уникальный идентификатор заказа
     * @return Optional с заказом, если найден
     */
    Optional<Order> findByOrderId(String orderId);

    /**
     * Находит все заказы конкретного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список заказов пользователя
     */
    List<Order> findByUserId(Long userId);

    /**
     * Находит все заказы с информацией о товарах для аналитики.
     *
     * @return список всех заказов с товарами
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items")
    List<Order> findAllWithItems();

    /**
     * Находит все заказы отсортированные по дате (новые первыми).
     *
     * @return список заказов отсортированных по дате
     */
    List<Order> findAllByOrderByOrderDateDesc();
}