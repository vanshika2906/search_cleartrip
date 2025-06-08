package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "org.example.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Additional database configurations can be added here if needed
} 