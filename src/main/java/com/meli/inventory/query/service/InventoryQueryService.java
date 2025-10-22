// ## Prompt: InventoryQueryService
// Create a Spring service `InventoryQueryService` that simulates a read model updated by events.

// Requirements:
// - Annotate with `@Service`.
// - Maintain a `ConcurrentHashMap<String, InventoryView>` to store the current stock per SKU.
// - Provide methods:
//   - `void handleStockUpdated(StockUpdatedEvent event)` → updates the map.
//   - `InventoryView getBySku(String sku)`
//   - `List<InventoryView> getAll()`
//   - `List<InventoryView> getChangesSince(LocalDateTime timestamp)`
// - Use SLF4J for logging updates.
package com.meli.inventory.query.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.meli.inventory.events.StockUpdatedEvent;
import com.meli.inventory.model.entities.InventoryItem;
import com.meli.inventory.model.repositories.InventoryRepository;
import com.meli.inventory.query.model.InventoryView;

@Service
public class InventoryQueryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryQueryService.class);
    private final ConcurrentHashMap<String, InventoryView> inventoryMap = new ConcurrentHashMap<>();

    private final InventoryRepository inventoryRepository;

    public InventoryQueryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
        System.out.println("🧩 InventoryRepository injected: " + (inventoryRepository != null));
    }

    /** 🔄 Carga inicial desde la base de datos **/
    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialData() {
        System.out.println("🚀 Loading initial inventory from DB...");
        List<InventoryItem> items = inventoryRepository.findAll();
        for (InventoryItem item : items) {
            inventoryMap.put(item.getSku(),
                    new InventoryView(item.getSku(), item.getName(), item.getQuantity(), LocalDateTime.now()));
        }
        logger.info("✅ InventoryQueryService initialized with {} items from DB", items.size());
    }

    /** 🔔 Se invoca cuando llega un StockUpdatedEvent **/
    public void handleStockUpdated(StockUpdatedEvent event) {
        inventoryMap.compute(event.getSku(), (sku, existingView) -> {
            if (existingView == null) {
                existingView = new InventoryView(sku, "Unknown", event.getNewQuantity(), LocalDateTime.now());
            } else {
                existingView.setQuantity(event.getNewQuantity());
                existingView.setLastUpdated(LocalDateTime.now());
            }
            logger.info("Stock updated for SKU {}: new quantity {}", sku, event.getNewQuantity());
            return existingView;
        });
    }

    public InventoryView getBySku(String sku) {
        return inventoryMap.get(sku);
    }

    public List<InventoryView> getAll() {
        return List.copyOf(inventoryMap.values());
    }

    public List<InventoryView> getChangesSince(LocalDateTime timestamp) {
        return inventoryMap.values().stream()
                .filter(view -> view.getLastUpdated().isAfter(timestamp))
                .collect(Collectors.toList());
    }
}

