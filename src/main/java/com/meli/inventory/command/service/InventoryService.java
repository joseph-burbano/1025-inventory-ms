// ## Prompt: InventoryService
// Create a Spring service class `InventoryService` that handles inventory operations.

// Requirements:
// - Annotate with `@Service`.
// - Inject a `JpaRepository<InventoryItem, Long>` named `inventoryRepository`.
// - Methods:
//   - `Optional<InventoryItem> findBySku(String sku)`
//   - `void decreaseStock(String sku, int amount)` → fetch item by SKU, call `decreaseStock(amount)`, save the entity.
//   - `void adjustStock(String sku, int newQuantity)` → directly set new quantity and save.
// - Throw `NotEnoughStockException` if not enough stock.
// - Use `@Transactional` for modifying methods.
package com.meli.inventory.command.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meli.inventory.command.exception.NotEnoughStockException;
import com.meli.inventory.model.InventoryItem;
import com.meli.inventory.model.InventoryRepository;
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Optional<InventoryItem> findBySku(String sku) {
        return inventoryRepository.findBySku(sku);
    }

    @Transactional
    public void decreaseStock(String sku, int amount) {
        InventoryItem item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new NotEnoughStockException("Item with SKU " + sku + " not found."));
        item.decreaseStock(amount);
        inventoryRepository.save(item);
    }

    @Transactional
    public void adjustStock(String sku, int newQuantity) {
        InventoryItem item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new NotEnoughStockException("Item with SKU " + sku + " not found."));
        item.setQuantity(newQuantity);
        inventoryRepository.save(item);
    }
}



