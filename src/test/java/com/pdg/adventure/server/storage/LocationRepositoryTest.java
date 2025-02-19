package com.pdg.adventure.server.storage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;

//@DataMongoTest()
//@ExtendWith(SpringExtension.class)
//@AutoConfigureDataMongo
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LocationRepositoryTest {

    @Autowired
    LocationRepository repository;

    @Autowired
    MongoOperations operations;

    String adjectiveOne = "first";
    String noun = "location";
    String shortDescriptionOne = "short 4 location one";
    String longDescriptionOne = "long 4 location one";
    private Location one, two;

    @Test
    void example(@Autowired final MongoTemplate mongoTemplate) {
      Assertions.assertThat(mongoTemplate.getDb()).isNotNull();
    }

    @Test
    void saveWorks(@Autowired MongoTemplate mongoTemplate) {
        List<LocationData> locationDataList = mongoTemplate.findAll(LocationData.class);
        assertThat(locationDataList.size()).isEqualTo(0);

        mongoTemplate.save(new LocationData());
        locationDataList = mongoTemplate.findAll(LocationData.class);
        assertThat(locationDataList.size()).isEqualTo(1);

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
