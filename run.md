
# run.md — How to run & demo

## Prerequisites
- Java 17+
- Maven (wrapper included: `./mvnw`)
- VS Code/IntelliJ (optional)

## Install & run
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## URLs
- Swagger UI: http://localhost:8080/swagger-ui.html  (or `/swagger-ui/index.html`)
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - User: `sa` | Password: *(blank)*

## Demo flow (end-to-end)
1) **Check initial inventory (from DataInitializer)**
```
GET /api/v1/inventory
→ Expect SKU 9090 with quantity 50
```

2) **Create reservation (decrease stock)**
```
POST /api/v1/reservations
{
  "sku": "9090",
  "quantity": 5,
  "storeId": "store-001"
}
→ 201 PENDING ; stock becomes 45 ; StockUpdatedEvent published
```

3) **Confirm reservation**
```
POST /api/v1/reservations/{id}/confirm
→ 200 CONFIRMED ; stock remains 45 ; event published
```

4) **Cancel reservation (idempotent guard)**
```
POST /api/v1/reservations/{id}/cancel
→ 200 CANCELLED ; stock restored to 50 (only on first cancel); event published
```

5) **Query inventory (eventually consistent)**
```
GET /api/v1/inventory
GET /api/v1/inventory/9090
GET /api/v1/inventory/changes?since=2025-10-21T09:00:00
```

## Troubleshooting
- **Swagger 404:** ensure `springdoc-openapi-starter-webmvc-ui` dependency and app is running.
- **H2 console 404:** set in `application.yml`:
```
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password:
    generate-unique-name: false
  jpa:
    hibernate:
      ddl-auto: update
```
- **Query empty on startup:** ensure `InventoryQueryService` loads on `ApplicationReadyEvent` and `DataInitializer` inserts SKU 9090.
