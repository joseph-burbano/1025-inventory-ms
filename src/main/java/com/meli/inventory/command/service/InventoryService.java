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

// ## Prompt: Update class to include custom metrics
// Refactor this existing class to:
// - Inject a MeterRegistry field
// - Increment a counter named "inventory_updates_total" inside decreaseStock()
// - Do not duplicate the class or re-append code; modify the existing methods inline
// - Keep current logic untouched except for adding metrics

package com.meli.inventory.command.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meli.inventory.command.exception.NotEnoughStockException;
import com.meli.inventory.events.EventBus;
import com.meli.inventory.events.StockUpdatedEvent;
import com.meli.inventory.model.entities.InventoryItem;
import com.meli.inventory.model.repositories.InventoryRepository;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final EventBus eventBus;
    private final MeterRegistry meterRegistry;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, EventBus eventBus, MeterRegistry meterRegistry) {
        this.inventoryRepository = inventoryRepository;
        this.eventBus = eventBus;
        this.meterRegistry = meterRegistry;
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
        
        // Increment metrics counter
        meterRegistry.counter("inventory_updates_total", "type", "decrease", "sku", sku).increment();
        
        // Publish event for eventual consistency in read replicas
        eventBus.publish(new StockUpdatedEvent(sku, item.getQuantity()));
    }

    @Transactional
    public void adjustStock(String sku, int newQuantity) {
        InventoryItem item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new NotEnoughStockException("Item with SKU " + sku + " not found."));
        item.setQuantity(newQuantity);
        inventoryRepository.save(item);
        
        // Increment metrics counter
        meterRegistry.counter("inventory_updates_total", "type", "adjust", "sku", sku).increment();
        
        // Publish event for eventual consistency in read replicas
        eventBus.publish(new StockUpdatedEvent(sku, newQuantity));
    }
}