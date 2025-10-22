package com.meli.inventory.query.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import com.meli.inventory.model.entities.InventoryItem;
import com.meli.inventory.model.repositories.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.meli.inventory.events.StockUpdatedEvent;
import com.meli.inventory.query.model.InventoryView;

@ExtendWith(MockitoExtension.class)
class InventoryQueryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    private InventoryQueryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryQueryService(inventoryRepository);
    }

    @Test
    void loadInitialData_ShouldPopulateMap() {
        // Arrange
        InventoryItem item = new InventoryItem(1L, "SKU1", "Test Item", 10, 1L);
        when(inventoryRepository.findAll()).thenReturn(List.of(item));

        // Act
        service.loadInitialData();

        // Assert
        InventoryView view = service.getBySku("SKU1");
        assertNotNull(view);
        assertEquals("SKU1", view.getSku());
        assertEquals("Test Item", view.getName());
        assertEquals(10, view.getQuantity());
    }

    @Test
    void handleStockUpdated_ShouldUpdateExistingItem() {
        // Arrange
        service.handleStockUpdated(new StockUpdatedEvent("SKU1", 10));
        StockUpdatedEvent updateEvent = new StockUpdatedEvent("SKU1", 20);

        // Act
        service.handleStockUpdated(updateEvent);

        // Assert
        InventoryView view = service.getBySku("SKU1");
        assertNotNull(view);
        assertEquals(20, view.getQuantity());
    }

    @Test
    void getChangesSince_ShouldReturnOnlyRecentChanges() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        service.handleStockUpdated(new StockUpdatedEvent("SKU1", 10));

        // Act
        List<InventoryView> changes = service.getChangesSince(now.minusMinutes(1));

        // Assert
        assertFalse(changes.isEmpty());
        assertEquals(1, changes.size());
        assertEquals("SKU1", changes.get(0).getSku());
    }

    @Test
    void getAll_ShouldReturnAllItems() {
        // Arrange
        service.handleStockUpdated(new StockUpdatedEvent("SKU1", 10));
        service.handleStockUpdated(new StockUpdatedEvent("SKU2", 20));

        // Act
        List<InventoryView> allItems = service.getAll();

        // Assert
        assertEquals(2, allItems.size());
        assertNotNull(allItems.stream().filter(v -> v.getSku().equals("SKU1")).findFirst().orElse(null));
        assertNotNull(allItems.stream().filter(v -> v.getSku().equals("SKU2")).findFirst().orElse(null));
    }
}
