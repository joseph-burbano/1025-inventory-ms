// ## Prompt: ReservationController
// Create a REST controller `ReservationController` to handle reservation endpoints.

// Requirements:
// - Annotate with `@RestController` and `@RequestMapping("/api/v1/reservations")`
// - Inject `ReservationService`.
// - Endpoints:
//   - `POST /` → create reservation (accepts JSON with sku, quantity, storeId)
//   - `POST /{id}/confirm` → confirm reservation
//   - `POST /{id}/cancel` → cancel reservation
// - Return `ResponseEntity` with appropriate HTTP status.
// - Handle exceptions with `@ExceptionHandler` methods.
package com.meli.inventory.command.controller;

import com.meli.inventory.model.requests.ReservationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meli.inventory.command.exception.ReservationNotFoundException;
import com.meli.inventory.command.service.ReservationService;
import com.meli.inventory.model.entities.Reservation;
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @Valid @RequestBody ReservationRequest request) {

        Reservation reservation = reservationService.createReservation(
                request.getSku(), request.getQuantity(), request.getStoreId());
        return new ResponseEntity<>(reservation, HttpStatus.CREATED);
    }



    @PostMapping("/{id}/confirm")
    public ResponseEntity<Reservation> confirmReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.confirmReservation(id);
        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.cancelReservation(id);
        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }
}
