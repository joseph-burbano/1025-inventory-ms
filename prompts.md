# Prompt Base (para Copilot)

You are a senior Java engineer building a distributed inventory management microservice using Spring Boot 3, following a CQRS and event-driven architecture.  
The project simulates distributed consistency using H2 or SQLite, with strong consistency for stock updates (reservations, confirmations) and eventual consistency for read replicas via simulated events.  

Use clean architecture and domain-driven design best practices:
- Keep command, query, and event logic in separate packages.
- Write readable, testable, and well-documented code.
- Apply optimistic locking or synchronization where concurrency issues may occur.
- Follow RESTful principles for controllers.
- Use Lombok for boilerplate and JPA annotations for persistence.
- Add inline comments explaining architectural decisions.

Your goal is to help generate or complete the Java classes with clean, maintainable code aligned with this architecture.

## Prompt: InventoryItem Entity
Create a JPA entity class called `InventoryItem` inside `com.meli.inventory.model`.

Requirements:
- Fields: id (Long), sku (String), name (String), quantity (Integer), version (Long)
- Annotate with `@Entity`, `@Table(name="inventory_items")`
- Use `@Version` for optimistic locking on `version`
- Add Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Include `@GeneratedValue` for `id`
- Add a helper method `decreaseStock(int amount)` that throws an exception if amount > quantity.

## Prompt: Reservation Entity
Create a JPA entity class `Reservation` inside `com.meli.inventory.model`.

Requirements:
- Fields: id (Long), sku (String), quantity (Integer), status (Enum), storeId (String), createdAt (LocalDateTime), expiresAt (LocalDateTime)
- Annotate with `@Entity`, `@Table(name="reservations")`
- Add Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Use `@Enumerated(EnumType.STRING)` for status
- Create enum `ReservationStatus { PENDING, CONFIRMED, CANCELLED }`
- Add helper method `isExpired()` returning true if now is after `expiresAt`

## Prompt: InventoryService
Create a Spring service class `InventoryService` that handles inventory operations.

Requirements:
- Annotate with `@Service`.
- Inject a `JpaRepository<InventoryItem, Long>` named `inventoryRepository`.
- Methods:
  - `Optional<InventoryItem> findBySku(String sku)`
  - `void decreaseStock(String sku, int amount)` → fetch item by SKU, call `decreaseStock(amount)`, save the entity.
  - `void adjustStock(String sku, int newQuantity)` → directly set new quantity and save.
- Throw `NotEnoughStockException` if not enough stock.
- Use `@Transactional` for modifying methods.

## Prompt: ReservationService
Create a Spring service class `ReservationService` to manage stock reservations.

Requirements:
- Annotate with `@Service`.
- Inject `ReservationRepository` and `InventoryService`.
- Methods:
  - `Reservation createReservation(String sku, int quantity, String storeId)` → check stock, decrease it, save reservation with status `PENDING`.
  - `Reservation confirmReservation(Long reservationId)` → mark status `CONFIRMED`, no stock change.
  - `Reservation cancelReservation(Long reservationId)` → mark status `CANCELLED` and restore stock.
- Throw `ReservationNotFoundException` if reservation doesn't exist.
- Use `@Transactional`.

## Prompt: Custom Exceptions
Create two custom exceptions:
1. `NotEnoughStockException` extending `RuntimeException` with a constructor that takes a message.
2. `ReservationNotFoundException` extending `RuntimeException` with a constructor that takes a message.

## Prompt: ReservationController
Create a REST controller `ReservationController` to handle reservation endpoints.

Requirements:
- Annotate with `@RestController` and `@RequestMapping("/api/v1/reservations")`
- Inject `ReservationService`.
- Endpoints:
  - `POST /` → create reservation (accepts JSON with sku, quantity, storeId)
  - `POST /{id}/confirm` → confirm reservation
  - `POST /{id}/cancel` → cancel reservation
- Return `ResponseEntity` with appropriate HTTP status.
- Handle exceptions with `@ExceptionHandler` methods.

## Prompt: Repositories
Create two interfaces extending JpaRepository:
- `InventoryRepository extends JpaRepository<InventoryItem, Long>` with a method `Optional<InventoryItem> findBySku(String sku)`
- `ReservationRepository extends JpaRepository<Reservation, Long>`
