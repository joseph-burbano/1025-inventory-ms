package com.meli.inventory.model;

import com.meli.inventory.command.exception.NotEnoughStockException;
import com.meli.inventory.model.entities.InventoryItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryItemTest {

    @Test
    void decreaseStock_ShouldReduceQuantity_WhenEnoughStock() {
        // Arrange
        InventoryItem item = new InventoryItem();
        item.setSku("SKU1");
        item.setQuantity(10);

        // Act
        item.decreaseStock(3);

        // Assert
        assertEquals(7, item.getQuantity());
    }

    @Test
    void decreaseStock_ShouldThrowException_WhenNotEnoughStock() {
        // Arrange
        InventoryItem item = new InventoryItem();
        item.setSku("SKU1"); 
        item.setQuantity(5);

        // Act & Assert
        NotEnoughStockException exception = assertThrows(NotEnoughStockException.class, 
            () -> item.decreaseStock(10));
        assertEquals("Not enough stock available for SKU SKU1", exception.getMessage());
    }

    @Test
    void decreaseStock_ShouldReduceToZero_WhenExactAmount() {
        // Arrange
        InventoryItem item = new InventoryItem();
        item.setSku("SKU1");
        item.setQuantity(5);

        // Act
        item.decreaseStock(5);

        // Assert
        assertEquals(0, item.getQuantity());
    }
}