package org.example.connectcg_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Enable Spring Retry for automatic transaction retry on deadlock
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
