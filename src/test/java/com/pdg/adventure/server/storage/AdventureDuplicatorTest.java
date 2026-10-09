package com.pdg.adventure.server.storage;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;
import com.pdg.adventure.server.storage.service.AdventureDuplicator;

/**
 * Duplicating an adventure must produce a fully independent copy: every document that belongs to
 * the original (locations, containers, items, pictures, vocabulary, words) is copied under a new
 * id, and every reference inside the copy points at the copy's own documents. The decisive check
 * is that deleting the copy leaves the original untouched.
 */
@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 CascadeDeleteHelper.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class AdventureDuplicatorTest {

    private static final List<String> COLLECTIONS =
            List.of("adventures", "locations", "containers", "items", "pictures", "vocabularies", "words");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CascadeDeleteHelper cascadeDeleteHelper;

    private AdventureData original;
    private LocationData cellar;
    private LocationData attic;
    private ItemData lamp;
    private ItemData key;
    private PictureData picture;
    private Word takeWord;
    private Word grabWord;

    @BeforeEach
    void saveOriginalAdventure() {
        mongoTemplate.getDb().drop();

        original = new AdventureData();
        original.setTitle("Original");
        original.setFont(AdventureFont.MEDIEVAL_SHARP);

        takeWord = original.getVocabularyData().createWord("take", Word.Type.VERB);
        grabWord = original.getVocabularyData().createSynonym("grab", takeWord);

        cellar = new LocationData();
        attic = new LocationData();
        lamp = new ItemData();
        lamp.setAdventureId(original.getId());
        lamp.setLocationId(cellar.getId());
        lamp.setParentContainerId(cellar.getItemContainerData().getId());
        cellar.getItemContainerData().getItems().add(lamp);

        DirectionData up = new DirectionData();
        up.setDestinationId(attic.getId());
        cellar.getDirectionsData().add(up);

        picture = new PictureData();
        picture.setAdventureId(original.getId());
        picture.setName("cellar.png");
        picture.setContent(new byte[] {1, 2, 3});
        picture.setContentType("image/png");
        cellar.setPictureId(picture.getId());
        original.getPictureData().put(picture.getId(), picture);

        original.getLocationData().put(cellar.getId(), cellar);
        original.getLocationData().put(attic.getId(), attic);
        original.setCurrentLocationId(cellar.getId());

        key = new ItemData();
        key.setAdventureId(original.getId());
        original.getPlayerPocket().getItems().add(key);

        MessageData message = new MessageData("greeting", "hello");
        original.getMessages().put(message.getId(), message);

        mongoTemplate.save(picture);
        mongoTemplate.save(original);
    }

    private AdventureDuplicator duplicator() {
        return new AdventureDuplicator(mongoTemplate);
    }

    private Set<String> allIds(String collection) {
        Set<String> ids = new HashSet<>();
        mongoTemplate.getCollection(collection).find().forEach(doc -> ids.add(doc.get("_id").toString()));
        return ids;
    }

    @Test
    void duplicateStartsFromAPersistedOriginal() {
        // guards the fixture: the vocabulary must really be stored, or the copy checks prove nothing
        assertSoftly(softly -> {
            softly.assertThat(mongoTemplate.getCollection("adventures").countDocuments()).isEqualTo(1);
            softly.assertThat(mongoTemplate.getCollection("locations").countDocuments()).isEqualTo(2);
            softly.assertThat(mongoTemplate.getCollection("containers").countDocuments()).isEqualTo(3);
            softly.assertThat(mongoTemplate.getCollection("items").countDocuments()).isEqualTo(2);
            softly.assertThat(mongoTemplate.getCollection("pictures").countDocuments()).isEqualTo(1);
            softly.assertThat(mongoTemplate.getCollection("vocabularies").countDocuments()).isEqualTo(1);
            softly.assertThat(mongoTemplate.getCollection("words").countDocuments()).isEqualTo(2);
        });
    }

    @Test
    void duplicateDoublesEveryCollectionAndSharesNoDocumentId() {
        Set<String> idsBefore = new HashSet<>();
        COLLECTIONS.forEach(c -> idsBefore.addAll(allIds(c)));
        long[] countsBefore = COLLECTIONS.stream()
                                         .mapToLong(c -> mongoTemplate.getCollection(c).countDocuments()).toArray();

        String copyId = duplicator().duplicate(original.getId(), "Copy of Original");

        assertThat(copyId).isNotEqualTo(original.getId());
        assertSoftly(softly -> {
            for (int i = 0; i < COLLECTIONS.size(); i++) {
                softly.assertThat(mongoTemplate.getCollection(COLLECTIONS.get(i)).countDocuments())
                      .as(COLLECTIONS.get(i)).isEqualTo(countsBefore[i] * 2);
            }
            Set<String> newIds = new HashSet<>();
            COLLECTIONS.forEach(c -> newIds.addAll(allIds(c)));
            newIds.removeAll(idsBefore);
            softly.assertThat(newIds).as("new ids").hasSize(idsBefore.size());
        });
    }

    @Test
    void duplicateReferencesPointAtTheCopysOwnDocuments() {
        String copyId = duplicator().duplicate(original.getId(), "Copy of Original");

        AdventureData copy = mongoTemplate.findById(copyId, AdventureData.class);
        assertThat(copy).isNotNull();

        Set<String> originalLocationIds = Set.of(cellar.getId(), attic.getId());
        LocationData copiedCellar = copy.getLocationData().values().stream()
                                        .filter(l -> !l.getDirectionsData().isEmpty()).findFirst().orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(copy.getTitle()).isEqualTo("Copy of Original");
            softly.assertThat(copy.getFont()).as("run font").isEqualTo(AdventureFont.MEDIEVAL_SHARP);
            softly.assertThat(copy.getLocationData()).hasSize(2);
            softly.assertThat(copy.getLocationData().keySet()).doesNotContainAnyElementsOf(originalLocationIds);
            copy.getLocationData().forEach((key, location) ->
                    softly.assertThat(location.getId()).as("map key matches location id").isEqualTo(key));
            softly.assertThat(copy.getLocationData()).as("current location")
                  .containsKey(copy.getCurrentLocationId());
            softly.assertThat(copy.getLocationData())
                  .as("direction destination")
                  .containsKey(copiedCellar.getDirectionsData().iterator().next().getDestinationId());
            softly.assertThat(copy.getPictureData()).as("picture of location")
                  .containsKey(copiedCellar.getPictureId());
            softly.assertThat(copy.getPictureData().get(copiedCellar.getPictureId()).getAdventureId())
                  .as("picture adventureId").isEqualTo(copyId);
            softly.assertThat(copy.getPictureData().get(copiedCellar.getPictureId()).getContent())
                  .as("picture content").containsExactly(1, 2, 3);

            ItemData copiedLamp = copiedCellar.getItemContainerData().getItems().getFirst();
            softly.assertThat(copiedLamp.getId()).isNotEqualTo(lamp.getId());
            softly.assertThat(copiedLamp.getAdventureId()).as("item adventureId").isEqualTo(copyId);
            softly.assertThat(copiedLamp.getLocationId()).as("item locationId").isEqualTo(copiedCellar.getId());
            softly.assertThat(copiedLamp.getParentContainerId()).as("item parent container")
                  .isEqualTo(copiedCellar.getItemContainerData().getId());
            softly.assertThat(copy.getPlayerPocket().getItems()).as("pocket items").hasSize(1);
            softly.assertThat(copy.getPlayerPocket().getItems().getFirst().getId()).isNotEqualTo(key.getId());
            softly.assertThat(copy.getMessages()).as("embedded messages").hasSize(1);

            VocabularyData vocabulary = copy.getVocabularyData();
            softly.assertThat(vocabulary).as("vocabulary").isNotNull();
            softly.assertThat(vocabulary.getWords()).extracting(Word::getText)
                  .containsExactlyInAnyOrder("take", "grab");
            softly.assertThat(vocabulary.getWords()).extracting(Word::getId)
                  .doesNotContain(takeWord.getId(), grabWord.getId());
            softly.assertThat(vocabulary.findWord("grab").orElseThrow().getSynonym())
                  .as("synonym").isEqualTo(vocabulary.findWord("take").orElseThrow());
        });
    }

    @Test
    void deletingTheCopyLeavesTheOriginalIntact() {
        String copyId = duplicator().duplicate(original.getId(), "Copy of Original");

        AdventureData copy = mongoTemplate.findById(copyId, AdventureData.class);
        cascadeDeleteHelper.cascadeDelete(copy);
        mongoTemplate.remove(copy);

        AdventureData survivor = mongoTemplate.findById(original.getId(), AdventureData.class);
        assertSoftly(softly -> {
            softly.assertThat(survivor).isNotNull();
            softly.assertThat(survivor.getTitle()).isEqualTo("Original");
            softly.assertThat(survivor.getLocationData()).hasSize(2);
            softly.assertThat(survivor.getLocationData().get(cellar.getId()).getItemContainerData().getItems())
                  .extracting(ItemData::getId).containsExactly(lamp.getId());
            softly.assertThat(survivor.getPlayerPocket().getItems()).extracting(ItemData::getId)
                  .containsExactly(key.getId());
            softly.assertThat(survivor.getVocabularyData().getWords()).hasSize(2);
            softly.assertThat(mongoTemplate.getCollection("adventures").countDocuments()).isEqualTo(1);
            softly.assertThat(mongoTemplate.getCollection("locations").countDocuments()).isEqualTo(2);
            softly.assertThat(mongoTemplate.getCollection("containers").countDocuments()).isEqualTo(3);
            softly.assertThat(mongoTemplate.getCollection("items").countDocuments()).isEqualTo(2);
            softly.assertThat(mongoTemplate.getCollection("words").countDocuments()).isEqualTo(2);
        });
    }

    @Test
    void duplicateLeavesTheOriginalDocumentsUnchanged() {
        Document before = mongoTemplate.getCollection("adventures")
                                       .find(new Document("_id", original.getId())).first();

        duplicator().duplicate(original.getId(), "Copy of Original");

        Document after = mongoTemplate.getCollection("adventures")
                                      .find(new Document("_id", original.getId())).first();
        assertThat(after).isEqualTo(before);
    }

    @Test
    void duplicatingAnUnknownAdventureFails() {
        assertThatThrownBy(() -> duplicator().duplicate("does-not-exist", "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does-not-exist");
    }
}
