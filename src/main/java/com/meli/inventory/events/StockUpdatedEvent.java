// ## Prompt: StockUpdatedEvent
// Create a class `StockUpdatedEvent` extending `BaseEvent`.

// Requirements:
// - Fields: `sku (String)`, `newQuantity (Integer)`.
// - Add Lombok annotations: `@Getter`, `@ToString`, `@EqualsAndHashCode(callSuper = true)`.
// - Provide constructor accepting `sku` and `newQuantity`.
package com.meli.inventory.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class StockUpdatedEvent extends BaseEvent {
    private final String sku;
    private final Integer newQuantity;

    public StockUpdatedEvent(String sku, Integer newQuantity) {
        super();
        this.sku = sku;
        this.newQuantity = newQuantity;
    }
}