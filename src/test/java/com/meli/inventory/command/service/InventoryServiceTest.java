package com.meli.inventory.command.service;

import com.meli.inventory.command.exception.NotEnoughStockException;
import com.meli.inventory.events.EventBus;
import com.meli.inventory.events.StockUpdatedEvent;
import com.meli.inventory.model.entities.InventoryItem;
import com.meli.inventory.model.repositories.InventoryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EventBus eventBus;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(counter);

        inventoryService = new InventoryService(inventoryRepository, eventBus, meterRegistry);
    }

    @Test
    void decreaseStock_WhenItemExistsAndHasEnoughStock_ShouldDecreaseAndSave() {
        // Arrange
        String sku = "SKU123";
        int amount = 5;
        InventoryItem item = new InventoryItem(1L, sku, "inventory_item1", 10, 1L);
        when(inventoryRepository.findBySku(sku)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

        // Act
        inventoryService.decreaseStock(sku, amount);

        // Assert
        verify(inventoryRepository).findBySku(sku);
        verify(inventoryRepository).save(item);
        verify(eventBus).publish(any(StockUpdatedEvent.class));
        verify(counter).increment();
        assertEquals(5, item.getQuantity());
    }

    @Test
    void decreaseStock_WhenItemNotFound_ShouldThrowException() {
        // Arrange
        String sku = "SKU123";
        when(inventoryRepository.findBySku(sku)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotEnoughStockException.class, () ->
                inventoryService.decreaseStock(sku, 5)
        );
        verify(inventoryRepository).findBySku(sku);
        verify(inventoryRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
        verify(counter, never()).increment();
    }

    @Test
    void decreaseStock_WhenNotEnoughStock_ShouldThrowException() {
        // Arrange
        String sku = "SKU123";
        InventoryItem item = new InventoryItem(2L, sku, "inventory_item2", 3, 1L);
        when(inventoryRepository.findBySku(sku)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(NotEnoughStockException.class, () ->
                inventoryService.decreaseStock(sku, 5)
        );
        verify(inventoryRepository).findBySku(sku);
        verify(inventoryRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
        verify(counter, never()).increment();
    }

    @Test
    void adjustStock_WhenItemExists_ShouldAdjustAndSave() {
        // Arrange
        String sku = "SKU123";
        int newQuantity = 15;
        InventoryItem item = new InventoryItem(1L, sku, "inventory_item1", 10, 1L);
        when(inventoryRepository.findBySku(sku)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(item);

        // Act
        inventoryService.adjustStock(sku, newQuantity);

        // Assert
        verify(inventoryRepository).findBySku(sku);
        verify(inventoryRepository).save(item);
        verify(eventBus).publish(any(StockUpdatedEvent.class));
        verify(counter).increment();
        assertEquals(newQuantity, item.getQuantity());
    }
}
