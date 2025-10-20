// ## Prompt: ReservationService
// Create a Spring service class `ReservationService` to manage stock reservations.

// Requirements:
// - Annotate with `@Service`.
// - Inject `ReservationRepository` and `InventoryService`.
// - Methods:
//   - `Reservation createReservation(String sku, int quantity, String storeId)` → check stock, decrease it, save reservation with status `PENDING`.
//   - `Reservation confirmReservation(Long reservationId)` → mark status `CONFIRMED`, no stock change.
//   - `Reservation cancelReservation(Long reservationId)` → mark status `CANCELLED` and restore stock.
// - Throw `ReservationNotFoundException` if reservation doesn't exist.
// - Use `@Transactional`.
package com.meli.inventory.command.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meli.inventory.command.exception.ReservationNotFoundException;
import com.meli.inventory.model.Reservation;
import com.meli.inventory.model.Reservation.ReservationStatus;
import com.meli.inventory.model.ReservationRepository;
@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, InventoryService inventoryService) {
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public Reservation createReservation(String sku, int quantity, String storeId) {
        inventoryService.decreaseStock(sku, quantity);
        Reservation reservation = new Reservation();
        reservation.setSku(sku);
        reservation.setQuantity(quantity);
        reservation.setStoreId(storeId);
        reservation.setStatus(ReservationStatus.PENDING);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationId + " not found."));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }
    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationId + " not found."));
        reservation.setStatus(ReservationStatus.CANCELLED);
        inventoryService.adjustStock(reservation.getSku(),
                inventoryService.findBySku(reservation.getSku())
                        .map(item -> item.getQuantity() + reservation.getQuantity())
                        .orElse(reservation.getQuantity()));
        return reservationRepository.save(reservation);
    }
}


