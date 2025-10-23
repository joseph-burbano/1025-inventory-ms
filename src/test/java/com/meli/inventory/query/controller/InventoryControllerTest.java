package com.meli.inventory.query.controller;

import com.meli.inventory.query.model.InventoryView;
import com.meli.inventory.query.service.InventoryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class InventoryControllerTest {

    @Mock
    private InventoryQueryService inventoryQueryService;

    private InventoryController inventoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryController = new InventoryController(inventoryQueryService);
    }

    @Test
    void getInventoryBySku_WhenSkuExists_ShouldReturnInventoryView() {
        String sku = "SKU123";
        InventoryView expectedView = new InventoryView();
        when(inventoryQueryService.getBySku(sku)).thenReturn(expectedView);

        ResponseEntity<InventoryView> response = inventoryController.getInventoryBySku(sku);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedView, response.getBody());
    }

    @Test
    void getInventoryBySku_WhenSkuDoesNotExist_ShouldReturnNotFound() {
        String sku = "NONEXISTENT";
        when(inventoryQueryService.getBySku(sku)).thenReturn(null);

        ResponseEntity<InventoryView> response = inventoryController.getInventoryBySku(sku);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllInventory_ShouldReturnAllInventoryViews() {
        List<InventoryView> expectedViews = Arrays.asList(new InventoryView(), new InventoryView());
        when(inventoryQueryService.getAll()).thenReturn(expectedViews);

        ResponseEntity<List<InventoryView>> response = inventoryController.getAllInventory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedViews, response.getBody());
    }

    @Test
    void getChangesSince_WithValidTimestamp_ShouldReturnChanges() {
        String timestamp = "2023-01-01T00:00:00";
        LocalDateTime dateTime = LocalDateTime.parse(timestamp);
        List<InventoryView> expectedChanges = Arrays.asList(new InventoryView());
        when(inventoryQueryService.getChangesSince(dateTime)).thenReturn(expectedChanges);

        ResponseEntity<List<InventoryView>> response = inventoryController.getChangesSince(timestamp);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedChanges, response.getBody());
    }

    @Test
    void getChangesSince_WithInvalidTimestamp_ShouldReturnBadRequest() {
        String invalidTimestamp = "invalid-timestamp";

        ResponseEntity<List<InventoryView>> response = inventoryController.getChangesSince(invalidTimestamp);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}