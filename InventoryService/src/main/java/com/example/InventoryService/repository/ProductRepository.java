package com.example.InventoryService.repository;

import com.example.InventoryService.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью ProductEntity в базе данных.
 * Предоставляет методы для доступа к данным о товарах.
 */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * Находит товар по названию.
     *
     * @param name название товара
     * @return Optional с товаром, если найден
     */
    Optional<ProductEntity> findByName(String name);

    /**
     * Атомарно уменьшает количество товара на указанное значение.
     * Выполняется только если текущее количество достаточно.
     *
     * @param productId идентификатор товара
     * @param quantity количество для уменьшения
     * @return количество обновленных строк (1 - успех, 0 - неудача)
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.quantity = p.quantity - :quantity WHERE p.id = :productId AND p.quantity >= :quantity")
    int decreaseQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Атомарно увеличивает количество товара на указанное значение.
     *
     * @param productId идентификатор товара
     * @param quantity количество для увеличения
     * @return количество обновленных строк
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.quantity = p.quantity + :quantity WHERE p.id = :productId")
    int increaseQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}