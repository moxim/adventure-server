# Adventure JSON Import / Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An author can export an adventure as one `.json` file and import it into another installation (another Mongo + MySQL), getting an independent copy owned by the importer.

**Architecture:** Export and import work on raw MongoDB documents, like `AdventureDuplicator`. The duplicator's graph logic (follow `@DBRef`s, rewrite ids, insert with rollback) is extracted into a package-private `AdventureDocumentGraph` that duplicate, export and import share. The file is a versioned envelope of canonical Extended JSON documents. Import validates the (untrusted) file completely, assigns new ULIDs and inserts all-or-nothing. `AdventureAccessService` adds the access rules and the MySQL author row; `AdventuresMenuView` gets an Export context-menu item and an Import dialog.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring Data MongoDB (raw `org.bson.Document`), Vaadin 25.2.6 (`Anchor` + `DownloadHandler`, `Upload` + `UploadHandler`), JUnit 5, AssertJ, Mockito, Vaadin browserless tests, embedded MongoDB (`@DataMongoTest`).

**Spec:** `server/docs/superpowers/specs/2026-10-09-adventure-json-import-export-design.md`

## Global Constraints

- All work is in the `server` module; do not touch `api` or `editor`. The git repo root is `server/` (run git, mvn from there).
- Maven needs `JAVA_HOME=/opt/homebrew/opt/openjdk@25`.
- Import always assigns new ids; an import never overwrites existing data.
- File format: `format` = `"adventurebuilder-adventure"`, `formatVersion` = `1`, canonical Extended JSON (`JsonMode.EXTENDED`), UTF-8.
- Import collection allowlist: `adventures, locations, containers, items, pictures, vocabularies, words` (every `@Document` collection except `savedgames`).
- Export requires write access (ADMIN or owning AUTHOR). Import requires role AUTHOR or ADMIN.
- Services return `Optional`/throw documented exceptions; no `null` returns. Tests use AssertJ (no JUnit `assertEquals`), Mockito for unit tests.
- Wherever ambiguity exists add `// TODO: Review needed — [reason]` (project rule). Do not add comments that merely restate code.
- Commit messages end with `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.

## Review Focus

Input classes/failure modes the spec implies; each has a test in the named task.

1. A file saved by an editor with a UTF-8 BOM, or not JSON at all (empty, a picture, plain text) — must produce a readable "not valid JSON"/"empty" message, not a stack trace. (Task 3)
2. A brand-new, empty adventure (no locations, no pictures, default vocabulary) — must export and import. (Task 3)
3. Importing the same file twice, or back into the DB it came from — two independent adventures, original untouched. (Task 3)
4. A hand-edited file that drops a document or adds a collection such as `savedgames` — rejected, and nothing is inserted. (Task 3)
5. Adventure titles with `/`, spaces, umlauts, quotes or nothing at all — must give a safe download filename. (Task 5)

## File Structure

| File | Responsibility |
|------|----------------|
| `src/main/java/com/pdg/adventure/server/storage/service/AdventureDocumentGraph.java` (new) | Package-private raw-document helper: collect reachable docs, copy with new ids, dangling-reference check, insert with rollback |
| `.../service/AdventureDuplicator.java` (modify) | Slimmed to use the helper; behaviour unchanged |
| `.../service/AdventureExporter.java` (new) | Adventure id → JSON bytes |
| `.../service/AdventureImporter.java` (new) | JSON bytes → validated, re-id'd, inserted adventure |
| `src/main/java/com/pdg/adventure/server/exception/AdventureImportException.java` (new) | User-presentable import failure |
| `.../security/service/AdventureAccessService.java` (modify) | `exportAdventure`, `importAdventure`, `getMaxImportBytes` |
| `src/main/java/com/pdg/adventure/view/adventure/AdventuresMenuView.java` (modify) | Export menu item, Import button + dialog |
| `src/test/.../server/testhelper/TestSupporter.java` (modify) | `createSampleAdventure` |
| `src/test/.../server/storage/AdventureExporterTest.java`, `AdventureImporterTest.java` (new) | Embedded-Mongo tests |
| `src/test/.../security/service/AdventureAccessServiceTransferTest.java` (new) | Access rules |
| `src/test/.../view/adventure/AdventuresMenuViewTest.java` (modify) | View tests |
| `CHANGELOG.md` (new, in `server/`) | Unreleased entry |

Test paths are under `server/src/test/java/com/pdg/adventure/`.

---

### Task 1: Extract `AdventureDocumentGraph` from `AdventureDuplicator`

**Files:**
- Create: `src/main/java/com/pdg/adventure/server/storage/service/AdventureDocumentGraph.java`
- Modify: `src/main/java/com/pdg/adventure/server/storage/service/AdventureDuplicator.java`
- Test (existing, must stay green): `src/test/java/com/pdg/adventure/server/storage/AdventureDuplicatorTest.java`

**Interfaces:**
- Produces (all package-private, in `com.pdg.adventure.server.storage.service`):
  - `final class AdventureDocumentGraph`
  - constants `static final String ADVENTURES = "adventures"`, `static final String ID = "_id"`
  - `AdventureDocumentGraph(MongoTemplate)`
  - `Document findAdventure(String anId)` — null if absent
  - `Map<String, List<Document>> collectReachableDocuments(Document anAdventure)` — adventure listed first under `"adventures"`, dangling refs skipped with a warning
  - `static Map<String, List<Document>> copyWithNewIds(Map<String, List<Document>> anOriginals)`
  - `static List<String> findDanglingReferences(Map<String, List<Document>> aDocuments)` — `"collection/id"` strings, distinct
  - `void insertAll(Map<String, List<Document>> aCopies)` — rolls back on failure
  - `static String newId()`

This is a pure refactor: the existing `AdventureDuplicatorTest` is the safety net, so no new test is written first.

- [ ] **Step 1: Run the existing duplicator tests as the baseline**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=AdventureDuplicatorTest`
Expected: PASS (all tests).

- [ ] **Step 2: Create `AdventureDocumentGraph`**

```java
package com.pdg.adventure.server.storage.service;

import com.github.f4b6a3.ulid.Ulid;
import com.mongodb.DBRef;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The raw-document operations that duplicating, exporting and importing an adventure share. Everything works on
 * {@link Document}s, not on the {@code *Data} classes - see {@link AdventureDuplicator} for why. Not a Spring bean:
 * each service creates one around its {@link MongoTemplate}.
 */
final class AdventureDocumentGraph {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureDocumentGraph.class);

    static final String ADVENTURES = "adventures";
    static final String ID = "_id";
    private static final String DBREF_COLLECTION = "$ref";
    private static final String DBREF_ID = "$id";

    private final MongoTemplate mongoTemplate;

    AdventureDocumentGraph(MongoTemplate aMongoTemplate) {
        mongoTemplate = aMongoTemplate;
    }

    /** @return the raw adventure document, or null if there is none with that id */
    Document findAdventure(String anId) {
        return mongoTemplate.getCollection(ADVENTURES).find(new Document(ID, anId)).first();
    }

    /** Follows every DBRef, transitively, starting at the adventure (which is listed first). */
    Map<String, List<Document>> collectReachableDocuments(Document anAdventure) {
        Map<String, List<Document>> result = new LinkedHashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        Deque<DBRef> pending = new ArrayDeque<>();

        result.computeIfAbsent(ADVENTURES, _ -> new ArrayList<>()).add(anAdventure);
        visited.put(key(ADVENTURES, anAdventure.get(ID)), true);
        findReferences(anAdventure, pending);

        while (!pending.isEmpty()) {
            DBRef reference = pending.poll();
            if (visited.putIfAbsent(key(reference.getCollectionName(), reference.getId()), true) != null) {
                continue;
            }
            Document target = mongoTemplate.getCollection(reference.getCollectionName())
                                           .find(new Document(ID, reference.getId())).first();
            if (target == null) {
                // TODO: Review needed — a dangling DBRef is copied as-is (pointing at its old id); alternative: fail the copy
                LOG.warn("Dangling reference {} while collecting an adventure", reference);
                continue;
            }
            result.computeIfAbsent(reference.getCollectionName(), _ -> new ArrayList<>()).add(target);
            findReferences(target, pending);
        }
        return result;
    }

    /**
     * Deep copies of all the documents, each with a new id. Every string value and map key that is an old id is
     * replaced by its new id, which rewires all references between the documents.
     */
    static Map<String, List<Document>> copyWithNewIds(Map<String, List<Document>> anOriginals) {
        Map<String, String> idMap = new HashMap<>();
        anOriginals.values().forEach(docs -> docs.forEach(doc -> idMap.put(doc.get(ID).toString(), newId())));

        Map<String, List<Document>> copies = new LinkedHashMap<>();
        anOriginals.forEach((collection, docs) -> {
            List<Document> copied = new ArrayList<>(docs.size());
            docs.forEach(doc -> copied.add((Document) rewrite(doc, idMap)));
            copies.put(collection, copied);
        });
        return copies;
    }

    /** The {@code "collection/id"} of every DBRef in the documents that points at a document not among them. */
    static List<String> findDanglingReferences(Map<String, List<Document>> aDocuments) {
        Set<String> present = new HashSet<>();
        aDocuments.forEach((collection, docs) -> docs.forEach(doc -> present.add(key(collection, doc.get(ID)))));

        Deque<DBRef> references = new ArrayDeque<>();
        aDocuments.values().forEach(docs -> docs.forEach(doc -> findReferences(doc, references)));
        return references.stream()
                         .map(reference -> key(reference.getCollectionName(), reference.getId()))
                         .filter(reference -> !present.contains(reference))
                         .distinct()
                         .toList();
    }

    /** MongoDB cannot span a transaction here (standalone), so undo by hand if a collection fails. */
    void insertAll(Map<String, List<Document>> aCopies) {
        try {
            aCopies.forEach((collection, docs) -> mongoTemplate.getCollection(collection).insertMany(docs));
        } catch (RuntimeException e) {
            // the new ids exist nowhere else, so removing by id cannot touch anything but the partial copy
            aCopies.forEach((collection, docs) -> mongoTemplate.getCollection(collection).deleteMany(
                    new Document(ID, new Document("$in", docs.stream().map(d -> d.get(ID)).toList()))));
            throw e;
        }
    }

    static String newId() {
        return Ulid.fast().toString().toLowerCase();
    }

    private static void findReferences(Object aNode, Deque<DBRef> aPending) {
        switch (aNode) {
            case DBRef reference -> aPending.add(reference);
            case Document document when document.containsKey(DBREF_COLLECTION) && document.containsKey(DBREF_ID) ->
                    aPending.add(new DBRef(document.getString(DBREF_COLLECTION), document.get(DBREF_ID)));
            case Map<?, ?> map -> map.values().forEach(value -> findReferences(value, aPending));
            case Iterable<?> iterable -> iterable.forEach(value -> findReferences(value, aPending));
            case null, default -> { /* a leaf value */ }
        }
    }

    /** Deep copy of the node with every string value and map key that is an old id replaced by its new id. */
    private static Object rewrite(Object aNode, Map<String, String> anIdMap) {
        return switch (aNode) {
            // TODO: Review needed — ids that are not strings (e.g. ObjectId) become string ids in the copy; none are created today
            case String text -> anIdMap.getOrDefault(text, text);
            case DBRef reference -> new DBRef(reference.getCollectionName(),
                                              anIdMap.getOrDefault(reference.getId().toString(), reference.getId().toString()));
            case Document document -> {
                Document copy = new Document();
                document.forEach((name, value) -> copy.put(anIdMap.getOrDefault(name, name), rewrite(value, anIdMap)));
                yield copy;
            }
            case List<?> list -> {
                List<Object> copy = new ArrayList<>(list.size());
                list.forEach(value -> copy.add(rewrite(value, anIdMap)));
                yield copy;
            }
            case Map<?, ?> map -> throw new IllegalStateException("Unexpected raw map in a BSON document: " + map);
            case null, default -> aNode; // numbers, dates, binary content, ...
        };
    }

    private static String key(String aCollection, Object anId) {
        return aCollection + "/" + anId;
    }
}
```

- [ ] **Step 3: Replace `AdventureDuplicator` with the slimmed version**

Overwrite the whole file (the public constructor and `duplicate` signature are unchanged, so `AdventureDuplicatorTest` and Spring wiring keep working):

```java
package com.pdg.adventure.server.storage.service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.pdg.adventure.server.storage.service.AdventureDocumentGraph.ADVENTURES;
import static com.pdg.adventure.server.storage.service.AdventureDocumentGraph.ID;

/**
 * Copies a complete adventure: the adventure document plus every document it reaches through a
 * {@code @DBRef} (locations, containers, items, pictures, vocabulary, words, ...).
 * <p>
 * The copy works on the raw documents, not on the {@code *Data} objects. Ids are fixed at
 * construction ({@code BasicData}) and {@code save} upserts by id, so a copy that kept any id
 * would silently overwrite the original's documents. Working on raw documents also copies
 * whatever the object model leaves out of cascade-save (e.g. pictures) or hides behind lazy
 * proxies, without knowing any of the classes.
 * <p>
 * Every document that gets copied receives a new id. All references between them are plain id
 * strings - {@code destinationId}, {@code currentLocationId}, {@code adventureId}, the keys of
 * the {@code locationData} map, the {@code $id} of each DBRef, ... - so one old-to-new id map,
 * applied to every string value and map key, rewires the whole copy. Documents embedded in a
 * copied document keep their ids: they are only meaningful inside their owner.
 * <p>
 * The graph walking itself lives in {@link AdventureDocumentGraph}, shared with export and import.
 */
@Service
public class AdventureDuplicator {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureDuplicator.class);

    private final AdventureDocumentGraph graph;

    public AdventureDuplicator(MongoTemplate aMongoTemplate) {
        graph = new AdventureDocumentGraph(aMongoTemplate);
    }

    /**
     * @return the id of the new adventure
     * @throws IllegalArgumentException if there is no adventure with the given id
     */
    public String duplicate(String aSourceAdventureId, String aNewTitle) {
        Document source = graph.findAdventure(aSourceAdventureId);
        if (source == null) {
            throw new IllegalArgumentException("Adventure not found: " + aSourceAdventureId);
        }

        Map<String, List<Document>> copies =
                AdventureDocumentGraph.copyWithNewIds(graph.collectReachableDocuments(source));

        Document copiedAdventure = copies.get(ADVENTURES).getFirst();
        copiedAdventure.put("title", aNewTitle);
        Date now = new Date();
        copiedAdventure.put("createdAt", now);
        copiedAdventure.put("updatedAt", now);

        graph.insertAll(copies);
        String copyId = copiedAdventure.get(ID).toString();
        LOG.info("Duplicated adventure {} as {} ({} documents)", aSourceAdventureId, copyId,
                 copies.values().stream().mapToInt(List::size).sum());
        return copyId;
    }
}
```

- [ ] **Step 4: Run the duplicator tests again**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=AdventureDuplicatorTest`
Expected: PASS, same tests as Step 1.

- [ ] **Step 5: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/server/storage/service/AdventureDocumentGraph.java src/main/java/com/pdg/adventure/server/storage/service/AdventureDuplicator.java
git commit -m "$(cat <<'EOF'
refactor: extract AdventureDocumentGraph from AdventureDuplicator

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: `AdventureExporter` and the shared sample-adventure fixture

**Files:**
- Create: `src/main/java/com/pdg/adventure/server/storage/service/AdventureExporter.java`
- Modify: `src/test/java/com/pdg/adventure/server/testhelper/TestSupporter.java` (add imports + `createSampleAdventure`)
- Test: `src/test/java/com/pdg/adventure/server/storage/AdventureExporterTest.java`

**Interfaces:**
- Consumes: `AdventureDocumentGraph` (Task 1); `BuilderVersion.current()` / `BuilderVersion.of(String)` (existing).
- Produces:
  - `AdventureExporter(MongoTemplate, BuilderVersion)` (Spring `@Service`)
  - `public byte[] export(String anAdventureId)` — throws `IllegalArgumentException` (unknown id), `IllegalStateException` (dangling DBRef)
  - `public static final String FORMAT = "adventurebuilder-adventure"`, `public static final int FORMAT_VERSION = 1`
  - `TestSupporter.createSampleAdventure(String aTitle)` → `AdventureData` (not persisted; its picture(s) must be saved before the adventure)

- [ ] **Step 1: Add the fixture to `TestSupporter`**

Add these imports next to the existing ones:

```java
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.PictureData;
```

Add this method before the closing `}` of the class:

```java
    /**
     * An adventure with two locations (cellar -> attic), a lamp in the cellar, a key in the pocket, a picture on the
     * cellar, a "take"/"grab" vocabulary, a message and the variable score=3. Not persisted: save the picture(s)
     * first, then the adventure. Persisted it spans 1 adventure, 2 locations, 3 containers, 2 items, 1 picture,
     * 1 vocabulary and 2 words.
     */
    public static AdventureData createSampleAdventure(String aTitle) {
        AdventureData adventure = new AdventureData();
        adventure.setTitle(aTitle);
        adventure.setFont(AdventureFont.MEDIEVAL_SHARP);

        Word take = adventure.getVocabularyData().createWord("take", Word.Type.VERB);
        adventure.getVocabularyData().createSynonym("grab", take);

        LocationData cellar = new LocationData();
        LocationData attic = new LocationData();
        ItemData lamp = new ItemData();
        lamp.setAdventureId(adventure.getId());
        lamp.setLocationId(cellar.getId());
        lamp.setParentContainerId(cellar.getItemContainerData().getId());
        cellar.getItemContainerData().getItems().add(lamp);

        DirectionData up = new DirectionData();
        up.setDestinationId(attic.getId());
        cellar.getDirectionsData().add(up);

        PictureData picture = new PictureData();
        picture.setAdventureId(adventure.getId());
        picture.setName("cellar.png");
        picture.setContent(new byte[] {1, 2, 3});
        picture.setContentType("image/png");
        cellar.setPictureId(picture.getId());
        adventure.getPictureData().put(picture.getId(), picture);

        adventure.getLocationData().put(cellar.getId(), cellar);
        adventure.getLocationData().put(attic.getId(), attic);
        adventure.setCurrentLocationId(cellar.getId());

        ItemData key = new ItemData();
        key.setAdventureId(adventure.getId());
        adventure.getPlayerPocket().getItems().add(key);

        MessageData message = new MessageData("greeting", "hello");
        adventure.getMessages().put(message.getId(), message);

        adventure.getVariableData().addVariable("score", 3);
        return adventure;
    }
```

- [ ] **Step 2: Write the failing exporter test**

```java
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
```

Note: if `adventure.getDate("createdAt")` is null because `DatedData` stamps differently, check `DatedData` and assert on the field it actually uses (the duplicator writes `createdAt`/`updatedAt`).

- [ ] **Step 3: Run it to verify it fails**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=AdventureExporterTest`
Expected: COMPILATION FAILURE — `AdventureExporter` does not exist.

- [ ] **Step 4: Implement `AdventureExporter`**

```java
package com.pdg.adventure.server.storage.service;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Writes an adventure - the adventure document plus every document it reaches through a {@code @DBRef} - as one
 * JSON file that {@link AdventureImporter} can read into another database.
 * <p>
 * The file is an envelope around canonical Extended JSON documents, so dates, picture bytes and DBRefs keep their
 * types. Like {@link AdventureDuplicator} it works on the raw documents and therefore needs no knowledge of the
 * {@code *Data} classes. Bump {@link #FORMAT_VERSION} when the envelope itself changes incompatibly.
 */
@Service
public class AdventureExporter {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureExporter.class);

    public static final String FORMAT = "adventurebuilder-adventure";
    public static final int FORMAT_VERSION = 1;

    private static final JsonWriterSettings JSON_SETTINGS =
            JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).indent(true).build();

    private final MongoTemplate mongoTemplate;
    private final AdventureDocumentGraph graph;
    private final BuilderVersion builderVersion;

    public AdventureExporter(MongoTemplate aMongoTemplate, BuilderVersion aBuilderVersion) {
        mongoTemplate = aMongoTemplate;
        graph = new AdventureDocumentGraph(aMongoTemplate);
        builderVersion = aBuilderVersion;
    }

    /**
     * @return the UTF-8 JSON file content
     * @throws IllegalArgumentException if there is no adventure with the given id
     * @throws IllegalStateException    if the adventure refers to documents that no longer exist (the file could
     *                                  not be imported again)
     */
    public byte[] export(String anAdventureId) {
        Document source = graph.findAdventure(anAdventureId);
        if (source == null) {
            throw new IllegalArgumentException("Adventure not found: " + anAdventureId);
        }

        Map<String, List<Document>> reachable = graph.collectReachableDocuments(source);
        List<String> dangling = AdventureDocumentGraph.findDanglingReferences(reachable);
        if (!dangling.isEmpty()) {
            // TODO: Review needed — refusing keeps an un-importable file from being written; alternative: export anyway and let the import reject it
            throw new IllegalStateException("Adventure " + anAdventureId
                                            + " refers to documents that no longer exist: " + dangling);
        }

        Document documents = new Document();
        reachable.forEach(documents::put);
        Document envelope = new Document("format", FORMAT)
                .append("formatVersion", FORMAT_VERSION)
                .append("builderVersion", builderVersion.current().orElse(null))
                .append("exportedAt", Instant.now().toString())
                .append("documents", documents);

        // the database's registry knows how to write a DBRef; Document's built-in default does not
        Codec<Document> codec = mongoTemplate.getDb().getCodecRegistry().get(Document.class);
        byte[] json = envelope.toJson(JSON_SETTINGS, codec).getBytes(StandardCharsets.UTF_8);
        LOG.info("Exported adventure {} ({} documents, {} bytes)", anAdventureId,
                 reachable.values().stream().mapToInt(List::size).sum(), json.length);
        return json;
    }
}
```

- [ ] **Step 5: Run the exporter tests**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=AdventureExporterTest`
Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/server/storage/service/AdventureExporter.java src/test/java/com/pdg/adventure/server/storage/AdventureExporterTest.java src/test/java/com/pdg/adventure/server/testhelper/TestSupporter.java
git commit -m "$(cat <<'EOF'
feat: export an adventure as a versioned Extended JSON file

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: `AdventureImporter` with validation

**Files:**
- Create: `src/main/java/com/pdg/adventure/server/exception/AdventureImportException.java`
- Create: `src/main/java/com/pdg/adventure/server/storage/service/AdventureImporter.java`
- Test: `src/test/java/com/pdg/adventure/server/storage/AdventureImporterTest.java`

**Interfaces:**
- Consumes: `AdventureDocumentGraph` (Task 1), `AdventureExporter.FORMAT` / `FORMAT_VERSION` / `export` (Task 2), `TestSupporter.createSampleAdventure` (Task 2).
- Produces:
  - `public class AdventureImportException extends RuntimeException` with `(String)` and `(String, Throwable)` constructors; messages are meant to be shown to the author
  - `AdventureImporter(MongoTemplate, BuilderVersion, long aMaxBytes)` (Spring `@Service`; `aMaxBytes` from property `adventure.import.max-bytes`, default 50 MB)
  - `public ImportResult importAdventure(byte[] aJson)` — throws `AdventureImportException`
  - `public record ImportResult(String adventureId, boolean builderVersionDiffers)`
  - `public long getMaxBytes()`
  - `public static final Set<String> ALLOWED_COLLECTIONS`, `public static final long DEFAULT_MAX_BYTES`

- [ ] **Step 1: Write the failing test**

```java
package com.pdg.adventure.server.storage;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
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
        Map<String, Long> counts = new java.util.LinkedHashMap<>();
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

    // A new @Document collection must be a deliberate decision: importable (add it to the allowlist) or excluded here.
    @Test
    void everyMongoCollectionIsEitherImportableOrDeliberatelyExcluded() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(
                new AnnotationTypeFilter(org.springframework.data.mongodb.core.mapping.Document.class));
        Set<String> collections = new HashSet<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.pdg.adventure")) {
            Class<?> type = Class.forName(definition.getBeanClassName());
            collections.add(type.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class)
                                .collection());
        }

        Set<String> expected = new HashSet<>(AdventureImporter.ALLOWED_COLLECTIONS);
        expected.add("savedgames"); // saved games belong to a player's progress, not to the adventure
        assertThat(collections).isEqualTo(expected);
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=AdventureImporterTest`
Expected: COMPILATION FAILURE — `AdventureImporter` and `AdventureImportException` do not exist.

- [ ] **Step 3: Create `AdventureImportException`**

```java
package com.pdg.adventure.server.exception;

/** An adventure file could not be imported. The message is written for the author and may be shown as is. */
public class AdventureImportException extends RuntimeException {
    public AdventureImportException(String aMessage) {
        super(aMessage);
    }

    public AdventureImportException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }
}
```

- [ ] **Step 4: Implement `AdventureImporter`**

```java
package com.pdg.adventure.server.storage.service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pdg.adventure.server.exception.AdventureImportException;

/**
 * Reads a file written by {@link AdventureExporter} into this database as a new, independent adventure.
 * <p>
 * The file is untrusted input. Everything is checked before the first insert - format and version, the collections
 * (an allowlist, so a crafted file cannot write elsewhere), exactly one adventure, an id on every document and
 * no reference to a document the file lacks. Then every document gets a new id (see {@link AdventureDuplicator}),
 * so an import can never overwrite anything and importing a file twice gives two adventures.
 */
@Service
public class AdventureImporter {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureImporter.class);

    /** Every {@code @Document} collection an adventure reaches. Deliberately without saved games. */
    public static final Set<String> ALLOWED_COLLECTIONS = Set.of(
            "adventures", "locations", "containers", "items", "pictures", "vocabularies", "words");

    public static final long DEFAULT_MAX_BYTES = 50L * 1024 * 1024;

    private static final long MEGABYTE = 1024L * 1024;
    private static final String NOT_AN_ADVENTURE_FILE = "This is not an adventure file.";

    /** @param builderVersionDiffers the file was written by another version of the builder than this one */
    public record ImportResult(String adventureId, boolean builderVersionDiffers) {
    }

    private final AdventureDocumentGraph graph;
    private final BuilderVersion builderVersion;
    private final long maxBytes;

    public AdventureImporter(MongoTemplate aMongoTemplate, BuilderVersion aBuilderVersion,
                             @Value("${adventure.import.max-bytes:" + DEFAULT_MAX_BYTES + "}") long aMaxBytes) {
        graph = new AdventureDocumentGraph(aMongoTemplate);
        builderVersion = aBuilderVersion;
        maxBytes = aMaxBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    /**
     * @return the id of the new adventure
     * @throws AdventureImportException if the file is not a complete adventure file this builder can read
     */
    public ImportResult importAdventure(byte[] aJson) {
        if (aJson == null || aJson.length == 0) {
            throw new AdventureImportException("The file is empty.");
        }
        if (aJson.length > maxBytes) {
            throw new AdventureImportException("The file is larger than the allowed " + describe(maxBytes) + ".");
        }

        Document envelope = parse(aJson);
        checkHeader(envelope);
        Map<String, List<Document>> documents = readDocuments(envelope);

        List<String> dangling = AdventureDocumentGraph.findDanglingReferences(documents);
        if (!dangling.isEmpty()) {
            throw new AdventureImportException("The file is incomplete: it refers to documents it does not contain ("
                                               + String.join(", ", dangling.stream().limit(5).toList()) + ").");
        }

        String fileVersion = envelope.getString("builderVersion");
        boolean differs = fileVersion != null && builderVersion.current().isPresent()
                          && !fileVersion.equals(builderVersion.current().get());

        Map<String, List<Document>> copies = AdventureDocumentGraph.copyWithNewIds(documents);
        Document adventure = copies.get(AdventureDocumentGraph.ADVENTURES).getFirst();
        Date now = new Date();
        adventure.put("createdAt", now);
        adventure.put("updatedAt", now);

        graph.insertAll(copies);
        String adventureId = adventure.get(AdventureDocumentGraph.ID).toString();
        LOG.info("Imported adventure '{}' as {} ({} documents)", adventure.get("title"), adventureId,
                 copies.values().stream().mapToInt(List::size).sum());
        return new ImportResult(adventureId, differs);
    }

    private static Document parse(byte[] aJson) {
        String text = new String(aJson, StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) {
            text = text.substring(1); // a byte order mark, as some editors write it
        }
        try {
            return Document.parse(text);
        } catch (RuntimeException e) {
            throw new AdventureImportException("The file is not valid JSON.", e);
        }
    }

    private static void checkHeader(Document anEnvelope) {
        if (!AdventureExporter.FORMAT.equals(anEnvelope.get("format"))
            || !(anEnvelope.get("formatVersion") instanceof Number version)) {
            throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
        }
        if (version.intValue() < 1) {
            throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
        }
        if (version.intValue() > AdventureExporter.FORMAT_VERSION) {
            throw new AdventureImportException("The file uses format version " + version.intValue()
                                               + ", but this builder reads up to version "
                                               + AdventureExporter.FORMAT_VERSION + ". Please update the builder.");
        }
    }

    private static Map<String, List<Document>> readDocuments(Document anEnvelope) {
        if (!(anEnvelope.get("documents") instanceof Document raw)) {
            throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
        }
        Map<String, List<Document>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            String collection = entry.getKey();
            if (!ALLOWED_COLLECTIONS.contains(collection)) {
                throw new AdventureImportException("The file contains data for '" + collection
                                                   + "', which adventures do not use.");
            }
            if (!(entry.getValue() instanceof List<?> list)) {
                throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
            }
            Set<String> ids = new HashSet<>();
            List<Document> docs = new ArrayList<>(list.size());
            for (Object element : list) {
                if (!(element instanceof Document doc) || !(doc.get(AdventureDocumentGraph.ID) instanceof String id)
                    || id.isBlank() || !ids.add(id)) {
                    throw new AdventureImportException("The file contains a damaged or duplicated document in '"
                                                       + collection + "'.");
                }
                docs.add(doc);
            }
            result.put(collection, docs);
        }
        if (result.getOrDefault(AdventureDocumentGraph.ADVENTURES, List.of()).size() != 1) {
            throw new AdventureImportException("The file must contain exactly one adventure.");
        }
        return result;
    }

    private static String describe(long aByteCount) {
        return aByteCount >= MEGABYTE ? aByteCount / MEGABYTE + " MB" : aByteCount + " bytes";
    }
}
```

- [ ] **Step 5: Run the importer tests**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest='AdventureImporterTest,AdventureExporterTest,AdventureDuplicatorTest'`
Expected: PASS. If `exportThenImportIntoAnEmptyDatabaseRebuildsTheAdventure` fails on a type mismatch in the loaded graph (e.g. an `Int32` read back as a different number type), inspect the failing field before changing anything: the spec requires typed round-tripping, so fix the cause rather than the assertion.

- [ ] **Step 6: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/server/exception/AdventureImportException.java src/main/java/com/pdg/adventure/server/storage/service/AdventureImporter.java src/test/java/com/pdg/adventure/server/storage/AdventureImporterTest.java
git commit -m "$(cat <<'EOF'
feat: import an adventure from a validated JSON file under new ids

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 4: Access rules in `AdventureAccessService`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/security/service/AdventureAccessService.java`
- Modify: `src/test/java/com/pdg/adventure/server/security/service/AdventureAccessServiceDuplicateTest.java` (constructor call, lines 46-47)
- Test: `src/test/java/com/pdg/adventure/server/security/service/AdventureAccessServiceTransferTest.java`

**Interfaces:**
- Consumes: `AdventureExporter.export(String)` (Task 2), `AdventureImporter.importAdventure(byte[])` / `ImportResult` / `getMaxBytes()` (Task 3).
- Produces (on `AdventureAccessService`):
  - constructor `(AdventureService, AdventureDuplicator, AdventureExporter, AdventureImporter, AdventureAuthorRepository, AdventurePlayerRepository)`
  - `public byte[] exportAdventure(String adventureId, UserData user)` — `AccessDeniedException` without write access
  - `public ImportedAdventure importAdventure(byte[] json, UserData user)` — `AccessDeniedException` unless AUTHOR/ADMIN; may throw `AdventureImportException`
  - `public record ImportedAdventure(AdventureData adventure, boolean builderVersionDiffers)`
  - `public long getMaxImportBytes()`

- [ ] **Step 1: Write the failing test**

```java
package com.pdg.adventure.server.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.AdventureAuthor;
import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.exception.AdventureImportException;
import com.pdg.adventure.server.security.repository.AdventureAuthorRepository;
import com.pdg.adventure.server.security.repository.AdventurePlayerRepository;
import com.pdg.adventure.server.storage.service.AdventureDuplicator;
import com.pdg.adventure.server.storage.service.AdventureExporter;
import com.pdg.adventure.server.storage.service.AdventureImporter;
import com.pdg.adventure.server.storage.service.AdventureService;

class AdventureAccessServiceTransferTest {

    private static final byte[] FILE = {1, 2, 3};

    private AdventureService adventureService;
    private AdventureExporter exporter;
    private AdventureImporter importer;
    private AdventureAuthorRepository authorRepository;
    private AdventureAccessService accessService;

    private UserData author;
    private AdventureData imported;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        exporter = mock(AdventureExporter.class);
        importer = mock(AdventureImporter.class);
        authorRepository = mock(AdventureAuthorRepository.class);
        accessService = new AdventureAccessService(adventureService, mock(AdventureDuplicator.class), exporter,
                                                   importer, authorRepository, mock(AdventurePlayerRepository.class));

        author = user("anna-id", Role.AUTHOR);
        imported = new AdventureData();
        imported.setId("new-id");
        when(adventureService.findAdventureById("new-id")).thenReturn(Optional.of(imported));
        when(importer.importAdventure(FILE)).thenReturn(new AdventureImporter.ImportResult("new-id", true));
        when(exporter.export("src")).thenReturn(FILE);
    }

    private void authorOwnsSource() {
        when(authorRepository.findByAdventureId("src")).thenReturn(Optional.of(new AdventureAuthor("src", author)));
    }

    @Test
    void export_returnsTheFileForTheOwningAuthor() {
        authorOwnsSource();

        assertThat(accessService.exportAdventure("src", author)).isEqualTo(FILE);
    }

    @Test
    void export_isAllowedForAnAdmin() {
        assertThat(accessService.exportAdventure("src", user("admin-id", Role.ADMIN))).isEqualTo(FILE);
    }

    @Test
    void export_isRefusedForAnAuthorWhoDoesNotOwnTheAdventure() {
        when(authorRepository.findByAdventureId("src"))
                .thenReturn(Optional.of(new AdventureAuthor("src", user("bob-id", Role.AUTHOR))));

        assertThatThrownBy(() -> accessService.exportAdventure("src", author))
                .isInstanceOf(AccessDeniedException.class);
        verify(exporter, never()).export(any());
    }

    @Test
    void import_makesTheImportingUserTheAuthorAndReportsAVersionMismatch() {
        AdventureAccessService.ImportedAdventure result = accessService.importAdventure(FILE, author);

        ArgumentCaptor<AdventureAuthor> saved = ArgumentCaptor.forClass(AdventureAuthor.class);
        verify(authorRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getAdventureId()).isEqualTo("new-id");
        assertThat(saved.getValue().getUser()).isSameAs(author);
        assertThat(result.adventure()).isSameAs(imported);
        assertThat(result.builderVersionDiffers()).isTrue();
    }

    @Test
    void import_isAllowedForAnAdminWithoutTheAuthorRole() {
        AdventureAccessService.ImportedAdventure result = accessService.importAdventure(FILE, user("admin-id", Role.ADMIN));

        assertThat(result.adventure()).isSameAs(imported);
    }

    @Test
    void import_isRefusedForAPlayer() {
        assertThatThrownBy(() -> accessService.importAdventure(FILE, user("pat-id", Role.PLAYER)))
                .isInstanceOf(AccessDeniedException.class);
        verify(importer, never()).importAdventure(any());
    }

    @Test
    void import_savesNoAuthorRowWhenTheFileIsRejected() {
        when(importer.importAdventure(FILE)).thenThrow(new AdventureImportException("This is not an adventure file."));

        assertThatThrownBy(() -> accessService.importAdventure(FILE, author))
                .isInstanceOf(AdventureImportException.class);
        verify(authorRepository, never()).saveAndFlush(any());
    }

    @Test
    void import_removesTheImportedDocumentsAgainWhenTheAuthorAssignmentFails() {
        doThrow(new IllegalStateException("mysql down")).when(authorRepository).saveAndFlush(any());

        assertThatThrownBy(() -> accessService.importAdventure(FILE, author))
                .isInstanceOf(IllegalStateException.class);

        verify(adventureService).deleteAdventure("new-id");
    }

    @Test
    void getMaxImportBytes_asksTheImporter() {
        when(importer.getMaxBytes()).thenReturn(42L);

        assertThat(accessService.getMaxImportBytes()).isEqualTo(42L);
    }

    // UserData has no id setter (ids are generated on persist), so stub the two getters the access checks read
    private static UserData user(String id, Role role) {
        UserData user = mock(UserData.class);
        when(user.getId()).thenReturn(id);
        when(user.getRoles()).thenReturn(Set.of(role));
        return user;
    }
}
```

- [ ] **Step 2: Update the existing duplicate test's constructor call**

In `AdventureAccessServiceDuplicateTest.setUp`, replace

```java
        accessService = new AdventureAccessService(adventureService, adventureDuplicator, authorRepository,
                                                   mock(AdventurePlayerRepository.class));
```

with

```java
        accessService = new AdventureAccessService(adventureService, adventureDuplicator,
                                                   mock(AdventureExporter.class), mock(AdventureImporter.class),
                                                   authorRepository, mock(AdventurePlayerRepository.class));
```

and add `import com.pdg.adventure.server.storage.service.AdventureExporter;` and `import com.pdg.adventure.server.storage.service.AdventureImporter;`.

- [ ] **Step 3: Run to verify failure**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest='AdventureAccessService*Test'`
Expected: COMPILATION FAILURE — constructor and methods missing.

- [ ] **Step 4: Implement in `AdventureAccessService`**

Add imports:

```java
import com.pdg.adventure.server.storage.service.AdventureExporter;
import com.pdg.adventure.server.storage.service.AdventureImporter;
```

Replace the fields and constructor:

```java
    private final AdventureService adventureService;
    private final AdventureDuplicator adventureDuplicator;
    private final AdventureExporter adventureExporter;
    private final AdventureImporter adventureImporter;
    private final AdventureAuthorRepository authorRepository;
    private final AdventurePlayerRepository playerRepository;

    public AdventureAccessService(AdventureService adventureService,
                                  AdventureDuplicator adventureDuplicator,
                                  AdventureExporter adventureExporter,
                                  AdventureImporter adventureImporter,
                                  AdventureAuthorRepository authorRepository,
                                  AdventurePlayerRepository playerRepository) {
        this.adventureService = adventureService;
        this.adventureDuplicator = adventureDuplicator;
        this.adventureExporter = adventureExporter;
        this.adventureImporter = adventureImporter;
        this.authorRepository = authorRepository;
        this.playerRepository = playerRepository;
    }
```

Add after `duplicateAdventure` (before `deleteAdventure`):

```java
    /** An adventure read from a file, and whether the file came from another version of the builder. */
    public record ImportedAdventure(AdventureData adventure, boolean builderVersionDiffers) {
    }

    /**
     * The adventure as a JSON file (see {@link AdventureExporter}). Requires write access: ADMIN or AUTHOR ownership.
     * <p>
     * TODO: Review needed — requires write access (not just read), so an assigned PLAYER cannot export an
     * adventure's content; alternative: allow anyone who can read it.
     */
    public byte[] exportAdventure(String adventureId, UserData user) {
        if (!canWrite(adventureId, user)) {
            throw new AccessDeniedException("No write access to adventure: " + adventureId);
        }
        return adventureExporter.export(adventureId);
    }

    /**
     * Reads an adventure file into this database as a new adventure (see {@link AdventureImporter}) and makes the
     * given user its AUTHOR. Requires the AUTHOR or ADMIN role.
     * <p>
     * TODO: Review needed — the title is kept as is; alternative: add a suffix when the user already has an
     * adventure with that title.
     *
     * @throws com.pdg.adventure.server.exception.AdventureImportException if the file cannot be imported
     */
    @Transactional
    public ImportedAdventure importAdventure(byte[] json, UserData user) {
        if (!user.getRoles().contains(Role.AUTHOR) && !user.getRoles().contains(Role.ADMIN)) {
            throw new AccessDeniedException("Only authors can import adventures");
        }
        AdventureImporter.ImportResult result = adventureImporter.importAdventure(json);
        try {
            // flush so a failing INSERT surfaces here, not at commit time after this catch
            authorRepository.saveAndFlush(new AdventureAuthor(result.adventureId(), user));
        } catch (RuntimeException e) {
            // MongoDB and MySQL cannot share a transaction: don't leave an author-less adventure behind
            adventureService.deleteAdventure(result.adventureId());
            throw e;
        }
        AdventureData adventure = adventureService.findAdventureById(result.adventureId()).orElseThrow(
                () -> new IllegalStateException("Imported adventure vanished: " + result.adventureId()));
        return new ImportedAdventure(adventure, result.builderVersionDiffers());
    }

    /** The largest adventure file {@link #importAdventure} accepts, in bytes. */
    public long getMaxImportBytes() {
        return adventureImporter.getMaxBytes();
    }
```

- [ ] **Step 5: Run the access tests**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest='AdventureAccessService*Test'`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/server/security/service/AdventureAccessService.java src/test/java/com/pdg/adventure/server/security/service/
git commit -m "$(cat <<'EOF'
feat: access rules for adventure export and import

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 5: Export item and Import dialog in `AdventuresMenuView`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/adventure/AdventuresMenuView.java`
- Test: `src/test/java/com/pdg/adventure/view/adventure/AdventuresMenuViewTest.java` (add tests)

**Interfaces:**
- Consumes: `AdventureAccessService.exportAdventure`, `importAdventure`, `getMaxImportBytes`, `ImportedAdventure` (Task 4); `AdventureImportException` (Task 3).
- Produces (package-private, for tests, like `duplicateAdventure`): `void exportAdventure(AdventureData)`, `boolean importAdventure(byte[])`, `Dialog buildImportDialog()`, `static String exportFileName(String aTitle)`.

Vaadin APIs used (verified against Vaadin 25.2 docs): `Anchor.setHref(DownloadHandler)`, `DownloadHandler.fromInputStream(callback)` with `new DownloadResponse(InputStream, fileName, contentType, contentLength)`, `Upload(UploadHandler)`, `UploadHandler.inMemory((metadata, data) -> ...)`, `Upload.setMaxFiles`, `setAcceptedFileExtensions`, `setMaxFileSize(int)`, `addAllFinishedListener`, `addFileRemovedListener`. The upload callback runs on a request thread without the UI lock, so it only stores the bytes in an `AtomicReference`; the UI-thread listeners enable the Import button. If the callback's parameter types differ from these, check with `get_java_symbol` for `InputStreamDownloadCallback` / `InMemoryUploadCallback`.

- [ ] **Step 1: Write the failing tests**

Add to `AdventuresMenuViewTest` (imports: `com.vaadin.flow.component.dialog.Dialog`, `com.vaadin.flow.component.html.Anchor`, `com.vaadin.flow.component.upload.Upload`, `com.pdg.adventure.server.exception.AdventureImportException`, `static org.mockito.ArgumentMatchers.any/eq` already present):

```java
    @Test
    void importAdventureButton_isOnTheView() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        assertThat(find(Button.class, view).withText("Import Adventure").single().isEnabled()).isTrue();
    }

    @Test
    void importDialog_offersASingleJsonFileUploadAndStartsWithImportDisabled() {
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        Dialog dialog = view.buildImportDialog();
        UI.getCurrent().add(dialog);
        dialog.open();

        Upload upload = find(Upload.class, dialog).single();
        assertThat(upload.getMaxFiles()).isEqualTo(1);
        assertThat(upload.getAcceptedFileExtensions()).containsExactly(".json");
        assertThat(find(Button.class, dialog).withText("Import").single().isEnabled()).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void importAdventure_addsTheImportedAdventureToTheGridAndPassesTheFileOn() {
        AdventureData imported = new AdventureData();
        imported.setId("adv-9");
        imported.setTitle("From Elsewhere");
        byte[] file = {1, 2, 3};
        when(accessService.importAdventure(eq(file), any(UserData.class)))
                .thenReturn(new AdventureAccessService.ImportedAdventure(imported, false));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        boolean imports = view.importAdventure(file);

        assertThat(imports).isTrue();
        assertThat(grid.getListDataView().getItems()).extracting(AdventureData::getId)
                                                      .containsExactly("adv-1", "adv-9");
    }

    @SuppressWarnings("unchecked")
    @Test
    void importAdventure_whenTheFileIsRejected_leavesTheGridAsItWasAndDoesNotThrow() {
        when(accessService.importAdventure(any(), any(UserData.class)))
                .thenThrow(new AdventureImportException("This is not an adventure file."));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);
        Grid<AdventureData> grid = (Grid<AdventureData>) find(Grid.class, view).single();

        boolean imports = view.importAdventure(new byte[] {1});

        assertThat(imports).isFalse();
        assertThat(grid.getListDataView().getItems()).extracting(AdventureData::getId).containsExactly("adv-1");
    }

    @Test
    void importAdventure_whenSomethingUnexpectedFails_returnsFalseInsteadOfThrowing() {
        when(accessService.importAdventure(any(), any(UserData.class))).thenThrow(new IllegalStateException("boom"));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        assertThat(view.importAdventure(new byte[] {1})).isFalse();
    }

    @Test
    void export_asksTheAccessServiceForTheFileAndPointsTheDownloadLinkAtIt() {
        when(accessService.exportAdventure(eq("adv-1"), any(UserData.class))).thenReturn(new byte[] {1, 2, 3});
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        view.exportAdventure(adventure);

        verify(accessService).exportAdventure(eq("adv-1"), any(UserData.class));
        assertThat(find(Anchor.class, view).single().getHref()).isNotBlank();
    }

    @Test
    void export_whenItIsRefused_offersNoDownloadAndDoesNotThrow() {
        when(accessService.exportAdventure(any(), any(UserData.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("no"));
        AdventuresMenuView view = new AdventuresMenuView(accessService);
        UI.getCurrent().add(view);

        view.exportAdventure(adventure);

        assertThat(find(Anchor.class, view).single().getHref()).isNullOrEmpty();
    }

    @Test
    void exportFileName_turnsATitleIntoASafeFileName() {
        assertThat(AdventuresMenuView.exportFileName("The Demo")).isEqualTo("The-Demo.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("  ../etc/passwd  ")).isEqualTo("etc-passwd.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("Über \"Größe\": 1/2")).isEqualTo("Über-Größe-1-2.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("")).isEqualTo("adventure.adventure.json");
        assertThat(AdventuresMenuView.exportFileName(null)).isEqualTo("adventure.adventure.json");
        assertThat(AdventuresMenuView.exportFileName("///")).isEqualTo("adventure.adventure.json");
    }
```

- [ ] **Step 2: Run to verify failure**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=AdventuresMenuViewTest`
Expected: COMPILATION FAILURE — new view members missing.

- [ ] **Step 3: Implement in `AdventuresMenuView`**

Add imports:

```java
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicReference;

import com.pdg.adventure.server.exception.AdventureImportException;
```

Add fields next to the existing ones:

```java
    private static final String EXPORT_FILE_SUFFIX = ".adventure.json";

    private final Anchor exportLink = new Anchor();
    private Grid<AdventureData> adventureGrid;
```

In the constructor, after `runAdventure` is created, create the import button and add both it and the hidden link:

```java
        Button importAdventure = new Button("Import Adventure", _ -> buildImportDialog().open());

        VerticalLayout leftSide = new VerticalLayout(create, importAdventure, runAdventure);

        // One hidden link serves every export: exportAdventure points it at the file and clicks it
        exportLink.getStyle().set("display", "none");
        exportLink.setRouterIgnore(true);
        leftSide.add(exportLink);
```

(Replace the existing `VerticalLayout leftSide = new VerticalLayout(create, runAdventure);` line.)

In `getGridContainer`, assign the grid to the field right after creating it: `adventureGrid = grid;`.

In `AdventureDataContextMenu`, add the Export item after Duplicate:

```java
            addItem("Export", e -> e.getItem().ifPresent(AdventuresMenuView.this::exportAdventure));
```

Add the members (next to `duplicateAdventure`):

```java
    /**
     * Package-private for testing, like {@link #duplicateAdventure}. Fetches the file on the UI thread, so a refusal is
     * shown as a notification, then points the hidden link at it and clicks it for the browser download.
     */
    void exportAdventure(AdventureData anAdventure) {
        try {
            byte[] json = accessService.exportAdventure(anAdventure.getId(), ViewSupporter.getCurrentUser());
            String fileName = exportFileName(anAdventure.getTitle());
            exportLink.setHref(DownloadHandler.fromInputStream(
                    _ -> new DownloadResponse(new ByteArrayInputStream(json), fileName, "application/json",
                                              json.length)));
            exportLink.getElement().executeJs("this.click()");
        } catch (RuntimeException e) {
            LOG.error("Could not export adventure {}", anAdventure.getId(), e);
            showError("Could not export adventure '" + anAdventure.getTitle() + "'.");
        }
    }

    /** A title made safe as a file name: letters and digits kept, every other run of characters becomes one dash. */
    static String exportFileName(String aTitle) {
        String name = aTitle == null ? "" : aTitle.trim().replaceAll("[^\\p{L}\\p{N}]+", "-").replaceAll("^-|-$", "");
        return (name.isEmpty() ? "adventure" : name) + EXPORT_FILE_SUFFIX;
    }

    /**
     * Package-private for testing. The upload only collects the bytes (its callback runs without the UI lock); the
     * Import button reads them on the UI thread once the upload has finished.
     */
    Dialog buildImportDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Import Adventure");

        AtomicReference<byte[]> uploaded = new AtomicReference<>();
        Upload upload = new Upload(UploadHandler.inMemory((_, data) -> uploaded.set(data)));
        upload.setMaxFiles(1);
        upload.setAcceptedFileExtensions(".json");
        long maxBytes = accessService.getMaxImportBytes();
        if (maxBytes > 0) {
            upload.setMaxFileSize((int) Math.min(Integer.MAX_VALUE, maxBytes));
        }

        Button importButton = new Button("Import", _ -> {
            byte[] data = uploaded.get();
            if (data != null && importAdventure(data)) {
                dialog.close();
            }
        });
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setEnabled(false);
        upload.addAllFinishedListener(_ -> importButton.setEnabled(uploaded.get() != null));
        upload.addFileRemovedListener(_ -> {
            uploaded.set(null);
            importButton.setEnabled(false);
        });

        dialog.add(upload);
        dialog.getFooter().add(new Button("Cancel", _ -> dialog.close()), importButton);
        return dialog;
    }

    /**
     * Package-private for testing.
     *
     * @return true if the adventure was imported
     */
    @SuppressWarnings("unchecked")
    boolean importAdventure(byte[] aFile) {
        try {
            AdventureAccessService.ImportedAdventure imported =
                    accessService.importAdventure(aFile, ViewSupporter.getCurrentUser());

            ListDataProvider<AdventureData> dataProvider = (ListDataProvider<AdventureData>) adventureGrid.getDataProvider();
            dataProvider.getItems().add(imported.adventure());
            dataProvider.refreshAll();

            String text = "Adventure '" + imported.adventure().getTitle() + "' imported.";
            if (imported.builderVersionDiffers()) {
                text += " The file was written by another version of the builder - please check the adventure.";
            }
            Notification notification = Notification.show(text, imported.builderVersionDiffers() ? 6000 : 2000,
                                                          Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } catch (AdventureImportException e) {
            showError("Could not import the adventure: " + e.getMessage());
            return false;
        } catch (RuntimeException e) {
            LOG.error("Could not import an adventure", e);
            showError("Could not import the adventure.");
            return false;
        }
    }

    private static void showError(String aText) {
        Notification notification = Notification.show(aText, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
```

(`duplicateAdventure` may keep its own inline error notification; do not refactor it.)

Check on the expected filename in the `exportFileName` test: `"Über \"Größe\": 1/2"` → regex replaces runs of non-letter/digit with `-`: `Über-Größe-1-2`. `"  ../etc/passwd  "` → trim → `../etc/passwd` → `-etc-passwd` → strip leading dash → `etc-passwd`.

- [ ] **Step 4: Run the view tests**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -q test -Dtest=AdventuresMenuViewTest`
Expected: PASS. If `export_asksTheAccessServiceForTheFileAndPointsTheDownloadLinkAtIt` cannot read an href in the browserless environment, assert on `exportLink`'s `href` element attribute instead (`find(Anchor.class, view).single().getElement().getAttribute("href")`), keeping the intent: a link target was set only on success. If an `InputStreamDownloadCallback`/`InMemoryUploadCallback` lambda shape differs from the above, fix against the Vaadin MCP (`get_java_symbol`), not by guessing.

- [ ] **Step 5: Manually verify in the running app**

Start the app (`cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn spring-boot:run`, embedded Mongo), log in as an author (see the dev-login memory note: reset the admin password if no login is known), create an adventure with a location, then in the browser: right-click the row → Export (a `.adventure.json` downloads); Import Adventure → upload that file → the new row appears; upload a text file renamed `.json` → error notification "not valid JSON". Report what was and was not verified.

- [ ] **Step 6: Commit**

```bash
cd server
git add src/main/java/com/pdg/adventure/view/adventure/AdventuresMenuView.java src/test/java/com/pdg/adventure/view/adventure/AdventuresMenuViewTest.java
git commit -m "$(cat <<'EOF'
feat: export and import adventures from the author menu

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 6: Changelog and full verification

**Files:**
- Create: `CHANGELOG.md` (in `server/`; none exists yet — this run establishes the baseline per `AGENTS-update-changelog.md`)

- [ ] **Step 1: Create the changelog**

```markdown
# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- Export an adventure as a JSON file and import it into another database: authors can move an adventure between installations. Import always creates a new, independent adventure and validates the file first.
```

- [ ] **Step 2: Run the whole test suite**

Run: `cd server && JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn test` (or the `verify-tests` skill)
Expected: BUILD SUCCESS; report the exact pass count. Any failure must be investigated, not skipped.

- [ ] **Step 3: Commit**

```bash
cd server
git add CHANGELOG.md
git commit -m "$(cat <<'EOF'
docs: changelog entry for adventure import/export

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

## Self-Review

**Spec coverage:** format/envelope (Tasks 2-3); raw-document approach and shared helper (Task 1); validation list — bad format, newer version, allowlist (+ guard test), exactly one adventure, ids, dangling refs, size limit, version mismatch flag (Task 3); export/import access rules, MySQL compensation (Task 4); Export item, Import dialog, notifications, filename (Task 5); tests listed in the spec incl. round trip with picture bytes and variable value, independent copy, rejection cases, player cannot export, author ownership, MySQL failure (Tasks 2-5); `TestSupporter` fixture and changelog (Tasks 2, 6). The spec's "failed insert leaves no partial documents" rests on the rollback moved unchanged into `AdventureDocumentGraph.insertAll` (the duplicator had the same untested guarantee); validation failures are tested to insert nothing.

**Placeholders:** none; the only conditional notes (DatedData field name in Task 2, href assertion fallback and callback shapes in Task 5) are verification hints tied to a concrete check.

**Type consistency:** `ImportResult(String adventureId, boolean builderVersionDiffers)`, `ImportedAdventure(AdventureData adventure, boolean builderVersionDiffers)`, `getMaxBytes()` / `getMaxImportBytes()`, `exportAdventure` / `importAdventure` names and the 6-argument `AdventureAccessService` constructor are used identically in Tasks 3-5.
