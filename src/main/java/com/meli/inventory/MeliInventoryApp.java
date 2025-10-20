// ## Create main spring boot application class
package com.meli.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class MeliInventoryApp {
    public static void main(String[] args) {
        SpringApplication.run(MeliInventoryApp.class, args);
    }
}