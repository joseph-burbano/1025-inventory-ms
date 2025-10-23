// ## Prompt: Repositories
// Create interface extending JpaRepository:
// - `InventoryRepository extends JpaRepository<InventoryItem, Long>` with a method `Optional<InventoryItem> findBySku(String sku)`
package com.meli.inventory.model.repositories;

import com.meli.inventory.model.entities.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findBySku(String sku);
}