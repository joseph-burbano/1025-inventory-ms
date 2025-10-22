// Prompt: InventoryItem Entity
// Create a JPA entity class called InventoryItem inside com.meli.inventory.model.
// Requirements:
// - Fields: id (Long), sku (String), name (String), quantity (Integer), version (Long)
// - Annotate with @Entity, @Table(name="inventory_items")
// - Use @Version for optimistic locking on version
// - Add Lombok annotations: @Data, @NoArgsConstructor, @AllArgsConstructor
// - Include @GeneratedValue for id
// - Add a helper method decreaseStock(int amount) that throws if amount > quantity.
package com.meli.inventory.model.entities;

import com.meli.inventory.command.exception.NotEnoughStockException;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String sku;

    @NonNull
    private String name;

    @NonNull
    private Integer quantity;

    @Version
    private Long version;

    public void decreaseStock(int amount) {
        if (amount > this.quantity) {
            throw new NotEnoughStockException("Not enough stock available for SKU " + this.sku);
        }
        this.quantity -= amount;
    }

}
