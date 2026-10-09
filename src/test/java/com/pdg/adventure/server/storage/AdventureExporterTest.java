package com.pdg.adventure.server.storage;

import org.bson.Document;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;
import com.pdg.adventure.server.storage.service.AdventureExporter;
import com.pdg.adventure.server.storage.service.BuilderVersion;
import com.pdg.adventure.server.testhelper.TestSupporter;

@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 CascadeDeleteHelper.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class AdventureExporterTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    private AdventureData original;
    private AdventureExporter exporter;

    @BeforeEach
    void saveOriginalAdventure() {
        mongoTemplate.getDb().drop();
        original = TestSupporter.createSampleAdventure("Original");
        original.getPictureData().values().forEach(mongoTemplate::save);
        mongoTemplate.save(original);
        exporter = new AdventureExporter(mongoTemplate, BuilderVersion.of("1.2.3"));
    }

    private Document exportedEnvelope() {
        byte[] json = exporter.export(original.getId());
        return Document.parse(new String(json, StandardCharsets.UTF_8));
    }

    @Test
    void exportWritesTheEnvelopeHeader() {
        Document envelope = exportedEnvelope();

        assertSoftly(softly -> {
            softly.assertThat(envelope.getString("format")).isEqualTo("adventurebuilder-adventure");
            softly.assertThat(envelope.getInteger("formatVersion")).isEqualTo(1);
            softly.assertThat(envelope.getString("builderVersion")).isEqualTo("1.2.3");
            softly.assertThat(envelope.getString("exportedAt")).isNotBlank();
        });
    }

    @Test
    void exportHoldsTheWholeGraphUnderItsCollections() {
        Document documents = exportedEnvelope().get("documents", Document.class);

        assertSoftly(softly -> {
            softly.assertThat(documents.keySet()).containsExactlyInAnyOrder(
                    "adventures", "locations", "containers", "items", "pictures", "vocabularies", "words");
            softly.assertThat(documents.getList("adventures", Document.class)).hasSize(1);
            softly.assertThat(documents.getList("locations", Document.class)).hasSize(2);
            softly.assertThat(documents.getList("containers", Document.class)).hasSize(3);
            softly.assertThat(documents.getList("items", Document.class)).hasSize(2);
            softly.assertThat(documents.getList("pictures", Document.class)).hasSize(1);
            softly.assertThat(documents.getList("vocabularies", Document.class)).hasSize(1);
            softly.assertThat(documents.getList("words", Document.class)).hasSize(2);
            softly.assertThat(documents.getList("adventures", Document.class).getFirst().getString("_id"))
                  .isEqualTo(original.getId());
        });
    }

    @Test
    void exportKeepsPictureBytesAndReferencesTyped() {
        Document documents = exportedEnvelope().get("documents", Document.class);

        Document picture = documents.getList("pictures", Document.class).getFirst();
        Document adventure = documents.getList("adventures", Document.class).getFirst();
        Document aLocationReference = adventure.get("locationData", Document.class).values().stream()
                                               .map(Document.class::cast).findFirst().orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(picture.get("content", Binary.class).getData()).containsExactly(1, 2, 3);
            softly.assertThat(aLocationReference.getString("$ref")).isEqualTo("locations");
            softly.assertThat(adventure.getDate("createdAt")).isNotNull();
        });
    }

    @Test
    void exportOfAnUnknownAdventureFails() {
        assertThatThrownBy(() -> exporter.export("does-not-exist"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does-not-exist");
    }

    @Test
    void exportRefusesAnAdventureWhoseReferencesAreBroken() {
        mongoTemplate.getCollection("locations").deleteMany(new Document());

        assertThatThrownBy(() -> exporter.export(original.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer exist");
    }
}
