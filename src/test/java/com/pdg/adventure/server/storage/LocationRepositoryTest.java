package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.mapper.LocationMapper;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;

@Testcontainers
@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LocationRepositoryTest {
//    @Container //
//    private static final MongoDBContainer mongoDBContainer = MongoContainers.getDefaultContainer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

//    @Autowired
    LocationMapper locationMapper;

    @Autowired
    LocationRepository repository;

    @Autowired
    MongoOperations operations;

    String adjectiveOne = "first";
    String noun = "location";
    String shortDescriptionOne = "short 4 location one";
    String longDescriptionOne = "long 4 location one";
    private Location one, two;

    @BeforeEach
    void setUp() {
        // repository.deleteAll();
    }

    @Test
    void saveWorks() {
        DescriptionProvider locationDescription = new DescriptionProvider(adjectiveOne, noun);
        locationDescription.setShortDescription(shortDescriptionOne);
        locationDescription.setLongDescription(longDescriptionOne);
        Container pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);

        one = new Location(locationDescription, pocket);

//        LocationData ld = locationMapper.mapToDO(one);

//        final LocationData locationData = repository.save(ld);
//        assertThat(locationData.getDescriptionData()).isNotNull();
    }
}
