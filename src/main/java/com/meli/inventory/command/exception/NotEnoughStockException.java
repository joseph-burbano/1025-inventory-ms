// ## Prompt: Custom Exceptions
// Create custom exception:
// 1. `NotEnoughStockException` extending `RuntimeException` with a constructor that takes a message.
package com.meli.inventory.command.exception;

public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException(String message) {
        super(message);
    }
}
