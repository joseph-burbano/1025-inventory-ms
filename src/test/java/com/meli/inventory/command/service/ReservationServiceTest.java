package com.meli.inventory.command.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.meli.inventory.command.exception.ReservationNotFoundException;
import com.meli.inventory.events.EventBus;
import com.meli.inventory.events.StockUpdatedEvent;
import com.meli.inventory.model.entities.InventoryItem;
import com.meli.inventory.model.entities.Reservation;
import com.meli.inventory.model.entities.Reservation.ReservationStatus;
import com.meli.inventory.model.repositories.ReservationRepository;

public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private InventoryService inventoryService;
    
    @Mock
    private EventBus eventBus;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationService = new ReservationService(reservationRepository, inventoryService, eventBus);
    }

    @Test
    void createReservation_Success() {
        // Arrange
        String sku = "SKU123";
        int quantity = 5;
        String storeId = "STORE1";
        
        InventoryItem inventory = new InventoryItem();
        inventory.setQuantity(10);
        
        when(inventoryService.findBySku(sku)).thenReturn(Optional.of(inventory));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Reservation result = reservationService.createReservation(sku, quantity, storeId);

        // Assert
        assertNotNull(result);
        assertEquals(ReservationStatus.PENDING, result.getStatus());
        assertEquals(sku, result.getSku());
        assertEquals(quantity, result.getQuantity());
        assertEquals(storeId, result.getStoreId());
        
        verify(inventoryService).decreaseStock(sku, quantity);
        verify(eventBus).publish(any(StockUpdatedEvent.class));
    }

    @Test
    void confirmReservation_Success() {
        // Arrange
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSku("SKU123");
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(inventoryService.findBySku(anyString())).thenReturn(Optional.of(new InventoryItem()));

        // Act
        Reservation result = reservationService.confirmReservation(reservationId);

        // Assert
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        verify(eventBus).publish(any(StockUpdatedEvent.class));
    }

    @Test
    void confirmReservation_NotFound() {
        // Arrange
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReservationNotFoundException.class, 
            () -> reservationService.confirmReservation(1L));
    }

    @Test
    void cancelReservation_Success() {
        // Arrange
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSku("SKU123");
        reservation.setQuantity(5);

        InventoryItem inventory = new InventoryItem();
        inventory.setQuantity(5);
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(inventoryService.findBySku(anyString())).thenReturn(Optional.of(inventory));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Reservation result = reservationService.cancelReservation(reservationId);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        verify(inventoryService).adjustStock(eq(reservation.getSku()), eq(10));
        verify(eventBus).publish(any(StockUpdatedEvent.class));
    }

    @Test
    void cancelReservation_AlreadyCancelled() {
        // Arrange
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CANCELLED);
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act
        Reservation result = reservationService.cancelReservation(reservationId);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        verify(inventoryService, never()).adjustStock(anyString(), anyInt());
    }

    // Genera el test de cancelReservation_AlreadyConfirmed
    @Test
    void cancelReservation_AlreadyConfirmed() {
        // Arrange
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act
        Reservation result = reservationService.cancelReservation(reservationId);

        // Assert
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        verify(inventoryService, never()).adjustStock(anyString(), anyInt());
    }
}