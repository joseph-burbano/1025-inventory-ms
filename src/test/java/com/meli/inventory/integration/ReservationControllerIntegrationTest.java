// ## Prompt: Add integration tests with MockMvc
// Generate end-to-end tests for the REST API using MockMvc.

// Requirements:
// - Create `ReservationControllerIntegrationTest`.
// - Annotate with `@SpringBootTest` and `@AutoConfigureMockMvc`.
// - Autowire `MockMvc`.
// - Write tests for:
//   1. `POST /api/v1/reservations` → returns 201
//   2. `POST /api/v1/reservations/{id}/confirm` → returns 200
//   3. `POST /api/v1/reservations/{id}/cancel` → returns 200
//   4. `GET /api/v1/inventory` → returns 200 with JSON body
// - Use H2 preloaded with DataInitializer.
// - Validate JSON with `jsonPath` for SKU and quantity.
// - Include tests for invalid input (missing SKU, negative quantity → 400).


package com.meli.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.inventory.model.requests.ReservationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH_HEADER =
            "Basic " + Base64.getEncoder().encodeToString("admin:admin123".getBytes());

    @Test
    void createReservation_ValidInput_ShouldReturnCreated() throws Exception {
        // Arrange
        ReservationRequest request = new ReservationRequest();
        request.setSku("9090");
        request.setQuantity(2);
        request.setStoreId("store1");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("9090"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        // Extract reservation ID
        String response = result.getResponse().getContentAsString();
        Integer reservationId = objectMapper.readTree(response).get("id").asInt();

        // Confirm reservation
        mockMvc.perform(post("/api/v1/reservations/{id}/confirm", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createReservation_InvalidSku_ShouldReturnBadRequest() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setSku("");  // Invalid SKU
        request.setQuantity(1);
        request.setStoreId("store1");

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReservation_NegativeQuantity_ShouldReturnBadRequest() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setSku("9090");
        request.setQuantity(-1);  // Invalid quantity
        request.setStoreId("store1");

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reservationLifecycle_ShouldFollowExpectedFlow() throws Exception {
        // Create reservation
        ReservationRequest request = new ReservationRequest();
        request.setSku("9090");
        request.setQuantity(1);
        request.setStoreId("store1");

        MvcResult createResult = mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        Integer reservationId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asInt();

        // Cancel reservation
        mockMvc.perform(post("/api/v1/reservations/{id}/cancel", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify inventory restored
        mockMvc.perform(get("/api/v1/inventory/9090")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(50)); // initial from DataInitializer
    }

    @Test
    void getAllInventory_ShouldReturnOkWithItems() throws Exception {
        mockMvc.perform(get("/api/v1/inventory")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].sku").exists())
                .andExpect(jsonPath("$[0].quantity").exists());
    }

    @Test
    void confirmNonExistingReservation_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/reservations/{id}/confirm", 999999)
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
