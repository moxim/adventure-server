package com.pdg.adventure.server.testhelper;


import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@TestConfiguration
public class MongoTestConfiguration {
    @Bean
    public MongoTemplate mongoTemplate() {
        return Mockito.mock(MongoTemplate.class);
    }
//
//    @Bean
//    public LocationRepository locationRepository() {
//        return Mockito.mock(LocationRepository.class);
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
//        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, "test"));
//    }

}
