package com.example.InventoryService.repository;

import com.example.InventoryService.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository interface for managing ProductEntity data access operations.
 * Extends JpaRepository to provide CRUD operations for ProductEntity.
 *
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see com.example.InventoryService.model.ProductEntity
 */
public interface ProductRepository extends JpaRepository<ProductEntity,Long> {
}
