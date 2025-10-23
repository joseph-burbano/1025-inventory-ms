package com.meli.inventory.model.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class ReservationRequest {
    @NotBlank(message = "SKU must not be empty")
    private String sku;

    @Positive(message = "Quantity must be positive")
    private int quantity;

    @NotBlank(message = "Store ID must not be empty")
    private String storeId;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}