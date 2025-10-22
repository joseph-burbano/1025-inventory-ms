// ## Prompt: EventBus
// Create a class `EventBus` that simulates an event publisher/subscriber mechanism.

// Requirements:
// - Maintain a list of `EventListener` subscribers.
// - Method `subscribe(EventListener listener)` → adds a subscriber.
// - Method `publish(BaseEvent event)` → asynchronously notifies all subscribers using `ExecutorService`.
// - Annotate with `@Component`.
// - Log every published event (use SLF4J logger).
package com.meli.inventory.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
@Component
public class EventBus {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private final List<EventListener> listeners = new ArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void publish(BaseEvent event) {
        logger.info("Publishing event: {}", event);
        for (EventListener listener : listeners) {
            executorService.submit(() -> listener.onEvent(event));
        }
    }
}