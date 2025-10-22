
# Meli Inventory System — CQRS + Event-Driven (Spring Boot)

## Overview
Prototype for Mercado Libre technical interview: redesign an inventory system to reduce update latency, improve consistency, and lower operational costs in a distributed setup.

**Key choices**
- **Architecture:** CQRS + Event-Driven.
- **Consistency strategy:** Strong consistency on writes (reservations/adjustments), eventual consistency on reads (query view updated by events).
- **Stack:** Java 17, Spring Boot 3, Spring Data JPA, H2 (in‑memory), Springdoc OpenAPI, JUnit 5 + Mockito, SLF4J.
- **Messaging:** Simulated Event Bus (ExecutorService) publishing domain events (`StockUpdatedEvent`) to subscribers.
- **Observability:** Logs + (optional) Actuator.

## Repository layout
```
src/
 ├─ main/java/com/meli/inventory/
 │   ├─ command/
 │   │   ├─ controller/ReservationController.java
 │   │   ├─ service/InventoryService.java
 │   │   └─ service/ReservationService.java
 │   ├─ query/
 │   │   ├─ controller/InventoryQueryController.java
 │   │   └─ service/InventoryQueryService.java
 │   ├─ events/ (BaseEvent, StockUpdatedEvent, EventBus, config)
 │   └─ model/ (InventoryItem, Reservation, ReservationStatus, repositories)
 └─ test/java/... (unit tests)
```

## API (high level)
### Command (writes)
- `POST /api/v1/reservations` — create reservation (decrease stock, status=PENDING)
- `POST /api/v1/reservations/{id}/confirm` — confirm reservation
- `POST /api/v1/reservations/{id}/cancel` — cancel reservation (restore stock once)

### Query (reads, eventually consistent)
- `GET /api/v1/inventory` — list inventory views
- `GET /api/v1/inventory/{sku}` — single item
- `GET /api/v1/inventory/changes?since=DATE` — items updated since timestamp

OpenAPI/Swagger: `http://localhost:8080/swagger-ui.html` (or `/swagger-ui/index.html`)

## Data model (summary)
- **InventoryItem**(id, sku, name, quantity, version@OptimisticLock)
- **Reservation**(id, sku, quantity, status[PENDING|CONFIRMED|CANCELLED], storeId, createdAt, expiresAt)
- **Events:** BaseEvent(eventId,timestamp), StockUpdatedEvent(sku,newQuantity)

## Consistency & concurrency
- Writes go through `InventoryService`/`ReservationService` with transactions and optimistic locking.
- `ReservationService.cancelReservation` restores stock **only if** current status is `PENDING` → `CANCELLED` (idempotent guard).
- Eventual consistency: `InventoryQueryService` holds a `ConcurrentHashMap` read model; it loads initial data on `ApplicationReadyEvent` and updates on `StockUpdatedEvent`.

## Fault tolerance
- EventBus is async with try/catch logging; retries/backoff are easy to add.
- Idempotency at cancel/confirm endpoints prevents double-apply of stock changes.

## Security (optional for prototype)
- Can add BasicAuth or a JWT filter for admin endpoints (`/inventory/adjust`).

## Observability
- Structured logs. Add Spring Boot Actuator for `/actuator/health` and `/actuator/metrics` if desired.

## Testing
- **Unit tests** with JUnit 5 & Mockito (services, controllers, query service, models/events).
- Edge cases: insufficient stock, double cancel, confirm non-existing, query since timestamp.

## Docker Support

### Using Docker
Build and run the application using Docker:
```bash
# Build the image
docker build -t meli-inventory .

# Run the container
docker run -p 8080:8080 meli-inventory
```

### Using Docker Compose
Start the application with persistent H2 database:
```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down
```

The application will be available at http://localhost:8080

## Trade-offs
- Kafka/NATS not included to reduce setup time → simulated bus shows the pattern.
- Read model in-memory → blazing fast but ephemeral (acceptable for prototype).

## Future work
- Replace EventBus with Kafka (Spring for Apache Kafka), add DLQ & retries.
- Materialized read store (Redis/Postgres) for durability.
- Sagas for cross-aggregate transactions.
- Cache invalidation & TTL tuning; multi-store sharding.

