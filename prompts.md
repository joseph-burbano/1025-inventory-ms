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

## Prompt: BaseEvent
Create an abstract class `BaseEvent` inside `com.meli.inventory.events`.

Requirements:
- Fields: `eventId (UUID)`, `timestamp (LocalDateTime)`.
- Constructor initializes both automatically.
- Add Lombok annotations: `@Getter`, `@ToString`.

## Prompt: StockUpdatedEvent
Create a class `StockUpdatedEvent` extending `BaseEvent`.

Requirements:
- Fields: `sku (String)`, `newQuantity (Integer)`.
- Add Lombok annotations: `@Getter`, `@ToString`, `@EqualsAndHashCode(callSuper = true)`.
- Provide constructor accepting `sku` and `newQuantity`.

## Prompt: EventBus
Create a class `EventBus` that simulates an event publisher/subscriber mechanism.

Requirements:
- Maintain a list of `EventListener` subscribers.
- Method `subscribe(EventListener listener)` → adds a subscriber.
- Method `publish(BaseEvent event)` → asynchronously notifies all subscribers using `ExecutorService`.
- Annotate with `@Component`.
- Log every published event (use SLF4J logger).

## Prompt: EventListener Interface
Create an interface `EventListener` with a method:
`void onEvent(BaseEvent event);`

This interface will be implemented by any component that wants to react to published events.

## Prompt: EventBusSimulatorConfig
Create a configuration class `EventBusSimulatorConfig` annotated with `@Configuration`.

Requirements:
- Inject `EventBus`.
- In a `@PostConstruct` method, subscribe a simple listener that logs every received event.
- Use SLF4J logger to print event details.

## Prompt: Refactor Services for Event Publishing
Refactor `InventoryService` and `ReservationService` to publish events for eventual consistency while maintaining strong consistency for writes.

Requirements for both services:
- Add `EventBus` dependency via constructor injection
- Publish `StockUpdatedEvent` after successful stock modifications
- Maintain `@Transactional` and optimistic locking
- Keep strong consistency for write operations
- Enable eventual consistency for read replicas via events

InventoryService specific:
- Publish events in `decreaseStock` and `adjustStock` methods
- Event should contain SKU and new quantity
- Maintain existing exception handling
- Events published after successful save

ReservationService specific:
- Publish events in `createReservation`, `confirmReservation`, and `cancelReservation`
- Event should reflect final stock quantity after operation
- Maintain existing transaction boundaries
- Preserve error handling for not found scenarios
- Publish events after successful reservation status changes

Example pattern for methods:
@Transactional
public void methodName() {
    // 1. Perform database operation
    // 2. Save changes
    // 3. Publish event
    eventBus.publish(new StockUpdatedEvent(sku, newQuantity));
}

## Prompt: InventoryView
Create a simple model class `InventoryView` representing the read model of stock.

Fields:
- sku (String)
- name (String)
- quantity (Integer)
- lastUpdated (LocalDateTime)
Add Lombok annotations `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`.

## Prompt: InventoryQueryService
Create a Spring service `InventoryQueryService` that simulates a read model updated by events.

Requirements:
- Annotate with `@Service`.
- Maintain a `ConcurrentHashMap<String, InventoryView>` to store the current stock per SKU.
- Provide methods:
  - `void handleStockUpdated(StockUpdatedEvent event)` → updates the map.
  - `InventoryView getBySku(String sku)`
  - `List<InventoryView> getAll()`
  - `List<InventoryView> getChangesSince(LocalDateTime timestamp)`
- Use SLF4J for logging updates.

## Prompt: InventoryQueryController
Create a REST controller `InventoryQueryController` with endpoints:

- `GET /api/v1/inventory/{sku}` → return InventoryView by SKU.
- `GET /api/v1/inventory/` → return all inventory views.
- `GET /api/v1/inventory/changes?since=timestamp` → return list of items updated since a given time (ISO format).

Inject `InventoryQueryService`.
Return `ResponseEntity` responses.

## Prompt: Unit Tests for Controller
Generate JUnit 5 + Spring Boot MockMvc tests for {ControllerName}.

Requirements:
- Use `@WebMvcTest({ControllerName}.class)`
- Mock dependent services with `@MockBean`
- Test all endpoints:
  - Happy paths (status 200/201)
  - Validation and bad requests (400)
  - Error scenarios (404, 409)
- Use `MockMvc` to perform real HTTP calls.
- Validate JSON fields with `jsonPath`.
- Use clear naming like `shouldReturnReservationWhenCreated`.

## Prompt: Unit Tests for Service Class
Generate comprehensive JUnit 5 + Mockito tests for the class {ClassName}.

Requirements:
- Cover all public methods and branches.
- Mock all repository or external dependencies.
- Include positive, negative, and edge cases.
- Validate exception handling (e.g., NotEnoughStockException, ReservationNotFoundException).
- Verify interactions using `verify()` and `times()`.
- Use descriptive test names like `shouldDecreaseStockWhenSufficientInventory`.
- Annotate with `@ExtendWith(MockitoExtension.class)`.
- Use `@InjectMocks` and `@Mock`.
- Aim for high coverage and clear arrange-act-assert sections.

## Prompt: Unit Tests for Model
Generate JUnit 5 tests for the class {ModelName}.

Requirements:
- Validate getters/setters using Lombok or manual tests.
- Test helper methods (e.g., `decreaseStock()`, `isExpired()`).
- Cover edge cases (nulls, boundaries).
- For events, verify that IDs and timestamps are auto-generated and not null.
- Use assertions with `assertEquals`, `assertTrue`, etc.

## Prompt: Add fault tolerance with RetryTemplate
Enhance the EventBus class to add retry logic for event publishing.

Requirements:
- Import `org.springframework.retry.support.RetryTemplate`.
- Define a `RetryTemplate` bean in a configuration class (e.g., EventBusConfig).
- In the `publish(BaseEvent event)` method, wrap the event dispatch logic in `retryTemplate.execute(...)`.
- If all retries fail, log an error like: `Failed to publish event after retries`.
- Keep it lightweight: no circuit breaker, just retries with exponential backoff.
- Add unit tests to verify that retries occur when the listener throws an exception.

## Prompt: Add Basic Authentication filter
Add a simple BasicAuth filter to secure all API endpoints.

Requirements:
- Create a class `SimpleAuthFilter` that extends `OncePerRequestFilter`.
- Check for an Authorization header like: `Basic dXNlcjpwYXNz`.
- Validate against hardcoded credentials (user=admin, password=admin123).
- If missing or invalid, return 401 with message "Unauthorized".
- Annotate the filter with `@Component` so it’s registered automatically.
- Ensure Swagger endpoints (`/swagger-ui` and `/v3/api-docs`) are excluded.
- Add a test in `SecurityFilterTest` that verifies 401 and 200 responses.

## Prompt: Add Spring Boot Actuator for metrics
Add observability endpoints to the project.

Requirements:
- Add dependency: `spring-boot-starter-actuator` in `pom.xml`.
- Enable `/actuator/health`, `/actuator/info`, and `/actuator/metrics` endpoints.
- In `application.yml`, configure:
management:
endpoints:
web:
exposure:
include: "health,info,metrics"

markdown
Copiar código
- Add a custom metric counter in `InventoryService` to track successful stock updates.
- Use `MeterRegistry` to increment a counter named "inventory_updates_total".
- Test that `/actuator/metrics/inventory_updates_total` returns expected values.


## Prompt: Add integration tests with MockMvc
Generate end-to-end tests for the REST API using MockMvc.

Requirements:
- Create `ReservationControllerIntegrationTest`.
- Annotate with `@SpringBootTest` and `@AutoConfigureMockMvc`.
- Autowire `MockMvc`.
- Write tests for:
  1. `POST /api/v1/reservations` → returns 201
  2. `POST /api/v1/reservations/{id}/confirm` → returns 200
  3. `POST /api/v1/reservations/{id}/cancel` → returns 200
  4. `GET /api/v1/inventory` → returns 200 with JSON body
- Use H2 preloaded with DataInitializer.
- Validate JSON with `jsonPath` for SKU and quantity.
- Include tests for invalid input (missing SKU, negative quantity → 400).
