// ## Prompt: EventListener Interface
// Create an interface `EventListener` with a method:
// `void onEvent(BaseEvent event);`

// This interface will be implemented by any component that wants to react to published events.
package com.meli.inventory.events;

public interface EventListener {
    void onEvent(BaseEvent event);
}
