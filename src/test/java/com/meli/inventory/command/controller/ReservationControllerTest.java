package com.meli.inventory.command.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.meli.inventory.command.exception.ReservationNotFoundException;
import com.meli.inventory.command.service.ReservationService;
import com.meli.inventory.model.entities.Reservation;
import com.meli.inventory.command.controller.ReservationController.ReservationRequest;

class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReservation_ShouldReturnCreatedReservation() {
        // Arrange
        ReservationRequest request = new ReservationRequest();
        request.setSku("SKU123");
        request.setQuantity(5);
        request.setStoreId("STORE1");
        
        Reservation mockReservation = new Reservation();
        when(reservationService.createReservation("SKU123", 5, "STORE1")).thenReturn(mockReservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.createReservation(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockReservation, response.getBody());
        verify(reservationService).createReservation("SKU123", 5, "STORE1");
    }

    @Test
    void confirmReservation_ShouldReturnConfirmedReservation() {
        // Arrange
        Long reservationId = 1L;
        Reservation mockReservation = new Reservation();
        when(reservationService.confirmReservation(reservationId)).thenReturn(mockReservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.confirmReservation(reservationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockReservation, response.getBody());
        verify(reservationService).confirmReservation(reservationId);
    }

    @Test
    void cancelReservation_ShouldReturnCancelledReservation() {
        // Arrange
        Long reservationId = 1L;
        Reservation mockReservation = new Reservation();
        when(reservationService.cancelReservation(reservationId)).thenReturn(mockReservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.cancelReservation(reservationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockReservation, response.getBody());
        verify(reservationService).cancelReservation(reservationId);
    }

    @Test
    void handleReservationNotFound_ShouldReturnNotFoundStatus() {
        // Arrange
        String errorMessage = "Reservation not found";
        ReservationNotFoundException ex = new ReservationNotFoundException(errorMessage);

        // Act
        ResponseEntity<String> response = reservationController.handleReservationNotFound(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}