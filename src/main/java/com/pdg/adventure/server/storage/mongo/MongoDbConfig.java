package com.pdg.adventure.server.storage.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing  // This is the magic—auto-populates @CreatedDate etc.
public class MongoDbConfig {
}
