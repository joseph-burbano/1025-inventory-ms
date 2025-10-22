// ## Prompt: InventoryQueryController
// Create a REST controller `InventoryQueryController` with endpoints:

// - `GET /api/v1/inventory/{sku}` → return InventoryView by SKU.
// - `GET /api/v1/inventory/` → return all inventory views.
// - `GET /api/v1/inventory/changes?since=timestamp` → return list of items updated since a given time (ISO format).

// Inject `InventoryQueryService`.
// Return `ResponseEntity` responses.
package com.meli.inventory.query.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.meli.inventory.query.model.InventoryView;
import com.meli.inventory.query.service.InventoryQueryService;
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryQueryService inventoryQueryService;

    @Autowired
    public InventoryController(InventoryQueryService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryView> getInventoryBySku(@PathVariable String sku) {
        InventoryView inventoryView = inventoryQueryService.getBySku(sku);
        if (inventoryView != null) {
            return ResponseEntity.ok(inventoryView);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<InventoryView>> getAllInventory() {
        List<InventoryView> allInventory = inventoryQueryService.getAll();
        return ResponseEntity.ok(allInventory);
    }

    @GetMapping("/changes")
    public ResponseEntity<List<InventoryView>> getChangesSince(@RequestParam("since") String since) {
        try {
            LocalDateTime timestamp = LocalDateTime.parse(since);
            List<InventoryView> changes = inventoryQueryService.getChangesSince(timestamp);
            return ResponseEntity.ok(changes);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
