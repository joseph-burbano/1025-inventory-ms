// ## Prompt: Reservation Entity
// Create a JPA entity class `Reservation` inside `com.meli.inventory.model`.

// Requirements:
// - Fields: id (Long), sku (String), quantity (Integer), status (Enum), storeId (String), createdAt (LocalDateTime), expiresAt (LocalDateTime)
// - Annotate with `@Entity`, `@Table(name="reservations")`
// - Add Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
// - Use `@Enumerated(EnumType.STRING)` for status
// - Create enum `ReservationStatus { PENDING, CONFIRMED, CANCELLED }`
// - Add helper method `isExpired()` returning true if now is after `expiresAt`
package com.meli.inventory.model.entities;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String sku;

    @NonNull
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @NonNull
    private ReservationStatus status;

    @NonNull
    private String storeId;

    @NonNull
    private LocalDateTime createdAt = LocalDateTime.now();

    @NonNull
    private LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public enum ReservationStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }
}