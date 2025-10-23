package com.meli.inventory.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final SimpleAuthFilter simpleAuthFilter;

    public SecurityConfig(SimpleAuthFilter simpleAuthFilter) {
        this.simpleAuthFilter = simpleAuthFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // ✅ evita 403 en POST por CSRF
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 console
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(simpleAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

