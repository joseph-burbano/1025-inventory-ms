// ## Prompt: InventoryView
// Create a simple model class `InventoryView` representing the read model of stock.

// Fields:
// - sku (String)
// - name (String)
// - quantity (Integer)
// - lastUpdated (LocalDateTime)
// Add Lombok annotations `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`.

package com.meli.inventory.query.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryView {
    private String sku;
    private String name;
    private Integer quantity;
    private LocalDateTime lastUpdated;
}