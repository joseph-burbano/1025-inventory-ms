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

import com.meli.inventory.events.EventBus;
import com.meli.inventory.events.StockUpdatedEvent;
import com.meli.inventory.query.service.InventoryQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meli.inventory.command.exception.ReservationNotFoundException;
import com.meli.inventory.model.entities.Reservation;
import com.meli.inventory.model.entities.Reservation.ReservationStatus;
import com.meli.inventory.model.repositories.ReservationRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final EventBus eventBus;
    private static final Logger logger = LoggerFactory.getLogger(InventoryQueryService.class);


    @Autowired
    public ReservationService(ReservationRepository reservationRepository, InventoryService inventoryService, EventBus eventBus) {
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.eventBus = eventBus;
    }

    @Transactional
    public Reservation createReservation(String sku, int quantity, String storeId) {
        // First decrease stock (this will throw exception if not enough stock)
        inventoryService.decreaseStock(sku, quantity);

        // Create and save reservation
        Reservation reservation = new Reservation();
        reservation.setSku(sku);
        reservation.setQuantity(quantity);
        reservation.setStoreId(storeId);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation = reservationRepository.save(reservation);
        
        // Publish stock updated event for eventual consistency
        eventBus.publish(new StockUpdatedEvent(sku,
            inventoryService.findBySku(sku).get().getQuantity()));
            
        return reservation;
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationId + " not found."));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation = reservationRepository.save(reservation);
        
        // Publish stock updated event for eventual consistency
        eventBus.publish(new StockUpdatedEvent(reservation.getSku(),
            inventoryService.findBySku(reservation.getSku()).get().getQuantity()));
            
        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationId + " not found."));

        // 🔒 Verificar si ya estaba cancelada o confirmada
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            logger.warn("Reservation {} is already cancelled — skipping stock restore", reservationId);
            return reservation;
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            logger.warn("Reservation {} already confirmed — cannot cancel", reservationId);
            return reservation;
        }

        // Cambiar estado
        reservation.setStatus(ReservationStatus.CANCELLED);

        // Restaurar stock solo una vez
        int reservedQty = reservation.getQuantity();
        int newQuantity = inventoryService.findBySku(reservation.getSku())
                .map(item -> item.getQuantity() + reservedQty)
                .orElse(reservedQty);

        inventoryService.adjustStock(reservation.getSku(), newQuantity);
        reservation = reservationRepository.save(reservation);

        // Publicar evento
        eventBus.publish(new StockUpdatedEvent(reservation.getSku(), newQuantity));

        return reservation;
    }


}


