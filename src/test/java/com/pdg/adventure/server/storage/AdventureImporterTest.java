package com.pdg.adventure.server.storage;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.types.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.exception.AdventureImportException;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;
import com.pdg.adventure.server.storage.service.AdventureExporter;
import com.pdg.adventure.server.storage.service.AdventureImporter;
import com.pdg.adventure.server.storage.service.BuilderVersion;
import com.pdg.adventure.server.testhelper.TestSupporter;

@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 CascadeDeleteHelper.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class AdventureImporterTest {

    private static final List<String> COLLECTIONS =
            List.of("adventures", "locations", "containers", "items", "pictures", "vocabularies", "words");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CascadeDeleteHelper cascadeDeleteHelper;

    private AdventureData original;
    private AdventureExporter exporter;
    private AdventureImporter importer;

    @BeforeEach
    void saveOriginalAdventure() {
        mongoTemplate.getDb().drop();
        original = TestSupporter.createSampleAdventure("Original");
        persist(original);
        exporter = new AdventureExporter(mongoTemplate, BuilderVersion.of("1.2.3"));
        importer = importerWithVersion("1.2.3");
    }

    private AdventureImporter importerWithVersion(String aVersion) {
        return new AdventureImporter(mongoTemplate, BuilderVersion.of(aVersion), AdventureImporter.DEFAULT_MAX_BYTES);
    }

    private void persist(AdventureData anAdventure) {
        anAdventure.getPictureData().values().forEach(mongoTemplate::save);
        mongoTemplate.save(anAdventure);
    }

    private Map<String, Long> counts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        COLLECTIONS.forEach(c -> counts.put(c, mongoTemplate.getCollection(c).countDocuments()));
        return counts;
    }

    private Set<String> allIds() {
        Set<String> ids = new HashSet<>();
        COLLECTIONS.forEach(c -> mongoTemplate.getCollection(c).find()
                                              .forEach(doc -> ids.add(doc.get("_id").toString())));
        return ids;
    }

    /** The exported file with the parsed envelope edited by the given change. */
    private byte[] editedExport(Consumer<Document> aChange) {
        Document envelope = Document.parse(new String(exporter.export(original.getId()), StandardCharsets.UTF_8));
        aChange.accept(envelope);
        return envelope.toJson(JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build())
                       .getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void exportThenImportIntoAnEmptyDatabaseRebuildsTheAdventure() {
        byte[] file = exporter.export(original.getId());
        Map<String, Long> countsBefore = counts();
        mongoTemplate.getDb().drop();

        AdventureImporter.ImportResult result = importer.importAdventure(file);

        AdventureData imported = mongoTemplate.findById(result.adventureId(), AdventureData.class);
        assertThat(imported).isNotNull();
        LocationData cellar = imported.getLocationData().values().stream()
                                      .filter(l -> !l.getDirectionsData().isEmpty()).findFirst().orElseThrow();
        VocabularyData vocabulary = imported.getVocabularyData();
        assertSoftly(softly -> {
            softly.assertThat(result.builderVersionDiffers()).isFalse();
            softly.assertThat(counts()).isEqualTo(countsBefore);
            softly.assertThat(imported.getTitle()).isEqualTo("Original");
            softly.assertThat(imported.getFont()).isEqualTo(AdventureFont.MEDIEVAL_SHARP);
            softly.assertThat(imported.getVariableData().getVariableValue("score")).isEqualTo(3);
            softly.assertThat(imported.getMessages()).hasSize(1);
            softly.assertThat(imported.getId()).isNotEqualTo(original.getId());
            softly.assertThat(imported.getLocationData()).hasSize(2);
            softly.assertThat(imported.getLocationData()).containsKey(imported.getCurrentLocationId());
            softly.assertThat(imported.getLocationData())
                  .containsKey(cellar.getDirectionsData().iterator().next().getDestinationId());
            softly.assertThat(imported.getPictureData().get(cellar.getPictureId()).getContent())
                  .containsExactly(1, 2, 3);
            softly.assertThat(imported.getPictureData().get(cellar.getPictureId()).getAdventureId())
                  .isEqualTo(imported.getId());
            softly.assertThat(cellar.getItemContainerData().getItems()).extracting(ItemData::getAdventureId)
                  .containsOnly(imported.getId());
            softly.assertThat(imported.getPlayerPocket().getItems()).hasSize(1);
            softly.assertThat(vocabulary.getWords()).extracting(Word::getText)
                  .containsExactlyInAnyOrder("take", "grab");
            softly.assertThat(vocabulary.findWord("grab").orElseThrow().getSynonym())
                  .isEqualTo(vocabulary.findWord("take").orElseThrow());
        });
    }

    @Test
    void importingIntoTheDatabaseTheFileCameFromCreatesAnIndependentCopy() {
        byte[] file = exporter.export(original.getId());
        Set<String> idsBefore = allIds();
        Map<String, Long> countsBefore = counts();

        String firstId = importer.importAdventure(file).adventureId();
        String secondId = importer.importAdventure(file).adventureId();

        assertSoftly(softly -> {
            softly.assertThat(Set.of(original.getId(), firstId, secondId)).hasSize(3);
            countsBefore.forEach((collection, count) -> softly
                    .assertThat(mongoTemplate.getCollection(collection).countDocuments())
                    .as(collection).isEqualTo(count * 3));
            Set<String> newIds = allIds();
            newIds.removeAll(idsBefore);
            softly.assertThat(newIds).hasSize(idsBefore.size() * 2);
        });

        AdventureData first = mongoTemplate.findById(firstId, AdventureData.class);
        cascadeDeleteHelper.cascadeDelete(first);
        mongoTemplate.remove(first);
        AdventureData survivor = mongoTemplate.findById(original.getId(), AdventureData.class);
        assertThat(survivor).isNotNull();
        assertThat(survivor.getLocationData()).hasSize(2);
        assertThat(mongoTemplate.getCollection("locations").countDocuments()).isEqualTo(4);
    }

    @Test
    void aBrandNewEmptyAdventureCanBeExportedAndImported() {
        AdventureData empty = new AdventureData();
        empty.setTitle("Empty");
        mongoTemplate.save(empty);

        AdventureImporter.ImportResult result = importer.importAdventure(exporter.export(empty.getId()));

        AdventureData imported = mongoTemplate.findById(result.adventureId(), AdventureData.class);
        assertThat(imported).isNotNull();
        assertThat(imported.getTitle()).isEqualTo("Empty");
        assertThat(imported.getLocationData()).isEmpty();
        assertThat(imported.getId()).isNotEqualTo(empty.getId());
    }

    @Test
    void aFileFromAnotherBuilderVersionImportsButIsFlagged() {
        byte[] file = exporter.export(original.getId());

        assertThat(importerWithVersion("9.9.9").importAdventure(file).builderVersionDiffers()).isTrue();
    }

    @Test
    void aLeadingByteOrderMarkIsIgnored() {
        byte[] file = ("﻿" + new String(exporter.export(original.getId()), StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);

        assertThat(importer.importAdventure(file).adventureId()).isNotBlank();
    }

    @Test
    void anEmptyFileIsRejected() {
        assertThatThrownBy(() -> importer.importAdventure(new byte[0]))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("empty");
    }

    @Test
    void textThatIsNotJsonIsRejected() {
        assertThatThrownBy(() -> importer.importAdventure("this is not json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("not valid JSON");
    }

    @Test
    void jsonThatIsNotAnAdventureFileIsRejected() {
        byte[] file = editedExport(envelope -> envelope.put("format", "something-else"));

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("not an adventure file");
    }

    @Test
    void aFileFromANewerFormatVersionIsRejected() {
        byte[] file = editedExport(envelope -> envelope.put("formatVersion", 2));

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("version 2");
    }

    @Test
    void aFileThatIsTooLargeIsRejected() {
        AdventureImporter small = new AdventureImporter(mongoTemplate, BuilderVersion.of("1.2.3"), 10);

        assertThatThrownBy(() -> small.importAdventure(exporter.export(original.getId())))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("larger than");
    }

    @Test
    void aCollectionAdventuresDoNotUseIsRejectedAndNothingIsInserted() {
        byte[] file = editedExport(envelope -> envelope.get("documents", Document.class)
                                                       .put("savedgames", List.of(new Document("_id", "evil"))));
        Map<String, Long> countsBefore = counts();

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("savedgames");
        assertThat(counts()).isEqualTo(countsBefore);
        assertThat(mongoTemplate.getCollection("savedgames").countDocuments()).isZero();
    }

    @Test
    void aFileWithTwoAdventuresIsRejected() {
        byte[] file = editedExport(envelope -> {
            List<Object> adventures = new ArrayList<>(envelope.get("documents", Document.class)
                                                              .getList("adventures", Object.class));
            adventures.add(new Document("_id", "another-one").append("title", "x"));
            envelope.get("documents", Document.class).put("adventures", adventures);
        });

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("exactly one adventure");
    }

    @Test
    void aFileMissingADocumentItReferencesIsRejectedAndNothingIsInserted() {
        byte[] file = editedExport(envelope -> envelope.get("documents", Document.class).remove("locations"));
        Map<String, Long> countsBefore = counts();

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("does not contain");
        assertThat(counts()).isEqualTo(countsBefore);
    }

    @Test
    void aDocumentWithoutAnIdIsRejected() {
        byte[] file = editedExport(envelope -> envelope.get("documents", Document.class)
                                                       .getList("words", Document.class).getFirst().remove("_id"));

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("damaged");
    }

    private static Document firstLocationReference(Document anEnvelope) {
        Document adventure = anEnvelope.get("documents", Document.class)
                                       .getList("adventures", Document.class).getFirst();
        return adventure.get("locationData", Document.class).values().stream()
                        .map(Document.class::cast).findFirst().orElseThrow();
    }

    // A reference whose id is not a string can pass a toString() comparison and still be resolved by MongoDB
    // against documents the file does not contain - e.g. another author's.
    @Test
    void aReferenceWhoseIdIsNotAStringIsRejected() {
        byte[] file = editedExport(envelope -> {
            Document reference = firstLocationReference(envelope);
            reference.put("$id", new Symbol(reference.getString("$id")));
        });
        Map<String, Long> countsBefore = counts();

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("reference");
        assertThat(counts()).isEqualTo(countsBefore);
    }

    @Test
    void aReferenceThatNamesAnotherDatabaseIsRejected() {
        byte[] file = editedExport(envelope -> firstLocationReference(envelope).put("$db", "someone-elses"));

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("reference");
    }

    @Test
    void aFieldNameStartingWithADollarIsRejected() {
        byte[] file = editedExport(envelope -> envelope.get("documents", Document.class)
                                                       .getList("adventures", Document.class).getFirst()
                                                       .put("$where", "1"));

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("$where");
    }

    // The file is well-formed but the builder cannot map it (an unknown font): nothing may stay behind.
    @Test
    void aFileTheBuilderCannotReadIsRejectedAndNothingIsLeftBehind() {
        byte[] file = editedExport(envelope -> envelope.get("documents", Document.class)
                                                       .getList("adventures", Document.class).getFirst()
                                                       .put("font", "NO_SUCH_FONT"));
        Map<String, Long> countsBefore = counts();

        assertThatThrownBy(() -> importer.importAdventure(file))
                .isInstanceOf(AdventureImportException.class).hasMessageContaining("cannot read");
        assertThat(counts()).isEqualTo(countsBefore);
    }

    // A new @Document collection must be a deliberate decision: importable (add it to the allowlist) or excluded here.
    @Test
    void everyMongoCollectionIsEitherImportableOrDeliberatelyExcluded() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(
                new AnnotationTypeFilter(org.springframework.data.mongodb.core.mapping.Document.class));
        Set<String> collections = new HashSet<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.pdg.adventure")) {
            Class<?> type = Class.forName(definition.getBeanClassName());
            if (type.getProtectionDomain().getCodeSource().getLocation().getPath().contains("/test-classes/")) {
                continue; // test fixtures (e.g. TestStore) are not part of the model
            }
            String collection = type.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class)
                                    .collection();
            // without an explicit name Spring Data uses the uncapitalized class name
            collections.add(collection.isEmpty() ? StringUtils.uncapitalize(type.getSimpleName()) : collection);
        }

        Set<String> expected = new HashSet<>(AdventureImporter.ALLOWED_COLLECTIONS);
        expected.add("savedgames"); // saved games belong to a player's progress, not to the adventure
        assertThat(collections).isEqualTo(expected);
    }
}
