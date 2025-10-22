// ## Prompt: Add fault tolerance with RetryTemplate
// Enhance the EventBus class to add retry logic for event publishing.

// Requirements:
// - Import `org.springframework.retry.support.RetryTemplate`.
// - Define a `RetryTemplate` bean in a configuration class (e.g., EventBusConfig).
// - In the `publish(BaseEvent event)` method, wrap the event dispatch logic in `retryTemplate.execute(...)`.
// - If all retries fail, log an error like: `Failed to publish event after retries`.
// - Keep it lightweight: no circuit breaker, just retries with exponential backoff.
// - Add unit tests to verify that retries occur when the listener throws an exception.


package com.meli.inventory.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class EventBusConfig {
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure exponential backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500); // initial interval in milliseconds
        backOffPolicy.setMultiplier(2.0); // multiplier for exponential backoff
        backOffPolicy.setMaxInterval(5000); // maximum interval in milliseconds
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Configure simple retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5); // maximum number of retry attempts
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}

