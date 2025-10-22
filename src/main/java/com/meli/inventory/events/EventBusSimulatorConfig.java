// ## Prompt: EventBusSimulatorConfig
// Create a configuration class `EventBusSimulatorConfig` annotated with `@Configuration`.

// Requirements:
// - Inject `EventBus`.
// - In a `@PostConstruct` method, subscribe a simple listener that logs every received event.
// - Use SLF4J logger to print event details.

package com.meli.inventory.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.meli.inventory.query.service.InventoryQueryService;

import jakarta.annotation.PostConstruct;

@Configuration
public class EventBusSimulatorConfig {
    private static final Logger logger = LoggerFactory.getLogger(EventBusSimulatorConfig.class);

    private final EventBus eventBus;
    private final InventoryQueryService queryService;

    @Autowired
    public EventBusSimulatorConfig(EventBus eventBus, InventoryQueryService queryService) {
        this.eventBus = eventBus;
        this.queryService = queryService;
    }

    @PostConstruct
    public void init() {
        // Log all events for debugging
        eventBus.subscribe(event -> logger.info("Received event: {}", event));
        
        // Subscribe query service to stock update events for read model synchronization
        eventBus.subscribe(event -> {
            if (event instanceof StockUpdatedEvent stockEvent) {
                logger.debug("Processing stock update event for SKU: {}", stockEvent.getSku());
                queryService.handleStockUpdated(stockEvent);
            }
        });
    }
}
