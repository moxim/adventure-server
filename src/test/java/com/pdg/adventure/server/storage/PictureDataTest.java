package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;

@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class PictureDataTest {

    @Test
    void saveAndFind_roundTripsBinaryContentAndContentType(@Autowired MongoTemplate mongoTemplate) {
        PictureData picture = new PictureData();
        picture.setAdventureId("adv-1");
        picture.setName("treasure-chest");
        picture.setContentType("image/png");
        picture.setContent(new byte[] {1, 2, 3, 4, 5});

        mongoTemplate.save(picture);

        PictureData found = mongoTemplate.findById(picture.getId(), PictureData.class);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("treasure-chest");
        assertThat(found.getContentType()).isEqualTo("image/png");
        assertThat(found.getContent()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @org.springframework.test.annotation.DirtiesContext(
            methodMode = org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD)
    void findAll_returnsSavedPictures(@Autowired MongoTemplate mongoTemplate) {
        mongoTemplate.save(new PictureData());

        List<PictureData> all = mongoTemplate.findAll(PictureData.class);
        assertThat(all).hasSize(1);
    }
}
