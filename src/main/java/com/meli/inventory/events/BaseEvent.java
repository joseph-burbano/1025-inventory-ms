// ## Prompt: BaseEvent
// Create an abstract class `BaseEvent` inside `com.meli.inventory.events`.

// Requirements:
// - Fields: `eventId (UUID)`, `timestamp (LocalDateTime)`.
// - Constructor initializes both automatically.
// - Add Lombok annotations: `@Getter`, `@ToString`.
package com.meli.inventory.events;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@ToString
public abstract class BaseEvent {
    private final UUID eventId;
    private final LocalDateTime timestamp;

    public BaseEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }
}