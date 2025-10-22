package com.meli.inventory.config;

import com.meli.inventory.model.entities.InventoryItem;
import com.meli.inventory.model.repositories.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(InventoryRepository inventoryRepository) {
        return args -> {
            if (inventoryRepository.findBySku("9090").isEmpty()) {
                InventoryItem item = new InventoryItem();
                item.setSku("9090");
                item.setName("Demo Product");
                item.setQuantity(50);
                inventoryRepository.save(item);
                System.out.println("✅ Inventory initialized with demo product SKU 9090");
            }
        };
    }
}