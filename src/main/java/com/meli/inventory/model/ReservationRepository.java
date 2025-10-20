// ## Prompt: Repositories
// Create interface extending JpaRepository:
// - `ReservationRepository extends JpaRepository<Reservation, Long>`
package com.meli.inventory.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
