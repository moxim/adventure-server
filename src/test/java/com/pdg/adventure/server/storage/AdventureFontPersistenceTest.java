package com.pdg.adventure.server.storage;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;

/**
 * The run font is stored as the enum constant's name on the adventure document. Adventures saved before
 * fonts existed have no such field and must keep looking exactly as they did: {@link AdventureFont#DEFAULT}.
 */
@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 CascadeDeleteHelper.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class AdventureFontPersistenceTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void aNewAdventureUsesTheDefaultFont() {
        assertThat(new AdventureData().getFont()).isEqualTo(AdventureFont.DEFAULT);
    }

    @Test
    void theChosenFontSurvivesASaveAndReload() {
        AdventureData adventure = new AdventureData();
        adventure.setTitle("fantasy");
        adventure.setFont(AdventureFont.CINZEL);
        mongoTemplate.save(adventure);

        assertThat(mongoTemplate.findById(adventure.getId(), AdventureData.class).getFont())
                .isEqualTo(AdventureFont.CINZEL);
        assertThat(mongoTemplate.getCollection("adventures").find(new Document("_id", adventure.getId()))
                                .first().getString("font")).isEqualTo("CINZEL");
    }

    @Test
    void anAdventureStoredBeforeFontsExistedLoadsWithTheDefaultFont() {
        AdventureData adventure = new AdventureData();
        adventure.setTitle("legacy");
        mongoTemplate.save(adventure);
        mongoTemplate.getCollection("adventures").updateOne(new Document("_id", adventure.getId()),
                                                            new Document("$unset", new Document("font", "")));
        assertThat(mongoTemplate.getCollection("adventures").find(new Document("_id", adventure.getId()))
                                .first()).doesNotContainKey("font");

        assertThat(mongoTemplate.findById(adventure.getId(), AdventureData.class).getFont())
                .isEqualTo(AdventureFont.DEFAULT);
    }

    @Test
    void anAdventureWithAnExplicitNullFontLoadsWithTheDefaultFont() {
        AdventureData adventure = new AdventureData();
        adventure.setTitle("null font");
        mongoTemplate.save(adventure);
        mongoTemplate.getCollection("adventures").updateOne(new Document("_id", adventure.getId()),
                                                            new Document("$set", new Document("font", null)));

        assertThat(mongoTemplate.findById(adventure.getId(), AdventureData.class).getFont())
                .isEqualTo(AdventureFont.DEFAULT);
    }
}
