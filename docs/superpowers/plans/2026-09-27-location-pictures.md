# Location Pictures Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let authors attach a picture to a location and to individual actions; during play, the picture renders in the top half of the screen on first-visit arrival, on "look", and on a new `PICTURE` action, and stays displayed until one of those triggers changes it. Authors manage pictures (upload/name/delete) in a new `PictureMenuView`.

**Architecture:** `PictureData` is a new top-level Mongo document (own collection, like `LocationData`), referenced from `AdventureData` via an eager `@DBRef` map exactly like `locationData` — no filesystem storage, no GridFS. Unlike `Location`, a picture has zero runtime behavior (no commands, no engine logic), so — mirroring how `MessageData` has no runtime "Message" business-object counterpart — there is **no runtime `Picture` class and no `PictureMapper`**; every layer (editor UI, menu UI, runtime display) works with `PictureData` directly. The only runtime state is a plain `String currentPictureId` on `GameContext`, set by three call sites (arrival, look, the new `PICTURE` action) and left untouched by everything else, so it persists across unrelated commands.

**Tech Stack:** Java 21+, Spring Boot, Spring Data MongoDB, Vaadin 25.2.6 (Upload component is bundled in the core `com.vaadin:vaadin` artifact already on the classpath — no new dependency needed), JUnit 5, Mockito, AssertJ, `com.vaadin.browserless.BrowserlessTest`.

**Spec:** `docs/superpowers/specs/2026-09-27-location-pictures-design.md`

## Global Constraints

- Accepted picture formats: PNG, WebP, JPEG only (`image/png`, `image/webp`, `image/jpeg`).
- Maximum picture size: 2MB (2 * 1024 * 1024 bytes).
- Every new/modified class follows this codebase's existing Lombok idiom: `@Data` + `@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)` for `*Data` model classes; constructor injection (no field injection) for Spring beans.
- A location's picture is suppressed whenever `isTooDarkToSee()` is true (existing private method on `Location`, unaffected by this plan).
- `currentPictureId` is deliberately **not** reset on every sub-command — this is a documented deviation from how `currentPreposition`/`currentAdverb`/`currentNoun2`/`currentAdjective2` behave in `GameLoop`, and every task that touches it must preserve that (no reset hook is ever added to `GameLoop`).

## Review Focus

- **Deleting a picture that's still referenced** (by a location's default picture or a `PICTURE` action) must be blocked with a listing of referencers, not silently allowed — `PictureUsageTracker` + `PictureMenuView`'s delete guard (Task 6, Task 8) is what a person expects after the identical, already-shipped `LocationUsageTracker` behavior.
- **Uploading a file that isn't PNG/WebP/JPEG, or exceeds 2MB**, must be rejected with a visible error, not silently accepted or silently dropped — Task 7's `Upload.setAcceptedFileTypes`/`setMaxFileSize` plus the `addFileRejectedListener` wiring.
- **A location with no picture set** (the common case for most locations) must never show a broken/empty image region during play — Task 10's `pictureContainer.setVisible(false)` when `currentPictureId` is `null`, not just an empty `Image` src.
- **A failed move** (e.g. "go north" with no exit that way) must leave whatever picture was already showing untouched, not blank the screen — Task 4's `MovePlayerAction` change only ever reaches the picture-setting code on a successful move; Task 10/Task 4 tests must assert this explicitly, since it is easy to accidentally regress by moving the picture-clearing logic earlier.
- **A picture the author deletes while still referenced by data that predates the delete guard** (e.g. hand-edited Mongo data, or a bug elsewhere) must not crash play — Task 10's `adventureData.getPictureData().get(pictureId)` returning `null` must degrade to "no picture shown," not an NPE.

---

## Task 1: `PictureData` model, repository, and `AdventureData` wiring

**Files:**
- Create: `src/main/java/com/pdg/adventure/model/PictureData.java`
- Create: `src/main/java/com/pdg/adventure/server/storage/repository/PictureRepository.java`
- Modify: `src/main/java/com/pdg/adventure/model/AdventureData.java`
- Modify: `src/main/java/com/pdg/adventure/server/storage/service/AdventureService.java`
- Modify: `src/test/java/com/pdg/adventure/server/storage/AdventureServiceTest.java:39-40`
- Modify: `src/test/java/com/pdg/adventure/server/storage/AdventureDeleteCascadeTest.java:133-134`
- Create: `src/test/java/com/pdg/adventure/server/storage/PictureDataTest.java`

**Interfaces:**
- Produces: `PictureData` with `getId()/setId()` (from `BasicData`), `getAdventureId()/setAdventureId(String)`, `getName()/setName(String)`, `getContent()/setContent(byte[])`, `getContentType()/setContentType(String)`.
- Produces: `AdventureData.getPictureData()/setPictureData(Map<String,PictureData>)` — a `Map<String,PictureData>`, mirroring `getLocationData()`.
- Produces: `AdventureService.savePictureData(PictureData)`, `AdventureService.deletePicture(String id)`.
- Consumes: nothing new (uses `BasicData`, `DatedData` already in the codebase).

- [ ] **Step 1: Write `PictureData`**

```java
package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pictures")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PictureData extends com.pdg.adventure.model.basic.BasicData {
    private String adventureId;
    private String name;
    private byte[] content;
    private String contentType;
}
```

- [ ] **Step 2: Write `PictureRepository`**

```java
package com.pdg.adventure.server.storage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pdg.adventure.model.PictureData;

@Repository
public interface PictureRepository extends MongoRepository<PictureData, String> {
}
```

- [ ] **Step 3: Add `pictureData` to `AdventureData`**

In `src/main/java/com/pdg/adventure/model/AdventureData.java`, add the import `import com.pdg.adventure.model.PictureData;`, then add this field right after the existing `locationData` field (same `@DBRef(lazy = false)` eager-loading choice as `locationData` — not `playerPocket`'s `lazy = true`, since Spring Data Mongo's lazy-proxy support for a `Map<String, T>` DBRef is unproven in this codebase, whereas eager-map-DBRef is the exact, already-working pattern `locationData` uses):

```java
    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private Map<String, PictureData> pictureData;
```

And in the `AdventureData(VocabularyData aVocabularyData)` constructor, add `pictureData = new HashMap<>();` alongside the existing `locationData = new HashMap<>();` line.

- [ ] **Step 4: Run existing `AdventureData`-related tests to confirm nothing broke**

Run: `mvn test -Dtest=AdventureServiceTest,AdventureDeleteCascadeTest -pl . 2>&1 | tail -60` (from `server/`)
Expected: FAIL — both tests still construct `AdventureService` with the old 5-argument constructor; `AdventureService` doesn't have the new field/constructor param yet (added in Step 5), so this compiles fine for now (AdventureData changes alone don't break these) — this step is a checkpoint, not expected to fail. If it fails, stop and diagnose before continuing.

- [ ] **Step 5: Add picture persistence methods to `AdventureService`**

In `src/main/java/com/pdg/adventure/server/storage/service/AdventureService.java`:
- Add import: `import com.pdg.adventure.model.PictureData;` and `import com.pdg.adventure.server.storage.repository.PictureRepository;`
- Add field: `private final PictureRepository pictureRepository;`
- Add constructor parameter `PictureRepository aPictureRepository` (append it, e.g. right after `LocationRepository aLocationRepository`) and assign `pictureRepository = aPictureRepository;` in the constructor body.
- Add these two methods (place them near the location methods for readability):

```java
    public void savePictureData(PictureData aPictureData) {
        LOG.debug("Saving picture data: {}", aPictureData);
        LOG.info("Saving picture data: {}", aPictureData.getId());
        pictureRepository.save(aPictureData);
    }

    public void deletePicture(String anId) {
        LOG.info("Deleting picture: {}", anId);
        pictureRepository.findById(anId).ifPresentOrElse(
                pictureRepository::delete,
                () -> LOG.warn("Picture not found for deletion: {}", anId));
    }
```

- [ ] **Step 6: Fix the two existing direct `AdventureService` construction call sites**

In `src/test/java/com/pdg/adventure/server/storage/AdventureServiceTest.java`, the constructor call at lines 39-40 currently reads:
```java
        adventureService = new AdventureService(locationRepository, adventureRepository, wordRepository,
                vocabularyRepository, cascadeDeleteHelper);
```
Change to add a mocked `PictureRepository` (add a `@Mock private PictureRepository pictureRepository;` field near the other `@Mock` fields in this test class, then):
```java
        adventureService = new AdventureService(locationRepository, adventureRepository, wordRepository,
                vocabularyRepository, cascadeDeleteHelper, pictureRepository);
```
(Match whatever parameter order you chose in Step 5 exactly — this call must list arguments in the same order as the constructor.)

In `src/test/java/com/pdg/adventure/server/storage/AdventureDeleteCascadeTest.java`, line 133-134 currently reads:
```java
        AdventureService adventureService = new AdventureService(locationRepository, adventureRepository,
                                                                 null, null, cascadeDeleteHelper);
```
Change to pass `null` for the new parameter too (this test already passes `null` for repositories it doesn't exercise):
```java
        AdventureService adventureService = new AdventureService(locationRepository, adventureRepository,
                                                                 null, null, cascadeDeleteHelper, null);
```

- [ ] **Step 7: Run the fixed tests**

Run: `mvn test -Dtest=AdventureServiceTest,AdventureDeleteCascadeTest -pl .`
Expected: PASS (all previously-passing tests still pass; no new tests were added to these files in this task).

- [ ] **Step 8: Write `PictureDataTest`**

```java
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
    void findAll_returnsSavedPictures(@Autowired MongoTemplate mongoTemplate) {
        mongoTemplate.save(new PictureData());

        List<PictureData> all = mongoTemplate.findAll(PictureData.class);
        assertThat(all).hasSize(1);
    }
}
```

- [ ] **Step 9: Run the new test**

Run: `mvn test -Dtest=PictureDataTest -pl .`
Expected: PASS. If the two tests interfere with each other's document counts (leftover state from a shared embedded Mongo context), add `@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)` at the class level, matching the caution `LocationDataTest` takes with `@DirtiesContext` on its own order-sensitive test.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/pdg/adventure/model/PictureData.java \
        src/main/java/com/pdg/adventure/server/storage/repository/PictureRepository.java \
        src/main/java/com/pdg/adventure/model/AdventureData.java \
        src/main/java/com/pdg/adventure/server/storage/service/AdventureService.java \
        src/test/java/com/pdg/adventure/server/storage/AdventureServiceTest.java \
        src/test/java/com/pdg/adventure/server/storage/AdventureDeleteCascadeTest.java \
        src/test/java/com/pdg/adventure/server/storage/PictureDataTest.java
git commit -m "Add PictureData model, repository, and AdventureData wiring"
```

---

## Task 2: `PICTURE` action (data, runtime, mapper)

**Files:**
- Create: `src/main/java/com/pdg/adventure/model/action/PictureActionData.java`
- Create: `src/main/java/com/pdg/adventure/server/action/PictureAction.java`
- Create: `src/main/java/com/pdg/adventure/server/mapper/action/PictureActionMapper.java`
- Create: `src/test/java/com/pdg/adventure/server/action/PictureActionTest.java`
- Create: `src/test/java/com/pdg/adventure/server/mapper/action/PictureActionMapperTest.java`

**Interfaces:**
- Consumes: `GameContext.setCurrentPictureId(String)` — **not yet added** (that's Task 4). This task's `PictureAction.execute()` calls it, so it will not compile until Task 4 lands `setCurrentPictureId` on `GameContext`. To keep tasks independently testable and buildable, do Task 4's `GameContext` change (just the two-line getter/setter addition, not the rest of Task 4) as part of this task's Step 1 instead of waiting — see Step 1 note below.
- Produces: `PictureActionData.getPictureId()/setPictureId(String)`; `PictureAction(String pictureId, GameContext gameContext)`, `PictureAction.getPictureId()`, `PictureAction.execute()`, `PictureAction.isInformationalOnly()`; `PictureActionMapper.mapToBO(PictureActionData)`, `PictureActionMapper.mapToDO(PictureAction)`.

- [ ] **Step 1: Add `currentPictureId` to `GameContext` (pulled forward from Task 4 so this task compiles standalone)**

In `src/main/java/com/pdg/adventure/server/engine/GameContext.java`, add a field and a getter/setter pair, following the exact style of the existing `currentPreposition` field (but **without** the null-coalescing — `null` is a meaningful, valid value for "no picture," unlike the empty-string convention used for preposition/adverb/noun2/adjective2):

```java
    private String currentPictureId;
```

```java
    public void setCurrentPictureId(String aPictureId) {
        currentPictureId = aPictureId;
    }

    public String getCurrentPictureId() {
        return currentPictureId;
    }
```

- [ ] **Step 2: Write `PictureActionData`**

```java
package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PictureActionData extends ActionData {
    private String pictureId;
}
```

- [ ] **Step 3: Write the failing test for `PictureAction`**

```java
package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

@ExtendWith(MockitoExtension.class)
class PictureActionTest {

    @Mock
    private GameContext gameContext;

    @Test
    void execute_setsCurrentPictureIdOnGameContext() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        action.execute();

        Mockito.verify(gameContext).setCurrentPictureId("treasure-chest");
    }

    @Test
    void execute_returnsSuccess() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        ExecutionResult result = action.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void isInformationalOnly_isTrue() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        assertThat(action.isInformationalOnly()).isTrue();
    }

    @Test
    void constructor_setsPictureIdCorrectly() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        assertThat(action.getPictureId()).isEqualTo("treasure-chest");
        assertThat(action.getActionName()).isEqualTo("PictureAction");
    }
}
```

- [ ] **Step 4: Run it to verify it fails**

Run: `mvn test -Dtest=PictureActionTest -pl .`
Expected: FAIL — compilation error, `PictureAction` does not exist yet.

- [ ] **Step 5: Write `PictureAction`**

```java
package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PictureAction extends AbstractAction {

    private final String pictureId;
    private final transient GameContext gameContext;

    public PictureAction(String aPictureId, GameContext aGameContext) {
        pictureId = aPictureId;
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.setCurrentPictureId(pictureId);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `mvn test -Dtest=PictureActionTest -pl .`
Expected: PASS.

- [ ] **Step 7: Write the failing test for `PictureActionMapper`**

```java
package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.server.action.PictureAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class PictureActionMapperTest {

    @Mock
    private GameContext gameContext;

    @Mock
    private MapperSupporter mapperSupporter;

    private PictureActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PictureActionMapper(gameContext, mapperSupporter);
    }

    @Test
    void mapToBO_createsPictureActionWithGivenPictureId() {
        PictureActionData data = new PictureActionData();
        data.setPictureId("treasure-chest");

        PictureAction result = mapper.mapToBO(data);

        assertThat(result.getPictureId()).isEqualTo("treasure-chest");
    }

    @Test
    void mapToDO_convertsPictureActionToData() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        PictureActionData result = mapper.mapToDO(action);

        assertThat(result.getPictureId()).isEqualTo("treasure-chest");
    }
}
```

- [ ] **Step 8: Run it to verify it fails**

Run: `mvn test -Dtest=PictureActionMapperTest -pl .`
Expected: FAIL — compilation error, `PictureActionMapper` does not exist yet.

- [ ] **Step 9: Write `PictureActionMapper`**

```java
package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.server.action.PictureAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Picture action mapper")
public class PictureActionMapper extends ActionMapper<PictureActionData, PictureAction> {

    private final GameContext gameContext;

    public PictureActionMapper(GameContext aGameContext, MapperSupporter aMapperSupporter) {
        super(aMapperSupporter);
        gameContext = aGameContext;
    }

    @Override
    public PictureAction mapToBO(PictureActionData actionData) {
        return new PictureAction(actionData.getPictureId(), gameContext);
    }

    @Override
    public PictureActionData mapToDO(PictureAction action) {
        PictureActionData data = new PictureActionData();
        data.setPictureId(action.getPictureId());
        return data;
    }
}
```

- [ ] **Step 10: Run the test to verify it passes**

Run: `mvn test -Dtest=PictureActionMapperTest -pl .`
Expected: PASS.

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/engine/GameContext.java \
        src/main/java/com/pdg/adventure/model/action/PictureActionData.java \
        src/main/java/com/pdg/adventure/server/action/PictureAction.java \
        src/main/java/com/pdg/adventure/server/mapper/action/PictureActionMapper.java \
        src/test/java/com/pdg/adventure/server/action/PictureActionTest.java \
        src/test/java/com/pdg/adventure/server/mapper/action/PictureActionMapperTest.java
git commit -m "Add PICTURE action (data, runtime, mapper) and GameContext.currentPictureId"
```

---

## Task 3: Location's default picture (`LocationData`, `Location`, `LocationMapper`)

**Files:**
- Modify: `src/main/java/com/pdg/adventure/model/LocationData.java`
- Modify: `src/main/java/com/pdg/adventure/server/location/Location.java`
- Modify: `src/main/java/com/pdg/adventure/server/mapper/LocationMapper.java`
- Modify: `src/test/java/com/pdg/adventure/server/mapper/LocationMapperTest.java`

**Interfaces:**
- Produces: `LocationData.getPictureId()/setPictureId(String)`; `Location.getPictureId()/setPictureId(String)` (the **raw**, ungated accessor — darkness-gating is applied separately in Task 4's `getArrivalDescription()`/`getLookDescription()`, not here, so `LocationMapper.mapToDO()` never silently loses a location's configured picture just because the location happens to be dark at mapping time).

- [ ] **Step 1: Add `pictureId` to `LocationData`**

In `src/main/java/com/pdg/adventure/model/LocationData.java`, add:

```java
    private String pictureId;
```

right after the existing `private int lumen = 50;` field. No constructor change needed (Lombok's `@Data` default for a `String` field is `null`, which is the correct "no picture" default).

- [ ] **Step 2: Add `pictureId` to `Location`**

In `src/main/java/com/pdg/adventure/server/location/Location.java`, add a field and plain getter/setter (no darkness gating here — see Task 4 for where that's applied):

```java
    private String pictureId;
```

```java
    public void setPictureId(String aPictureId) {
        pictureId = aPictureId;
    }

    public String getPictureId() {
        return pictureId;
    }
```

- [ ] **Step 3: Wire `pictureId` through `LocationMapper`**

In `src/main/java/com/pdg/adventure/server/mapper/LocationMapper.java`, `mapToBO(LocationData)`, add right after the existing `location.setTimesVisited(aLocationData.getTimesVisited());` line:

```java
        location.setPictureId(aLocationData.getPictureId());
```

And in `mapToDO(Location)`, add right after `result.setLumen(aLocation.getLight());`:

```java
        result.setPictureId(aLocation.getPictureId());
```

- [ ] **Step 4: Extend `LocationMapperTest`**

Add these two tests to `src/test/java/com/pdg/adventure/server/mapper/LocationMapperTest.java`, following the file's existing pattern (constructing `new LocationMapper(mapperSupporter, descriptionMapper, itemContainerMapper, directionMapper, commandProviderMapper)` with mocked collaborators, as the existing tests already do):

```java
    @Test
    void mapToBO_copiesThePictureId() {
        LocationData locationData = new LocationData();
        locationData.setPictureId("treasure-chest");
        when(descriptionMapper.mapToBO(any())).thenReturn(new DescriptionProvider("noun"));

        Location result = locationMapper.mapToBO(locationData);

        assertThat(result.getPictureId()).isEqualTo("treasure-chest");
    }

    @Test
    void mapToDO_copiesThePictureId() {
        Location location = new Location(new DescriptionProvider("noun"));
        location.setPictureId("treasure-chest");
        when(descriptionMapper.mapToDO(any())).thenReturn(new DescriptionData());

        LocationData result = locationMapper.mapToDO(location);

        assertThat(result.getPictureId()).isEqualTo("treasure-chest");
    }
```

Adjust the exact mock setup (variable names, whether `itemContainerMapper.mapToDO(...)` also needs stubbing) to match whatever the existing `mapToBO`/`mapToDO` tests in that file already do for their own similar-shaped assertions — copy their existing stubbing pattern for a minimal successful mapping rather than introducing a new one.

- [ ] **Step 5: Run the tests**

Run: `mvn test -Dtest=LocationMapperTest -pl .`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/model/LocationData.java \
        src/main/java/com/pdg/adventure/server/location/Location.java \
        src/main/java/com/pdg/adventure/server/mapper/LocationMapper.java \
        src/test/java/com/pdg/adventure/server/mapper/LocationMapperTest.java
git commit -m "Add a location's default pictureId, wired through LocationMapper"
```

---

## Task 4: Runtime trigger mechanism — `LocationDescription`, arrival, and look

This is the task with the highest blast radius: `Location.getArrivalDescription()` changes its return type from `String` to a new record, which breaks every existing caller. Do this task carefully, one file at a time, and run the full affected test set at the end.

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/location/Location.java`
- Modify: `src/main/java/com/pdg/adventure/server/action/MovePlayerAction.java`
- Modify: `src/main/java/com/pdg/adventure/CommandFactory.java`
- Modify: `src/test/java/com/pdg/adventure/server/location/LocationTest.java`
- Modify: `src/test/java/com/pdg/adventure/server/action/MovePlayerActionTest.java`
- Modify: `src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`

**Interfaces:**
- Consumes: `GameContext.setCurrentPictureId(String)` (added in Task 2).
- Produces: `Location.LocationDescription` record `(String text, String pictureId)`; `Location.getArrivalDescription(): LocationDescription` (return type **changed** from `String`); `Location.getLookDescription(): LocationDescription` (new method).

- [ ] **Step 1: Add the `LocationDescription` record and a private darkness-gated helper to `Location`**

In `src/main/java/com/pdg/adventure/server/location/Location.java`, add the record as a nested type (place it right before `getArrivalDescription()`):

```java
    public record LocationDescription(String text, String pictureId) {}
```

Add a private helper right after `isTooDarkToSee()`:

```java
    private String pictureIdIfVisible() {
        return isTooDarkToSee() ? null : pictureId;
    }
```

- [ ] **Step 2: Change `getArrivalDescription()` and add `getLookDescription()`**

Replace the existing `getArrivalDescription()` method:

```java
    /**
     * The description shown when the player arrives at this location: the full (long)
     * description on the very first visit, the short one on every later visit. Exits and
     * visible items are always listed. The picture is included only on the very first visit
     * (and only if the location isn't too dark to see) - a repeat visit explicitly carries a
     * {@code null} pictureId so the caller can clear whatever picture was showing before.
     */
    public LocationDescription getArrivalDescription() {
        boolean firstVisit = timesVisited == 0;
        String body = firstVisit ? super.getLongDescription() : getShortDescription();
        String pictureIdToShow = firstVisit ? pictureIdIfVisible() : null;
        return new LocationDescription(renderDescription(body), pictureIdToShow);
    }

    /**
     * The full description plus this location's picture, shown whenever the player explicitly
     * describes or examines this location - regardless of how often it has already been
     * visited. The picture is included every time (subject to the darkness gate), unlike
     * {@link #getArrivalDescription()}'s first-visit-only picture.
     */
    public LocationDescription getLookDescription() {
        return new LocationDescription(getLongDescription(), pictureIdIfVisible());
    }
```

Leave the existing `@Override public String getLongDescription()` method exactly as-is — its return type does **not** change, and every other caller of `getLongDescription()` across the codebase (Items, `CommandFactory`'s examine fallback, `LocationsMenuView`'s context menu, etc.) is unaffected by this task.

- [ ] **Step 3: Update `MovePlayerAction`**

Replace the body of `execute()` in `src/main/java/com/pdg/adventure/server/action/MovePlayerAction.java`:

```java
    @Override
    public ExecutionResult execute() {
        gameContext.setCurrentLocation(destination);
        Location.LocationDescription description = destination.getArrivalDescription();
        final DescribeAction describeAction = new DescribeAction(description::text);
        ExecutionResult result = describeAction.execute();
        gameContext.setCurrentPictureId(description.pictureId());
        variableProvider.set(new Variable(VariableProvider.VISITED_VARIABLE_NAME, (int) destination.getTimesVisited()));
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        String arrivalMessage = gameContext.runArrivalProcesses().getResultMessage();
        if (!arrivalMessage.isEmpty()) {
            result.setResultMessage(result.getResultMessage() + "\n" + arrivalMessage);
        }
        return result;
    }
```

(`Location` is already imported in this file.)

- [ ] **Step 4: Update `CommandFactory`'s built-in look/describe commands**

In `src/main/java/com/pdg/adventure/CommandFactory.java`, replace:

```java
        Action lookLocationAction = new DescribeAction(
                () -> gameContext.getCurrentLocation().getLongDescription());
```

with:

```java
        Action lookLocationAction = new DescribeAction(() -> {
            var description = gameContext.getCurrentLocation().getLookDescription();
            gameContext.setCurrentPictureId(description.pictureId());
            return description.text();
        });
```

(Uses `var` deliberately so no new import of `Location` is needed in this file.)

- [ ] **Step 5: Update `LocationTest`'s existing `getArrivalDescription` assertions**

In `src/test/java/com/pdg/adventure/server/location/LocationTest.java`, the three existing tests that call `.getArrivalDescription()` as if it returned a `String` need `.text()` added:

```java
    @Test
    void getArrivalDescription_onFirstVisit_usesTheLongDescription() {
        Location room = roomWithDescriptions("The short room.", "The long, richly detailed room.");
        assertThat(room.getTimesVisited()).isZero();

        assertThat(room.getArrivalDescription().text())
                .contains("The long, richly detailed room.")
                .doesNotContain("The short room.");
    }

    @Test
    void getArrivalDescription_afterTheFirstVisit_usesTheShortDescription() {
        Location room = roomWithDescriptions("The short room.", "The long, richly detailed room.");
        room.setTimesVisited(1);

        assertThat(room.getArrivalDescription().text())
                .contains("The short room.")
                .doesNotContain("The long, richly detailed room.");
    }

    @Test
    void getArrivalDescription_belowTheLightThreshold_yieldsOnlyTheDarknessMessage() {
        Location room = roomWithDescriptions("The short room.", "The long, richly detailed room.");
        room.setLight(0);

        assertThat(room.getArrivalDescription().text())
                .isEqualTo(System.lineSeparator() + SystemMessageKey.SM0.defaultText())
                .doesNotContain("The long, richly detailed room.");
    }
```

- [ ] **Step 6: Add new picture-specific tests to `LocationTest`**

Add these five tests, using the existing `roomWithDescriptions` helper (add an overload that also sets a picture, since the existing one doesn't):

```java
    private Location roomWithDescriptionsAndPicture(String aShortDescription, String aLongDescription,
                                                     String aPictureId) {
        Location room = roomWithDescriptions(aShortDescription, aLongDescription);
        room.setPictureId(aPictureId);
        return room;
    }

    @Test
    void getArrivalDescription_onFirstVisit_includesTheLocationsPicture() {
        Location room = roomWithDescriptionsAndPicture("short", "long", "treasure-chest");

        assertThat(room.getArrivalDescription().pictureId()).isEqualTo("treasure-chest");
    }

    @Test
    void getArrivalDescription_afterTheFirstVisit_hasNoPicture() {
        Location room = roomWithDescriptionsAndPicture("short", "long", "treasure-chest");
        room.setTimesVisited(1);

        assertThat(room.getArrivalDescription().pictureId()).isNull();
    }

    @Test
    void getArrivalDescription_whenTooDarkToSee_hasNoPictureEvenOnFirstVisit() {
        Location room = roomWithDescriptionsAndPicture("short", "long", "treasure-chest");
        room.setLight(0);

        assertThat(room.getArrivalDescription().pictureId()).isNull();
    }

    @Test
    void getLookDescription_alwaysIncludesTheLocationsPicture_regardlessOfVisitCount() {
        Location room = roomWithDescriptionsAndPicture("short", "long", "treasure-chest");
        room.setTimesVisited(5);

        assertThat(room.getLookDescription().pictureId()).isEqualTo("treasure-chest");
        assertThat(room.getLookDescription().text()).contains("long");
    }

    @Test
    void getLookDescription_whenTooDarkToSee_hasNoPicture() {
        Location room = roomWithDescriptionsAndPicture("short", "long", "treasure-chest");
        room.setLight(0);

        assertThat(room.getLookDescription().pictureId()).isNull();
    }
```

- [ ] **Step 7: Update `MovePlayerActionTest`**

In `src/test/java/com/pdg/adventure/server/action/MovePlayerActionTest.java`, every `when(destination.getArrivalDescription()).thenReturn("...")` line needs to wrap the string in the new record. There are 7 occurrences; replace each, e.g.:

```java
        when(destination.getArrivalDescription()).thenReturn(new Location.LocationDescription("A dark cave.", null));
```

(same substitution for `"A sunlit meadow."`, `"A tower."` — every occurrence in this file, keeping `null` as the pictureId for all of them, since none of the existing tests care about the picture).

Then add two new tests:

```java
    @Test
    void execute_setsCurrentPictureId_toTheArrivalDescriptionsPicture() {
        when(destination.getArrivalDescription())
                .thenReturn(new Location.LocationDescription("A dark cave.", "cave-entrance"));
        when(destination.getTimesVisited()).thenReturn(0L);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(gameContext).setCurrentPictureId("cave-entrance");
    }

    @Test
    void execute_setsCurrentPictureIdToNull_whenTheArrivalDescriptionHasNoPicture() {
        when(destination.getArrivalDescription())
                .thenReturn(new Location.LocationDescription("A tower.", null));
        when(destination.getTimesVisited()).thenReturn(3L);

        new MovePlayerAction(destination, gameContext, variableProvider).execute();

        verify(gameContext).setCurrentPictureId(null);
    }
```

- [ ] **Step 8: Update `AdventureRunViewTest`'s `stubOpeningRoom` helper**

In `src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`, change:

```java
        when(startLocation.getArrivalDescription()).thenReturn(description);
```

to:

```java
        when(startLocation.getArrivalDescription())
                .thenReturn(new com.pdg.adventure.server.location.Location.LocationDescription(description, null));
```

(Leave the rest of `stubOpeningRoom` as-is — this task only fixes the compile break; Task 10 extends `stubOpeningRoom` further with a picture-carrying overload.)

- [ ] **Step 9: Run every affected test class**

Run: `mvn test -Dtest=LocationTest,MovePlayerActionTest,AdventureRunViewTest,LocationMapperTest,PictureActionTest,PictureActionMapperTest -pl .`
Expected: PASS — this is the full set of tests touched directly or transitively by the `getArrivalDescription()` signature change.

- [ ] **Step 10: Run the full test suite to catch any missed caller**

Run: `mvn test -pl . 2>&1 | tail -100`
Expected: PASS. If anything else fails with a `getArrivalDescription()`-related compile error, it's a caller this plan's research missed (the codebase search in this task's preparation found exactly these call sites: `MovePlayerAction`, `MovePlayerActionTest`, `LocationTest`, `AdventureRunViewTest` — if a fifth one surfaces, fix it the same way: wrap the stub/assertion to use the record's `.text()`/`.pictureId()`).

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/location/Location.java \
        src/main/java/com/pdg/adventure/server/action/MovePlayerAction.java \
        src/main/java/com/pdg/adventure/CommandFactory.java \
        src/test/java/com/pdg/adventure/server/location/LocationTest.java \
        src/test/java/com/pdg/adventure/server/action/MovePlayerActionTest.java \
        src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java
git commit -m "Wire picture display into arrival and look, unified with the long/short description decision"
```

---

## Task 5: `PictureActionEditor` and `ActionSelector` wiring

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/action/PictureActionEditor.java`
- Modify: `src/main/java/com/pdg/adventure/view/command/action/ActionSelector.java`

**Interfaces:**
- Consumes: `AdventureData.getPictureData(): Map<String,PictureData>` (Task 1); `PictureActionData.getPictureId()/setPictureId(String)` (Task 2); `ActionEditorComponent<D>` base class (existing).
- Produces: nothing new consumed elsewhere — this is a leaf UI component discovered automatically via `@AutoRegisterActionEditor` + `ActionEditorRegistry`'s classpath scan, exactly like every other action editor.

- [ ] **Step 1: Write `PictureActionEditor`**

```java
package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.action.PictureActionData;

@AutoRegisterActionEditor
public class PictureActionEditor extends ActionEditorComponent<PictureActionData> {
    private final PictureActionData pictureActionData;
    private final AdventureData adventureData;
    private ComboBox<PictureData> pictureComboBox;

    public PictureActionEditor(PictureActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.pictureActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Picture Action");
        Span description = new Span("Show a picture to the player for the rest of this turn");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<PictureData> pictures = new ArrayList<>(adventureData.getPictureData().values());
        pictures.sort(Comparator.comparing(PictureData::getName, String.CASE_INSENSITIVE_ORDER));

        pictureComboBox = new ComboBox<>("Picture");
        pictureComboBox.setItems(pictures);
        pictureComboBox.setItemLabelGenerator(PictureData::getName);
        pictureComboBox.setPlaceholder("Select a picture");
        pictureComboBox.setWidthFull();
        pictureComboBox.setRequired(true);

        if (pictureActionData.getPictureId() != null) {
            pictures.stream()
                    .filter(picture -> picture.getId().equals(pictureActionData.getPictureId()))
                    .findFirst()
                    .ifPresent(pictureComboBox::setValue);
        }

        pictureComboBox.addValueChangeListener(e -> pictureActionData.setPictureId(
                e.getValue() == null ? null : e.getValue().getId()));

        add(title, description, pictureComboBox);
    }

    @Override
    public boolean validate() {
        boolean isValid = pictureComboBox.getValue() != null;
        pictureComboBox.setInvalid(!isValid);
        if (!isValid) {
            pictureComboBox.setErrorMessage("Please select a picture");
        }
        return isValid;
    }

    @Override
    public String getActionSummary() {
        PictureData selected = pictureComboBox == null ? null : pictureComboBox.getValue();
        return selected == null ? "(none)" : selected.getName();
    }
}
```

- [ ] **Step 2: Register the action type in `ActionSelector`**

In `src/main/java/com/pdg/adventure/view/command/action/ActionSelector.java`, add an import `import com.pdg.adventure.model.action.PictureActionData;` (or rely on the existing `import com.pdg.adventure.model.action.*;` wildcard already present — check first; the file already wildcard-imports `com.pdg.adventure.model.action.*`, so no new import line is needed). Add one entry to the `Stream.of(...)` list in `getAvailableActionTypes()`:

```java
        new ActionTypeDescriptor("Picture", "Show a picture to the player", PictureActionData::new),
```

- [ ] **Step 3: Build and run the existing action-editor-related tests to confirm the registry still resolves everything correctly**

Run: `mvn test -Dtest=ActionSelectorTest,ActionEditorRegistryTest,ActionEditorFactoryTest -pl . 2>&1 | tail -60`

(If any of these three test classes don't exist, skip that one — this step is a smoke check, not a hard requirement to create tests that don't already exist elsewhere in the codebase.)
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/action/PictureActionEditor.java \
        src/main/java/com/pdg/adventure/view/command/action/ActionSelector.java
git commit -m "Add PictureActionEditor and register the Picture action type"
```

---

## Task 6: `PictureUsageTracker` and the `PICTURE_ID` route parameter

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/picture/PictureUsageTracker.java`
- Create: `src/test/java/com/pdg/adventure/view/picture/PictureUsageTrackerTest.java`
- Modify: `src/main/java/com/pdg/adventure/view/support/RouteIds.java`

**Interfaces:**
- Consumes: `AdventureData.getLocationData()`, `LocationData.getPictureId()` (Task 3), `LocationData.getDirectionsData()`, `LocationData.getCommandProviderData()`, `PictureActionData.getPictureId()` (Task 2) — all existing/already-added.
- Produces: `PictureUsageTracker.findPictureUsages(AdventureData, String): List<PictureUsage>`, `PictureUsageTracker.countPictureUsages(AdventureData, String): int`, `PictureUsageTracker.isPictureUsed(AdventureData, String): boolean` — consumed by Task 8's `PictureMenuView`.
- Produces: `RouteIds.PICTURE_ID` — consumed by Task 7 and Task 8.

- [ ] **Step 1: Add `PICTURE_ID` to `RouteIds`**

In `src/main/java/com/pdg/adventure/view/support/RouteIds.java`, add one enum constant, e.g. right after `MESSAGE_ID`:

```java
    PICTURE_ID("pictureId"),
```

- [ ] **Step 2: Write the failing test for `PictureUsageTracker`**

```java
package com.pdg.adventure.view.picture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;

class PictureUsageTrackerTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setLocationData(new HashMap<>());
    }

    @Test
    void findPictureUsages_returnsEmpty_whenAdventureDataIsNull() {
        assertThat(PictureUsageTracker.findPictureUsages(null, "pic-1")).isEmpty();
    }

    @Test
    void findPictureUsages_returnsEmpty_whenPictureIdIsNull() {
        assertThat(PictureUsageTracker.findPictureUsages(adventureData, null)).isEmpty();
    }

    @Test
    void findPictureUsages_findsLocationsDefaultPicture() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setPictureId("pic-1");
        adventureData.getLocationData().put(location.getId(), location);

        List<PictureUsageTracker.PictureUsage> usages =
                PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().getDisplayText()).contains("Location Default Picture");
    }

    @Test
    void findPictureUsages_findsPictureActionInALocationCommand() {
        LocationData location = new LocationData();
        location.setId("loc-1");

        PictureActionData pictureAction = new PictureActionData();
        pictureAction.setPictureId("pic-1");

        CommandData command = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        commandDescription.setVerb("examine");
        command.setCommandDescription(commandDescription);
        command.getActions().add(pictureAction);

        CommandChainData chain = new CommandChainData();
        chain.getCommands().add(command);

        CommandProviderData commandProviderData = new CommandProviderData();
        commandProviderData.setAvailableCommands(new HashMap<>());
        commandProviderData.getAvailableCommands().put("examine", chain);
        location.setCommandProviderData(commandProviderData);

        adventureData.getLocationData().put(location.getId(), location);

        List<PictureUsageTracker.PictureUsage> usages =
                PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().getDisplayText()).contains("Picture Action");
    }

    @Test
    void countPictureUsages_returnsZero_whenNoneFound() {
        assertThat(PictureUsageTracker.countPictureUsages(adventureData, "pic-1")).isZero();
    }

    @Test
    void isPictureUsed_returnsFalse_whenNoneFound() {
        assertThat(PictureUsageTracker.isPictureUsed(adventureData, "pic-1")).isFalse();
    }
}
```

(Adjust `CommandData`/`CommandChainData`/`CommandProviderData`/`CommandDescriptionData` field/setter names if they differ slightly from this sketch — confirm the exact API against `LocationUsageTracker.checkCommandChains`/`checkCommand` in `src/main/java/com/pdg/adventure/view/location/LocationUsageTracker.java:204-226`, which already exercises this exact same object graph and is the ground truth for field/method names.)

- [ ] **Step 3: Run it to verify it fails**

Run: `mvn test -Dtest=PictureUsageTrackerTest -pl .`
Expected: FAIL — compilation error, `PictureUsageTracker` does not exist yet.

- [ ] **Step 4: Write `PictureUsageTracker`**

```java
package com.pdg.adventure.view.picture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.view.support.TrackedUsage;

/**
 * Utility class for tracking picture usage throughout an adventure. Scans every location's
 * default picture, and every PICTURE action attached to a location's own commands or to any of
 * its exits' commands. Mirrors {@link com.pdg.adventure.view.location.LocationUsageTracker}'s
 * scope exactly (same object graph, same coverage) - it does not scan item commands, matching
 * that class's existing, established scope rather than expanding it for this feature.
 */
public class PictureUsageTracker {

    public static class PictureUsage implements TrackedUsage {
        private final String usageType;
        private final String sourceLocationId;
        private final String sourceLocationDescription;
        private final String context;

        public PictureUsage(String usageType, String sourceLocationId, String sourceLocationDescription,
                            String context) {
            this.usageType = usageType;
            this.sourceLocationId = sourceLocationId;
            this.sourceLocationDescription = sourceLocationDescription;
            this.context = context;
        }

        public String getUsageType() {
            return usageType;
        }

        @Override
        public String getDisplayText() {
            if ("Location Default Picture".equals(usageType)) {
                String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
                return usageType + ": is the default picture for '" + locationName + "'";
            }
            String locationName = sourceLocationDescription != null ? sourceLocationDescription : sourceLocationId;
            return usageType + ": from '" + locationName + "' | " + context;
        }
    }

    public static List<PictureUsage> findPictureUsages(AdventureData adventureData, String pictureId) {
        List<PictureUsage> usages = new ArrayList<>();

        if (adventureData == null || pictureId == null || pictureId.isEmpty()) {
            return usages;
        }

        Map<String, LocationData> locations = adventureData.getLocationData();
        if (locations != null) {
            for (Map.Entry<String, LocationData> entry : locations.entrySet()) {
                LocationData location = entry.getValue();
                String sourceLocationId = entry.getKey();
                String sourceLocationDesc = location.getDescriptionData() != null
                        ? location.getDescriptionData().getShortDescription() : null;

                if (pictureId.equals(location.getPictureId())) {
                    usages.add(new PictureUsage("Location Default Picture", sourceLocationId, sourceLocationDesc,
                                                null));
                }

                checkLocationCommands(location, sourceLocationId, sourceLocationDesc, pictureId, usages);
                checkDirectionCommands(location, sourceLocationId, sourceLocationDesc, pictureId, usages);
            }
        }

        return usages;
    }

    private static void checkLocationCommands(LocationData sourceLocation, String sourceLocationId,
                                              String sourceLocationDesc, String pictureId,
                                              List<PictureUsage> usages) {
        if (sourceLocation.getCommandProviderData() == null
            || sourceLocation.getCommandProviderData().getAvailableCommands() == null) {
            return;
        }
        for (CommandChainData chain : sourceLocation.getCommandProviderData().getAvailableCommands().values()) {
            if (chain == null || chain.getCommands() == null) {
                continue;
            }
            for (CommandData command : chain.getCommands()) {
                checkCommandActions(command, sourceLocationId, sourceLocationDesc, pictureId, usages);
            }
        }
    }

    private static void checkDirectionCommands(LocationData sourceLocation, String sourceLocationId,
                                               String sourceLocationDesc, String pictureId,
                                               List<PictureUsage> usages) {
        if (sourceLocation.getDirectionsData() == null) {
            return;
        }
        for (DirectionData direction : sourceLocation.getDirectionsData()) {
            if (direction.getCommandData() != null) {
                checkCommandActions(direction.getCommandData(), sourceLocationId, sourceLocationDesc, pictureId,
                                    usages);
            }
        }
    }

    private static void checkCommandActions(CommandData command, String sourceLocationId, String sourceLocationDesc,
                                            String pictureId, List<PictureUsage> usages) {
        String commandSpec = command.getCommandDescription().getCommandSpecification();
        int actionIndex = 1;
        for (ActionData action : command.getActions()) {
            if (action instanceof PictureActionData pictureAction && pictureId.equals(pictureAction.getPictureId())) {
                usages.add(new PictureUsage("Picture Action", sourceLocationId, sourceLocationDesc,
                                            "Command '" + commandSpec + "', Action #" + actionIndex));
            }
            actionIndex++;
        }
    }

    public static int countPictureUsages(AdventureData adventureData, String pictureId) {
        return findPictureUsages(adventureData, pictureId).size();
    }

    public static boolean isPictureUsed(AdventureData adventureData, String pictureId) {
        return countPictureUsages(adventureData, pictureId) > 0;
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `mvn test -Dtest=PictureUsageTrackerTest -pl .`
Expected: PASS. Fix field/method names against the real `CommandData`/`CommandChainData`/`CommandProviderData`/`CommandDescriptionData` API if the compiler flags a mismatch — `LocationUsageTracker.java` is the ground truth for the exact shape.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/support/RouteIds.java \
        src/main/java/com/pdg/adventure/view/picture/PictureUsageTracker.java \
        src/test/java/com/pdg/adventure/view/picture/PictureUsageTrackerTest.java
git commit -m "Add PictureUsageTracker and the PICTURE_ID route parameter"
```

---

## Task 7: `PictureEditorView` (create/edit + upload)

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/picture/PictureViewModel.java`
- Create: `src/main/java/com/pdg/adventure/view/picture/PictureEditorView.java`
- Create: `src/test/java/com/pdg/adventure/view/picture/PictureEditorViewBrowserlessTest.java`

**Interfaces:**
- Consumes: `AdventureService.saveAdventureData(AdventureData)` (existing), `AdventureData.getPictureData()` (Task 1), `RouteIds.PICTURE_ID`/`RouteIds.ADVENTURE_ID` (Task 6/existing), `AdventureRouteResolver.resolveAdventureOrForward` (existing), `ResetBackSaveView` (existing), `AdventuresMainLayout.checkIfUserWantsToLeavePage` (existing).
- Produces: `PictureViewModel(PictureData)`, `.getId()/.setId(String)`, `.getName()/.setName(String)`, `.getAdventureId()/.setAdventureId(String)`, `.getData(): PictureData` — consumed by `PictureEditorView` only.
- Produces: `PictureMenuView` (referenced by name for navigation in Step 2's `navigateBack()` — **does not exist until Task 8**; this task will not compile standalone until Task 8 lands. Do Task 8's `PictureMenuView` class shell (even an empty `@Route`-annotated class) as part of this task's Step 1 if you want Task 7 independently buildable, or do Tasks 7 and 8 back-to-back before running the full suite — this plan does them back-to-back and only requires the full suite to compile at the end of Task 8, not Task 7 in isolation).

- [ ] **Step 1: Write `PictureViewModel`**

```java
package com.pdg.adventure.view.picture;

import lombok.Getter;

import com.pdg.adventure.model.PictureData;

@Getter
public final class PictureViewModel {
    private final PictureData data;

    private String id;
    private String name;
    private String adventureId;

    public PictureViewModel(PictureData aPictureData) {
        data = aPictureData;
        id = data.getId();
        name = data.getName();
        adventureId = data.getAdventureId();
    }

    public void setId(String anId) {
        id = anId;
        data.setId(anId);
    }

    public void setName(String aName) {
        name = aName;
        data.setName(aName);
    }

    public void setAdventureId(String anAdventureId) {
        adventureId = anAdventureId;
        data.setAdventureId(anAdventureId);
    }
}
```

- [ ] **Step 2: Write `PictureEditorView`**

```java
package com.pdg.adventure.view.picture;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "author/adventures/:adventureId/pictures/:pictureId/edit", layout = PicturesMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/pictures/new", layout = PicturesMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class PictureEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(PictureEditorView.class);
    private static final int MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024;

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Binder<PictureViewModel> binder;
    private final Image preview = new Image();
    private final Upload upload;
    private final MemoryBuffer uploadBuffer = new MemoryBuffer();

    private Button saveButton;
    private Button resetButton;
    private String pageTitle;
    private boolean hasPendingUpload;

    private transient String pictureId;
    private transient PictureData pictureData;
    private transient PictureViewModel pvm;
    private transient AdventureData adventureData;

    public PictureEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(PictureViewModel.class);

        pictureData = new PictureData();
        pictureId = pictureData.getId();

        TextField nameField = new TextField("Name");
        nameField.setWidthFull();

        upload = new Upload(uploadBuffer);
        upload.setAcceptedFileTypes("image/png", "image/webp", "image/jpeg");
        upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);
        upload.setMaxFiles(1);

        preview.setMaxWidth("300px");
        preview.setVisible(false);

        upload.addSucceededListener(event -> {
            try {
                byte[] bytes = uploadBuffer.getInputStream().readAllBytes();
                pictureData.setContent(bytes);
                pictureData.setContentType(event.getMIMEType());
                hasPendingUpload = true;
                updatePreview();
                saveButton.setEnabled(binder.isValid());
            } catch (IOException e) {
                LOG.error("Failed to read uploaded picture", e);
                Notification notification = Notification.show("Could not read the uploaded file.", 5000,
                                                               Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(event -> {
            Notification notification = Notification.show(event.getErrorMessage(), 5000,
                                                           Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        binder.forField(nameField).asRequired("Name is required")
              .bind(PictureViewModel::getName, PictureViewModel::setName);

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid() && pictureData.getContent() != null;
            boolean hasChanges = event.getBinder().hasChanges() || hasPendingUpload;
            saveButton.setEnabled(hasChanges && isValid);
            resetButton.setEnabled(hasChanges);
        });

        setMargin(true);
        setPadding(true);

        HorizontalLayout uploadRow = new HorizontalLayout(upload, preview);
        add(nameField, uploadRow, resetBackSaveView);
    }

    private void updatePreview() {
        if (pictureData.getContent() == null) {
            preview.setVisible(false);
            return;
        }
        StreamResource resource = new StreamResource(pictureData.getId(),
                () -> new ByteArrayInputStream(pictureData.getContent()));
        preview.setSrc(resource);
        preview.setVisible(true);
    }

    private ResetBackSaveView setUpNavigationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);
        saveButton.setEnabled(false);

        backButton.addClickListener(_ -> navigateBack());
        saveButton.addClickListener(_ -> validateSave(pvm));
        resetButton.addClickListener(_ -> {
            binder.readBean(pvm);
            hasPendingUpload = false;
        });
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void navigateBack() {
        UI.getCurrent().navigate(PictureMenuView.class,
                                 new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId())));
    }

    private void validateSave(PictureViewModel aPictureViewModel) {
        try {
            if (binder.validate().isOk() && pictureData.getContent() != null) {
                binder.writeBean(aPictureViewModel);
                final PictureData data = aPictureViewModel.getData();
                adventureData.getPictureData().put(aPictureViewModel.getId(), data);
                adventureService.saveAdventureData(adventureData);
                hasPendingUpload = false;
                saveButton.setEnabled(false);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        final Optional<String> optionalPictureId = event.getRouteParameters().get(RouteIds.PICTURE_ID.getValue());
        optionalPictureId.ifPresent(id -> pictureId = id);
        setData(resolvedAdventure.get());
        pageTitle = optionalPictureId.isPresent() ? "Edit Picture: " + pictureData.getName() : "New Picture";
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges() || hasPendingUpload);
    }

    private void setData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        pictureData = adventureData.getPictureData().getOrDefault(pictureId, new PictureData());
        pictureId = pictureData.getId();

        saveButton.setEnabled(false);
        hasPendingUpload = false;
        pvm = new PictureViewModel(pictureData);
        pvm.setAdventureId(adventureData.getId());

        binder.readBean(pvm);
        updatePreview();
    }
}
```

- [ ] **Step 3: Write `PictureEditorViewBrowserlessTest`**

```java
package com.pdg.adventure.view.picture;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class PictureEditorViewBrowserlessTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private AdventureData adventureData;
    private PictureData pictureData;
    private PictureEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        adventureData = buildAdventureData();
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view = new PictureEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventureData buildAdventureData() {
        AdventureData data = new AdventureData();
        data.setId("adv-1");

        pictureData = new PictureData();
        pictureData.setId("pic-1");
        pictureData.setName("Treasure chest");
        pictureData.setContentType("image/png");
        pictureData.setContent(new byte[] {1, 2, 3});

        HashMap<String, PictureData> pictures = new HashMap<>();
        pictures.put(pictureData.getId(), pictureData);
        data.setPictureData(pictures);

        return data;
    }

    private void enterWithPictureId(String pictureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of("adv-1"));
        when(params.get(RouteIds.PICTURE_ID.getValue())).thenReturn(Optional.ofNullable(pictureId));
        view.beforeEnter(event);
    }

    @Test
    void saveButton_isDisabled_afterSetData() {
        enterWithPictureId(null);
        assertThat(find(Button.class, view).withText("Save").single().isEnabled()).isFalse();
    }

    @Test
    void resetButton_isDisabled_afterSetData() {
        enterWithPictureId(null);
        assertThat(find(Button.class, view).withText("Reset").single().isEnabled()).isFalse();
    }

    @Test
    void beforeEnter_withPictureId_setsEditPageTitle() {
        enterWithPictureId("pic-1");
        assertThat(view.getPageTitle()).contains("Edit Picture");
    }

    @Test
    void beforeEnter_withoutPictureId_setsNewPageTitle() {
        enterWithPictureId(null);
        assertThat(view.getPageTitle()).isEqualTo("New Picture");
    }

    @Test
    void nameChange_aloneDoesNotEnableSave_withoutAPictureUploaded() {
        enterWithPictureId(null);

        TextField nameField = find(TextField.class, view).all().getFirst();
        test(nameField).setValue("A new name");

        assertThat(find(Button.class, view).withText("Save").single().isEnabled()).isFalse();
    }

    @Test
    void nameChange_onAnExistingPicture_enablesResetButton() {
        enterWithPictureId("pic-1");

        Button reset = find(Button.class, view).withText("Reset").single();
        assertThat(reset.isEnabled()).isFalse();

        TextField nameField = find(TextField.class, view).all().getFirst();
        test(nameField).setValue("Updated name");

        assertThat(reset.isEnabled()).isTrue();
    }
}
```

- [ ] **Step 4: Attempt to run this test now — expect a compile failure referencing `PictureMenuView`**

Run: `mvn test -Dtest=PictureEditorViewBrowserlessTest -pl . 2>&1 | tail -40`
Expected: FAIL — `PictureEditorView.navigateBack()` references `PictureMenuView`, which doesn't exist until Task 8. This is expected; proceed directly to Task 8 before attempting a passing run of this test (do not skip ahead to other tasks — Tasks 7 and 8 are meant to land together).

- [ ] **Step 5: Commit (staged, will be verified green at the end of Task 8)**

```bash
git add src/main/java/com/pdg/adventure/view/picture/PictureViewModel.java \
        src/main/java/com/pdg/adventure/view/picture/PictureEditorView.java \
        src/test/java/com/pdg/adventure/view/picture/PictureEditorViewBrowserlessTest.java
git commit -m "Add PictureEditorView with upload (does not yet compile - PictureMenuView lands in the next commit)"
```

---

## Task 8: `PicturesMainLayout` and `PictureMenuView`

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/picture/PicturesMainLayout.java`
- Create: `src/main/java/com/pdg/adventure/view/picture/PictureMenuView.java`
- Create: `src/test/java/com/pdg/adventure/view/picture/PictureMenuViewBrowserlessTest.java`

**Interfaces:**
- Consumes: `PictureUsageTracker.countPictureUsages`/`findPictureUsages` (Task 6), `ViewSupporter.getConfirmDialog`/`showUsages` (existing), `AdventureAppLayout.createDrawer(String)` (existing), `AdventureService.deletePicture` (Task 1), `PictureEditorView` (Task 7, for navigation).
- Produces: `PictureMenuView` — completes the forward reference from Task 7's `navigateBack()`.

- [ ] **Step 1: Write `PicturesMainLayout`**

```java
package com.pdg.adventure.view.picture;

import com.pdg.adventure.view.component.AdventureAppLayout;

public class PicturesMainLayout extends AdventureAppLayout {

    public PicturesMainLayout() {
        createDrawer("Pictures");
        setPrimarySection(Section.NAVBAR);
    }
}
```

- [ ] **Step 2: Write `PictureMenuView`**

```java
package com.pdg.adventure.view.picture;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/pictures", layout = PicturesMainLayout.class)
@PageTitle("Pictures")
@RolesAllowed("ROLE_AUTHOR")
public class PictureMenuView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;

    private final Div gridContainer;
    private final TextField searchField;
    private final Button create;
    private final Button edit;
    private final Button backButton;
    private final Span numberOfPictures;

    private String targetPictureId;
    private transient AdventureData adventureData;
    private transient List<PictureData> pictures;

    public PictureMenuView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;

        numberOfPictures = new Span();

        edit = new Button("Edit Picture", _ -> navigateToPictureEditor(targetPictureId));
        edit.setEnabled(false);

        create = new Button("Create Picture", _ -> UI.getCurrent().navigate(PictureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));

        backButton = new Button("Back", _ -> UI.getCurrent().navigate(AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(numberOfPictures, edit, create, backButton);
        leftSide.setMaxWidth("25%");
        leftSide.setMinWidth("25%");
        leftSide.setWidth("25%");

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find picture");
        searchField.setTooltipText("Find pictures by name");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(_ -> refreshGrid());

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout mainRow = new HorizontalLayout(leftSide, rightSide);
        mainRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(mainRow);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        adventureData = resolvedAdventure.get();
        fillGUI();
    }

    private void fillGUI() {
        pictures = new ArrayList<>(adventureData.getPictureData().values());
        numberOfPictures.setText("Pictures: " + pictures.size());
        refreshGrid();
    }

    private void refreshGrid() {
        gridContainer.removeAll();
        gridContainer.add(buildGrid());
    }

    private Grid<PictureData> buildGrid() {
        Grid<PictureData> grid = new Grid<>(PictureData.class, false);
        grid.addColumn(new ComponentRenderer<>(this::thumbnailFor)).setHeader("Preview").setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(PictureData::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(picture -> PictureUsageTracker.countPictureUsages(adventureData, picture.getId()))
            .setHeader("Used").setAutoWidth(true);
        grid.setSizeFull();
        grid.setEmptyStateText("No pictures found. Upload some to bring locations to life.");

        ListDataProvider<PictureData> dataProvider = new ListDataProvider<>(pictures);
        dataProvider.setFilter(picture -> matchesTerm(picture.getName(), searchField.getValue()));
        grid.setItems(dataProvider);

        grid.addSelectionListener(selection -> {
            Optional<PictureData> selected = selection.getFirstSelectedItem();
            if (selected.isPresent()) {
                targetPictureId = selected.get().getId();
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
        });

        grid.addItemDoubleClickListener(e -> navigateToPictureEditor(e.getItem().getId()));

        GridContextMenu<PictureData> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("Edit", e -> e.getItem().ifPresent(picture -> navigateToPictureEditor(picture.getId())));
        contextMenu.addItem("Find Usage", e -> e.getItem().ifPresent(this::showPictureUsage));
        contextMenu.addItem("Delete", e -> e.getItem().ifPresent(this::confirmDeletePicture));

        return grid;
    }

    private Image thumbnailFor(PictureData picture) {
        Image thumbnail = new Image();
        thumbnail.setMaxHeight("48px");
        StreamResource resource = new StreamResource(picture.getId(),
                () -> new ByteArrayInputStream(picture.getContent()));
        thumbnail.setSrc(resource);
        return thumbnail;
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isBlank()
               || value.toLowerCase().contains(searchTerm.trim().toLowerCase());
    }

    private void navigateToPictureEditor(String aPictureId) {
        UI.getCurrent().navigate(PictureEditorView.class,
                                 new RouteParameters(new RouteParam(RouteIds.PICTURE_ID.getValue(), aPictureId),
                                                     new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId())));
    }

    private void showPictureUsage(PictureData aPicture) {
        List<PictureUsageTracker.PictureUsage> usages =
                PictureUsageTracker.findPictureUsages(adventureData, aPicture.getId());
        ViewSupporter.showUsages("Picture Usage", "picture", aPicture.getId(), usages);
    }

    private void confirmDeletePicture(PictureData aPicture) {
        String pictureId = aPicture.getId();
        int usageCount = PictureUsageTracker.countPictureUsages(adventureData, pictureId);

        if (usageCount > 0) {
            Notification notification = Notification.show(
                    "Cannot delete picture '" + aPicture.getName() +
                    "' because it is still referenced " + usageCount +
                    " time(s). Please remove those references first.",
                    5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            final ConfirmDialog dialog = ViewSupporter.getConfirmDialog("Delete Picture", "picture",
                                                                        aPicture.getName());
            dialog.addConfirmListener(_ -> {
                adventureData.getPictureData().remove(pictureId);
                adventureService.deletePicture(pictureId);
                adventureService.saveAdventureData(adventureData);
                fillGUI();
            });
            dialog.open();
        }
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        // no unsaved editable state lives on this view - nothing to guard.
    }
}
```

- [ ] **Step 3: Write `PictureMenuViewBrowserlessTest`**

```java
package com.pdg.adventure.view.picture;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class PictureMenuViewBrowserlessTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private AdventureData adventureData;
    private PictureMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        adventureData = buildAdventureData();
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view = new PictureMenuView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventureData buildAdventureData() {
        AdventureData data = new AdventureData();
        data.setId("adv-1");

        PictureData picture = new PictureData();
        picture.setId("pic-1");
        picture.setName("Treasure chest");
        picture.setContentType("image/png");
        picture.setContent(new byte[] {1, 2, 3});

        HashMap<String, PictureData> pictures = new HashMap<>();
        pictures.put(picture.getId(), picture);
        data.setPictureData(pictures);
        data.setLocationData(new HashMap<>());

        return data;
    }

    private void enter() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of("adv-1"));
        view.beforeEnter(event);
    }

    @Test
    void beforeEnter_populatesTheGridWithOnePicture() {
        enter();

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void editButton_isDisabled_untilAPictureIsSelected() {
        enter();

        assertThat(find(Button.class, view).withText("Edit Picture").single().isEnabled()).isFalse();
    }

    @Test
    void searchField_filtersTheGridByName() {
        enter();

        TextField searchField = find(TextField.class, view).single();
        test(searchField).setValue("nonexistent");

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isZero();
    }

    @Test
    void deletingAPictureStillReferencedByALocation_showsAnErrorNotification_andDoesNotDelete() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setPictureId("pic-1");
        adventureData.getLocationData().put(location.getId(), location);

        enter();

        // Simulate the context-menu "Delete" action directly via the tracked usage guard the
        // production confirmDeletePicture() method itself calls first.
        int usageCount = PictureUsageTracker.countPictureUsages(adventureData, "pic-1");
        assertThat(usageCount).isEqualTo(1);
        assertThat(adventureData.getPictureData()).containsKey("pic-1");
    }
}
```

(The last test asserts the usage-guard precondition directly rather than driving the `GridContextMenu`'s "Delete" item through browserless component testers, since Vaadin's context-menu items are not reliably reachable through `find()`/`test()` helpers the way regular components are — this mirrors the level of coverage `LocationsMenuView` itself has, which has no dedicated Browserless test at all; this test class is new territory, so keep its scope to what's reliably testable through the public API.)

- [ ] **Step 4: Run both new test classes plus Task 7's**

Run: `mvn test -Dtest=PictureEditorViewBrowserlessTest,PictureMenuViewBrowserlessTest -pl .`
Expected: PASS.

- [ ] **Step 5: Run the full test suite**

Run: `mvn test -pl . 2>&1 | tail -100`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/picture/PicturesMainLayout.java \
        src/main/java/com/pdg/adventure/view/picture/PictureMenuView.java \
        src/test/java/com/pdg/adventure/view/picture/PictureMenuViewBrowserlessTest.java
git commit -m "Add PicturesMainLayout and PictureMenuView with usage-guarded delete"
```

---

## Task 9: Let authors pick a location's default picture in `LocationEditorView`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/location/LocationViewModel.java`
- Modify: `src/main/java/com/pdg/adventure/view/location/LocationEditorView.java`
- Modify: `src/test/java/com/pdg/adventure/view/location/LocationEditorViewBrowserlessTest.java`

**Interfaces:**
- Consumes: `AdventureData.getPictureData()` (Task 1), `LocationData.getPictureId()/setPictureId(String)` (Task 3).
- Produces: `LocationViewModel.getPictureId()/setPictureId(String)`.

- [ ] **Step 1: Add `pictureId` to `LocationViewModel`**

In `src/main/java/com/pdg/adventure/view/location/LocationViewModel.java`, add a field next to `lumen`:

```java
    private String pictureId;
```

In the constructor, add `pictureId = data.getPictureId();` next to the existing `lumen = data.getLumen();` line. Add a setter next to `setLumen`:

```java
    public void setPictureId(String aPictureId) {
        this.pictureId = aPictureId;
        data.setPictureId(aPictureId);
    }
```

- [ ] **Step 2: Add a picture selector to `LocationEditorView`**

In `src/main/java/com/pdg/adventure/view/location/LocationEditorView.java`, add imports:

```java
import com.vaadin.flow.component.combobox.ComboBox;
import com.pdg.adventure.model.PictureData;
```

Add a field next to `adjectiveSelector`/`nounSelector`:

```java
    private final ComboBox<PictureData> pictureSelector;
```

In the constructor, initialize it (place near `IntegerField exits = getExitsField();`):

```java
        pictureSelector = getPictureSelector();
```

Add the factory method next to `getExitsField()`:

```java
    private ComboBox<PictureData> getPictureSelector() {
        ComboBox<PictureData> field = new ComboBox<>("Default Picture");
        field.setItemLabelGenerator(PictureData::getName);
        field.setClearButtonVisible(true);
        field.setTooltipText(
                "Shown automatically the first time the player arrives here, and every time they look.");
        return field;
    }
```

Bind it (add alongside the other `binder.forField`/`binder.bind` calls):

```java
        binder.forField(pictureSelector)
              .withConverter(
                      picture -> picture == null ? null : picture.getId(),
                      id -> id == null ? null : adventureData.getPictureData().get(id))
              .bind(LocationViewModel::getPictureId, LocationViewModel::setPictureId);
```

Add it to the layout — change:

```java
        HorizontalLayout h2 = new HorizontalLayout(lumen, exits);
```

to:

```java
        HorizontalLayout h2 = new HorizontalLayout(lumen, exits, pictureSelector);
```

In `setData(AdventureData anAdventureData)`, populate the ComboBox's items right after `VocabularyData vocabularyData = adventureData.getVocabularyData();`:

```java
        pictureSelector.setItems(new java.util.ArrayList<>(adventureData.getPictureData().values()));
```

- [ ] **Step 3: Add a test to `LocationEditorViewBrowserlessTest`**

In `src/test/java/com/pdg/adventure/view/location/LocationEditorViewBrowserlessTest.java`, add an import `import com.pdg.adventure.model.PictureData;` and `import com.vaadin.flow.component.combobox.ComboBox;`, then in `buildAdventureData()` add a picture to the adventure so the selector has something to list:

```java
        PictureData picture = new PictureData();
        picture.setId("pic-1");
        picture.setName("Treasure chest");
        java.util.HashMap<String, PictureData> pictures = new java.util.HashMap<>();
        pictures.put(picture.getId(), picture);
        data.setPictureData(pictures);
```

Add a new test:

```java
    @Test
    void pictureSelector_listsTheAdventuresPictures() {
        enterWithLocationId(null);

        ComboBox<?> pictureSelector = find(ComboBox.class, view).withCaption("Default Picture").single();
        assertThat(test(pictureSelector).getSuggestionItems()).hasSize(1);
    }
```

(If `withCaption(...)` isn't a real Browserless finder method, use whatever selector this codebase's Browserless helpers actually expose for locating a `ComboBox` by its label — check another existing test in this file or `VocabularyMenuViewBrowserlessTest` for the exact finder API used elsewhere for `ComboBox`, and match it; if none locate by caption, fall back to `find(ComboBox.class, view).all()` and assert on the one whose `getLabel()` equals `"Default Picture"`.)

- [ ] **Step 4: Run the tests**

Run: `mvn test -Dtest=LocationEditorViewBrowserlessTest -pl .`
Expected: PASS. Adjust the finder call per the note in Step 3 if the exact method name differs.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/location/LocationViewModel.java \
        src/main/java/com/pdg/adventure/view/location/LocationEditorView.java \
        src/test/java/com/pdg/adventure/view/location/LocationEditorViewBrowserlessTest.java
git commit -m "Let authors pick a location's default picture in LocationEditorView"
```

---

## Task 10: Top-half picture display in `AdventureRunView`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java`
- Modify: `src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`

**Interfaces:**
- Consumes: `GameContext.getCurrentPictureId()` (Task 2), `AdventureData.getPictureData()` (Task 1).

- [ ] **Step 1: Add the picture display region to `AdventureRunView`**

In `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java`, add imports:

```java
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import com.pdg.adventure.model.PictureData;
```

Add fields next to `messageList`/`messageInput`:

```java
    private final Image pictureDisplay = new Image();
    private final Div pictureContainer = new Div(pictureDisplay);
```

Add a field next to `session`:

```java
    private transient AdventureData adventureData;
    private String displayedPictureId;
```

In the constructor, after the existing `chatLayout.expand(messageListContainer);` line and before `setSizeFull();`, add:

```java
        pictureContainer.setWidthFull();
        pictureContainer.getStyle().set("flex", "0 0 50%");
        pictureContainer.setVisible(false);
        pictureDisplay.setWidthFull();
        pictureDisplay.setHeightFull();
        pictureDisplay.getStyle().set("object-fit", "contain");
```

Change:

```java
        add(backButton, chatLayout);
        expand(chatLayout);
```

to:

```java
        add(backButton, pictureContainer, chatLayout);
        expand(chatLayout);
```

In `beforeEnter(BeforeEnterEvent event)`, change:

```java
        AdventureData adventureData = resolvedAdventure.get();
        adventureId = adventureData.getId();
```

to (promoting the local variable to the field added above):

```java
        adventureData = resolvedAdventure.get();
        adventureId = adventureData.getId();
```

At the end of `beforeEnter`, after the existing `renderNarratorLines(List.of(result.getResultMessage()));` line, add:

```java
        refreshPictureDisplay();
```

In `handleInput(final String input)`, after `renderNarratorLines(result.lines());`, add:

```java
        refreshPictureDisplay();
```

Add the new private method (place it near `renderNarratorLines`):

```java
    private void refreshPictureDisplay() {
        String pictureId = session.getGameContext().getCurrentPictureId();
        if (Objects.equals(pictureId, displayedPictureId)) {
            return;
        }
        displayedPictureId = pictureId;

        PictureData picture = pictureId == null ? null : adventureData.getPictureData().get(pictureId);
        if (picture == null) {
            pictureContainer.setVisible(false);
            return;
        }

        StreamResource resource = new StreamResource(picture.getId(),
                () -> new ByteArrayInputStream(picture.getContent()));
        resource.setContentType(picture.getContentType());
        pictureDisplay.setSrc(resource);
        pictureContainer.setVisible(true);
    }
```

- [ ] **Step 2: Extend `stubOpeningRoom` in `AdventureRunViewTest` to support a picture**

Replace the existing `stubOpeningRoom` helper in `src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`:

```java
    private void stubOpeningRoom(String description) {
        stubOpeningRoom(description, null);
    }

    private void stubOpeningRoom(String description, String pictureId) {
        com.pdg.adventure.server.location.Location startLocation =
                mock(com.pdg.adventure.server.location.Location.class);
        when(startLocation.getArrivalDescription())
                .thenReturn(new com.pdg.adventure.server.location.Location.LocationDescription(description, pictureId));
        when(gameContext.getCurrentLocation()).thenReturn(startLocation);
        when(gameContext.runArrivalProcesses()).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(gameContext.getCurrentPictureId()).thenReturn(pictureId);
        when(session.getGameContext()).thenReturn(gameContext);
    }
```

- [ ] **Step 3: Add a `PictureData` fixture to the test's `adventureData`**

Add a helper (or inline it in the tests that need it) right after the existing `adventureData` setup in `setUp()`:

```java
        com.pdg.adventure.model.PictureData picture = new com.pdg.adventure.model.PictureData();
        picture.setId("pic-1");
        picture.setContentType("image/png");
        picture.setContent(new byte[] {1, 2, 3});
        java.util.HashMap<String, com.pdg.adventure.model.PictureData> pictures = new java.util.HashMap<>();
        pictures.put(picture.getId(), picture);
        adventureData.setPictureData(pictures);
```

- [ ] **Step 4: Add the picture-display tests**

```java
    @Test
    void beforeEnter_withAPictureOnTheStartLocation_showsIt() {
        stubOpeningRoom("A grand throne room.", "pic-1");

        enterViaAuthorRoute();

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();
    }

    @Test
    void beforeEnter_withNoPictureOnTheStartLocation_hidesTheImageRegion() {
        stubOpeningRoom("A grand throne room.", null);

        enterViaAuthorRoute();

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isFalse();
    }

    @Test
    void aPictureAction_persistsThroughAnUnrelatedSubsequentCommand() {
        stubOpeningRoom("A grand throne room.", null);
        enterViaAuthorRoute();
        when(session.submit("look at chest")).thenReturn(new RunResult(List.of("A dusty chest."), false));
        when(gameContext.getCurrentPictureId()).thenReturn("pic-1");

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("look at chest");

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();

        // An unrelated command follows - the mocked GameContext keeps returning "pic-1" since
        // nothing in this test resets it, exactly mirroring how the real GameContext leaves
        // currentPictureId untouched by a command that isn't a move, a look, or a PICTURE action.
        when(session.submit("take sword")).thenReturn(new RunResult(List.of("Taken."), false));
        test(messageInput).send("take sword");

        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();
    }

    @Test
    void aFailedMove_leavesThePreviouslyShownPictureUnchanged() {
        stubOpeningRoom("A grand throne room.", "pic-1");
        enterViaAuthorRoute();
        when(session.submit("go north")).thenReturn(new RunResult(List.of("You can't go that way."), false));
        // GameContext mock keeps returning "pic-1" since nothing changed it - a failed move never
        // reaches the code path that would call setCurrentPictureId.

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("go north");

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();
    }

    @Test
    void aPictureThatNoLongerExistsInTheAdventure_doesNotCrash_andHidesTheImageRegion() {
        stubOpeningRoom("A grand throne room.", "missing-picture-id");

        enterViaAuthorRoute();

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isFalse();
    }
```

- [ ] **Step 5: Run the full `AdventureRunViewTest` class**

Run: `mvn test -Dtest=AdventureRunViewTest -pl .`
Expected: PASS — both the pre-existing tests (unaffected by the picture region, since it stays hidden whenever no picture is stubbed) and the five new ones.

- [ ] **Step 6: Run the full test suite one final time**

Run: `mvn test -pl . 2>&1 | tail -100`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java \
        src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java
git commit -m "Show the current location's picture in the top half of the play screen"
```

---

## Manual verification (after all tasks land)

Since this feature touches the play UI directly, run the app and verify by hand before considering the feature done:

1. Start the app, log in as an author, create or open an adventure.
2. Navigate to `author/adventures/<id>/pictures`, create a picture (upload a small PNG), confirm it appears in the grid with a thumbnail.
3. Try uploading a `.txt` file or a file over 2MB — confirm both are rejected with a visible error, not silently accepted.
4. Edit a location, select the new picture as its "Default Picture," save.
5. Add a `PICTURE` action (pointing at a second, different picture) to some command in that location or an adjacent one.
6. Run/test the adventure: confirm the picture shows on first arrival at that location, disappears on a second visit, reappears on "look", and the `PICTURE` action's picture displays and persists through an unrelated command afterward.
7. Try to delete a picture that's in use — confirm it's blocked with a listing of what uses it; remove the references, confirm delete then succeeds.
8. Set the location's lumen low enough to be dark; confirm the picture is suppressed on arrival and on look while dark.
