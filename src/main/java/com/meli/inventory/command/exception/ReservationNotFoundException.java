// ## Prompt: Custom Exceptions
// Create custom exception:
// 1. `ReservationNotFoundException` extending `RuntimeException` with a constructor that takes a message.
package com.meli.inventory.command.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}