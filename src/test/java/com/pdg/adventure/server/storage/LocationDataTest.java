package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.BasicDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;

@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
@Order(1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocationDataTest {

    String adjectiveText = "first";
    String nounText = "location";

    @Test
    @Order(1)
    void hasDb(@Autowired final MongoTemplate mongoTemplate) {
        assertThat(mongoTemplate.getDb()).isNotNull();
    }

    @Test
    @Order(2)
    void saveWorks(@Autowired MongoTemplate mongoTemplate) {
        List<LocationData> locationDataList = mongoTemplate.findAll(LocationData.class);
        assertThat(locationDataList).isEmpty();

        mongoTemplate.save(new LocationData());
        locationDataList = mongoTemplate.findAll(LocationData.class);
        assertThat(locationDataList).hasSize(1);
    }

    @Test
    @Order(3)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void findSpecificLocation(@Autowired MongoTemplate mongoTemplate) {
        // Create and explicitly save the Words first
        Word nounWord = new Word(nounText, Word.Type.NOUN);
//        mongoTemplate.save(nounWord);  // Saves to "wordData" collection

        Word adjectiveWord = new Word(adjectiveText, Word.Type.ADJECTIVE);
//        mongoTemplate.save(adjectiveWord);  // Saves to "wordData" collection

        // Now set them on DescriptionData
        final DescriptionData descriptionDataToSave = new DescriptionData();
        descriptionDataToSave.setNoun(nounWord);
        descriptionDataToSave.setAdjective(adjectiveWord);

        // Create and save LocationData (DBRefs will now point to existing Words)
        final LocationData locationDataToSave = new LocationData();
        locationDataToSave.setDescriptionData(descriptionDataToSave);
        mongoTemplate.save(locationDataToSave);

        // Step 1: Find the Words by text and type
        Query nounQuery = new Query();
        nounQuery.addCriteria(Criteria.where("text").is(nounText));
        nounQuery.addCriteria(Criteria.where("type").is(Word.Type.NOUN));
        Word foundNoun = mongoTemplate.findOne(nounQuery, Word.class);

        Query adjectiveQuery = new Query();
        adjectiveQuery.addCriteria(Criteria.where("text").is(adjectiveText));
        adjectiveQuery.addCriteria(Criteria.where("type").is(Word.Type.ADJECTIVE));
        Word foundAdjective = mongoTemplate.findOne(adjectiveQuery, Word.class);

        // These should now pass
        assertThat(foundNoun).isNotNull();
        assertThat(foundAdjective).isNotNull();

        // Step 2: Query LocationData using the Word IDs
        Query locationQuery = new Query();
        locationQuery.addCriteria(Criteria.where("descriptionData.noun.$id").is(foundNoun.getId()));
        locationQuery.addCriteria(Criteria.where("descriptionData.adjective.$id").is(foundAdjective.getId()));

        LocationData foundLocationData = mongoTemplate.findOne(locationQuery, LocationData.class);

        // Verify
        assertThat(foundLocationData).isNotNull();
        assertThat(foundLocationData.getDescriptionData().getNoun().getText()).isEqualTo(nounText);
        assertThat(foundLocationData.getDescriptionData().getAdjective().getText()).isEqualTo(adjectiveText);

        List<LocationData> allLocations = mongoTemplate.findAll(LocationData.class);
        assertThat(allLocations).hasSize(1);
    }

    @Test
    void checkCascadeSaveWorksForBasicDescriptions(@Autowired MongoTemplate mongoTemplate) {
        BasicDescriptionData basicDescriptionData = new BasicDescriptionData();
        basicDescriptionData.setNoun(new Word(nounText, Word.Type.NOUN));
        basicDescriptionData.setAdjective(new Word(adjectiveText, Word.Type.ADJECTIVE));

        final BasicDescriptionData saved = mongoTemplate.save(basicDescriptionData);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        List<BasicDescriptionData> found = mongoTemplate.findAll(BasicDescriptionData.class);
        assertThat(found).isNotNull();
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getAdjective().getText()).isEqualTo(adjectiveText);
        assertThat(found.getFirst().getAdjective().getType()).isEqualTo(Word.Type.ADJECTIVE);
        assertThat(found.getFirst().getNoun().getText()).isEqualTo(nounText);
        assertThat(found.getFirst().getNoun().getType()).isEqualTo(Word.Type.NOUN);
    }
}

// LFq51qqupnaiTNn39w6zATiOTxZI2JYuRJEBlzmUDv4zeeNlXhMgJZVb0q5QkLr+CIUrSuNB7ucifrGXawLB4qswPOXYG7+ItDNUR/9UkLTUWlnHLX07hnR1USOrWIjTmbytcIKEdaI6x0RskyotuItj84xxoSBP/iRBW2EHpOc
