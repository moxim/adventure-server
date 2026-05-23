package com.pdg.adventure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.pdg.adventure.server.security.repository") // For MySQL
@EnableMongoRepositories(basePackages = "com.pdg.adventure.server.storage.repository") // For MongoDB
public class DatabaseConfig {
    // This empty class acts as the instruction manual for Spring Boot
}
