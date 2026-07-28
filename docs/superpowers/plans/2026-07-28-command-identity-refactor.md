# Command Identity Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop item-owned commands from duplicating their owning item's adjective/noun, and stop persistence/UI identity from being derived from that (or any) mutable text — fixing both the orphaning bug and a newly-found chain-collapse bug in one coherent change.

**Architecture:** (1) Auto-generated pickup commands store an empty adjective/noun instead of copying the item's; `GenericCommandProvider`'s matching gains the same empty-is-wildcard rule for noun that verb/adjective already have, so matching still works via the item resolution that already happened upstream. (2) `CommandProviderData.availableCommands` is re-keyed from a derived `"verb|adjective|noun"` string to each chain's own pre-existing, previously-unused `CommandChainData.id`; grouping ("does a new command join an existing chain?") becomes a description-match search over chain values, not a map key lookup, so a chain's identity survives edits to its trigger. (3) `CommandEditorView`/`CommandsMenuView` route and select commands by that same chain id instead of the spec string.

**Tech Stack:** Java 25, Spring Boot, Vaadin Flow, MongoDB (Spring Data), JUnit 5 + Mockito + AssertJ, `com.vaadin.browserless.BrowserlessTest`.

## Global Constraints

- AssertJ only for assertions (`assertThat`) — no `assertEquals`.
- No new `@Mock`s where a real object (as the existing tests already do — `GenericContainer`, `Location`, `Item`, `DescriptionProvider`) is simpler; follow each file's existing test style.
- `GenericCommandProvider.getMatchingCommandChain` must remain the single runtime dispatch path — do not introduce a second matching algorithm.
- `CommandData`/`CommandChainData` equality is id-based (`BasicData`, `@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)`) — rely on this for `List.contains`/`remove`, don't add new equality logic.
- Every task must leave `mvn test` fully green before moving to the next task.
- Commit after each task with its own commit (no squashing across tasks).

---

### Task 1: Wildcard-match an empty stored noun

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/parser/GenericCommandProvider.java:78-81` (the noun check inside `getMatchingCommandChain`)
- Test: `src/test/java/com/pdg/adventure/server/parser/GenericCommandProviderTest.java`

**Interfaces:**
- Consumes: nothing new.
- Produces: `GenericCommandProvider.getMatchingCommandChain(CommandDescription)` now also matches an entry whose **stored** noun is `VocabularyData.EMPTY_STRING`, regardless of the query's noun (mirroring the existing verb/adjective wildcard rule). A non-empty stored noun still requires an exact match, unchanged.

- [ ] **Step 1: Write the failing test**

Add to `GenericCommandProviderTest`:

```java
@Test
void emptyStoredNounMatchesAnyQueriedNoun() {
    GenericCommandProvider provider = new GenericCommandProvider();
    Action noop = new Action() {
        @Override
        public ExecutionResult execute() {
            return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        }

        @Override
        public String getActionName() {
            return "noop";
        }
    };
    GenericCommandDescription itemScoped = new GenericCommandDescription("get", "", "");
    provider.addCommand(new GenericCommand(itemScoped, noop));

    List<CommandChain> matches = provider.getMatchingCommandChain(
            new GenericCommandDescription("get", "", "suit"));

    assertThat(matches).hasSize(1);
}

@Test
void nonEmptyStoredNounStillRequiresExactMatch() {
    GenericCommandProvider provider = new GenericCommandProvider();
    Action noop = new Action() {
        @Override
        public ExecutionResult execute() {
            return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        }

        @Override
        public String getActionName() {
            return "noop";
        }
    };
    GenericCommandDescription doorScoped = new GenericCommandDescription("open", "", "door");
    provider.addCommand(new GenericCommand(doorScoped, noop));

    List<CommandChain> matches = provider.getMatchingCommandChain(
            new GenericCommandDescription("open", "", "window"));

    assertThat(matches).isEmpty();
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=GenericCommandProviderTest`
Expected: `emptyStoredNounMatchesAnyQueriedNoun` FAILS (`matches` is empty, not size 1). `nonEmptyStoredNounStillRequiresExactMatch` already passes — that's fine, it documents behavior this task must not break.

- [ ] **Step 3: Implement the wildcard rule**

In `GenericCommandProvider.getMatchingCommandChain`, replace:

```java
            // noun must match exactly
            if (!Objects.equals(itemNoun, noun)) {
                continue;
            }
```

with:

```java
            // noun must match exactly, unless the stored command has no noun of its own -
            // that's an item-scoped command (its trigger's noun is implicit in which item it
            // lives on, already resolved upstream by ItemIdentifier before we get here).
            boolean nounMatches = Objects.equals(itemNoun, noun) || VocabularyData.EMPTY_STRING.equals(itemNoun);
            if (!nounMatches) {
                continue;
            }
```

- [ ] **Step 4: Run to verify it passes**

Run: `mvn test -Dtest=GenericCommandProviderTest`
Expected: both new tests PASS.

- [ ] **Step 5: Full suite**

Run: `mvn test`
Expected: all green. This change touches shared matching code used by every `Thing` (locations, items, directions via their commands, workflow does **not** go through this class — it uses `Workflow`'s own `TreeMap`) — a full green run here is the actual proof the wildcard addition is safe, not just the two new tests.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/parser/GenericCommandProvider.java src/test/java/com/pdg/adventure/server/parser/GenericCommandProviderTest.java
git commit -m "feat: treat an empty stored noun as a wildcard in command matching"
```

---

### Task 2: Stop copying the item's adjective/noun into auto-generated pickup commands

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/item/ItemEditorView.java:364-368` (`getRawCommandData`)
- Test: `src/test/java/com/pdg/adventure/view/item/ItemEditorViewTest.java`

**Interfaces:**
- Consumes: Task 1's wildcard matching (so the resulting empty-noun commands still work at runtime — verified by the full suite in Task 1, not re-verified here).
- Produces: every command `ItemEditorView.createPickupCommands` generates now has `commandDescription.getAdjective()` / `.getNoun()` equal to `VocabularyData.EMPTY_STRING`-backed empty `Word`s (in practice: no adjective/noun `Word` reference at all — see step 3), regardless of the owning item's own adjective/noun, past or present.

- [ ] **Step 1: Write the failing test**

Add to `ItemEditorViewTest` (reuses the `take`/`drop` words and `createPickupCommands` reflection seam already established by `regeneratingPickupCommandsAfterAdjectiveChange_removesStaleNoAdjectiveCommands`):

```java
@Test
void generatedPickupCommands_neverCopyTheItemsAdjectiveOrNoun() throws Exception {
    // given: an item that already has an adjective when pickup commands are first generated
    itemData.getDescriptionData().setAdjective(golden);
    locationData.getItemContainerData().getItems().add(itemData);

    Word take = new Word("take", Word.Type.VERB);
    Word drop = new Word("drop", Word.Type.VERB);
    vocabularyData.setTakeWord(take);
    vocabularyData.setDropWord(drop);

    view = new ItemEditorView(adventureService, itemService, accessService);
    view.beforeEnter(eventWithParams(
            new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
            new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
            new RouteParam(RouteIds.ITEM_ID.getValue(), itemData.getId())));

    Method createPickupCommands = ItemEditorView.class.getDeclaredMethod(
            "createPickupCommands", Word.class, Word.class, ItemData.class);
    createPickupCommands.setAccessible(true);

    // when
    createPickupCommands.invoke(view, take, drop, itemData);

    // then: none of the generated commands' descriptions carry the item's adjective/noun
    for (CommandChainData chain : itemData.getCommandProviderData().getAvailableCommands().values()) {
        for (CommandData command : chain.getCommands()) {
            assertThat(command.getCommandDescription().getAdjective()).isNull();
            assertThat(command.getCommandDescription().getNoun()).isNull();
        }
    }
}
```

Add the `golden` field where `setUp()` currently declares its local `Word golden = new Word("golden", Word.Type.ADJECTIVE);` — promote it from a `setUp()`-local variable to a field (`private Word golden;`, assigned in `setUp()`) since this test needs it and the existing `setUp()` currently only uses it locally to build `itemData`'s description.

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=ItemEditorViewTest`
Expected: FAILS — `command.getCommandDescription().getAdjective()` is the `golden` word, and `.getNoun()` is `sword`, not `null`.

- [ ] **Step 3: Implement**

In `ItemEditorView.getRawCommandData`, replace:

```java
    private CommandData getRawCommandData(final Word aTakeVerb, final ItemData anItem) {
        DescriptionData itemDescription = anItem.getDescriptionData();
        CommandDescriptionData commandDescription = new CommandDescriptionData(aTakeVerb,
                                                                               itemDescription.getAdjective(),
                                                                               itemDescription.getNoun());
        return new CommandData(commandDescription);
    }
```

with:

```java
    // Item-scoped commands (take/drop) don't restate the item's adjective/noun: the command
    // already lives on this specific item, and GenericCommandProvider treats an empty stored
    // noun as a wildcard, so it matches regardless of the item's current description. This is
    // what stops the command's identity from silently drifting out of sync when an author
    // edits the item afterwards.
    private CommandData getRawCommandData(final Word aTakeVerb, final ItemData anItem) {
        CommandDescriptionData commandDescription = new CommandDescriptionData(aTakeVerb, null, null);
        return new CommandData(commandDescription);
    }
```

- [ ] **Step 4: Run to verify it passes**

Run: `mvn test -Dtest=ItemEditorViewTest`
Expected: all tests in this class PASS, including the Task-2-added test and the earlier `regeneratingPickupCommandsAfterAdjectiveChange_removesStaleNoAdjectiveCommands` (which now proves the same thing a simpler way — both stay, they check different things: staleness-on-change vs. never-copied-in-the-first-place).

- [ ] **Step 5: Full suite**

Run: `mvn test`
Expected: all green.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/item/ItemEditorView.java src/test/java/com/pdg/adventure/view/item/ItemEditorViewTest.java
git commit -m "feat: stop duplicating item adjective/noun into auto-generated pickup commands"
```

---

### Task 3: `CommandProviderData` groups and keys by stable chain id

**Files:**
- Modify: `src/main/java/com/pdg/adventure/model/CommandProviderData.java`
- Test: create `src/test/java/com/pdg/adventure/model/CommandProviderDataTest.java`

**Interfaces:**
- Consumes: `CommandChainData.getId()` (already exists via `BasicData`, already a ULID assigned on construction — nothing to add there).
- Produces:
  - `CommandProviderData.get(CommandDescriptionData aKey): CommandChainData` — **same signature**, new behavior: finds an existing chain whose first command's specification matches `aKey`, or creates and registers a new one keyed by *its own* id.
  - `CommandProviderData.add(CommandData aCommand): void` — unchanged signature/behavior from the caller's point of view (still "join or create a chain for this command's description"); callers in `ItemEditorView`/`WorkflowEditorView` need no changes.
  - New: `CommandProviderData.findChainIdContaining(CommandData aCommand): Optional<String>` — used by Task 5/6 to resolve a specific command's chain id for UI routing.

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/com/pdg/adventure/model/CommandProviderDataTest.java`:

```java
package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;

class CommandProviderDataTest {

    @Test
    void addingTwoCommandsWithTheSameSpecJoinsOneChainKeyedByStableId() {
        CommandProviderData provider = new CommandProviderData();
        Word verb = new Word("open", Word.Type.VERB);
        Word noun = new Word("door", Word.Type.NOUN);

        CommandData first = new CommandData(new CommandDescriptionData(verb, null, noun));
        first.setActions(java.util.List.of(new MessageActionData()));
        CommandData second = new CommandData(new CommandDescriptionData(verb, null, noun));
        second.setActions(java.util.List.of(new MessageActionData()));

        provider.add(first);
        provider.add(second);

        assertThat(provider.getAvailableCommands()).hasSize(1);
        String chainId = provider.getAvailableCommands().keySet().iterator().next();
        assertThat(chainId).isNotEqualTo("open||door"); // not a derived spec string
        assertThat(provider.getAvailableCommands().get(chainId).getCommands())
                .containsExactly(first, second);
    }

    @Test
    void chainIdSurvivesEditingTheCommandsDescription() {
        CommandProviderData provider = new CommandProviderData();
        Word open = new Word("open", Word.Type.VERB);
        Word close = new Word("close", Word.Type.VERB);
        Word door = new Word("door", Word.Type.NOUN);

        CommandData command = new CommandData(new CommandDescriptionData(open, null, door));
        provider.add(command);
        String chainId = provider.getAvailableCommands().keySet().iterator().next();

        // author renames the trigger verb - the chain keeps its id, no map-key move needed
        command.setCommandDescription(new CommandDescriptionData(close, null, door));

        assertThat(provider.getAvailableCommands()).containsKey(chainId);
        assertThat(provider.getAvailableCommands().get(chainId).getCommands()).containsExactly(command);
    }

    @Test
    void findChainIdContaining_locatesTheChainHoldingAGivenCommand() {
        CommandProviderData provider = new CommandProviderData();
        Word verb = new Word("get", Word.Type.VERB);
        CommandData command = new CommandData(new CommandDescriptionData(verb, null, null));
        provider.add(command);
        String expectedChainId = provider.getAvailableCommands().keySet().iterator().next();

        Optional<String> found = provider.findChainIdContaining(command);

        assertThat(found).contains(expectedChainId);
    }

    @Test
    void findChainIdContaining_isEmptyForAnUnknownCommand() {
        CommandProviderData provider = new CommandProviderData();
        CommandData unknown = new CommandData();

        assertThat(provider.findChainIdContaining(unknown)).isEmpty();
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=CommandProviderDataTest`
Expected: compile failure (`findChainIdContaining` doesn't exist yet) or, once stubbed to compile, failures on the "not a derived spec string" / "chain id survives" assertions against the current `get(CommandDescriptionData)` implementation (which keys by `getCommandSpecification()` and would move the entry to a new key `"close||door"` on rename, breaking the second test).

- [ ] **Step 3: Implement**

Replace `CommandProviderData.get`/`add` and add `findChainIdContaining`:

```java
    public CommandChainData get(CommandDescriptionData aKey) {
        String targetSpec = aKey.getCommandSpecification();
        for (CommandChainData chain : availableCommands.values()) {
            if (!chain.getCommands().isEmpty()
                    && chain.getCommands().getFirst().getCommandDescription().getCommandSpecification()
                            .equals(targetSpec)) {
                return chain;
            }
        }
        CommandChainData result = new CommandChainData();
        availableCommands.put(result.getId(), result);
        return result;
    }

    public void add(CommandData aCommand) {
        CommandDescriptionData commandDescription = aCommand.getCommandDescription();
        CommandChainData result = get(commandDescription);
        result.getCommands().add(aCommand);
    }

    public Optional<String> findChainIdContaining(CommandData aCommand) {
        for (Map.Entry<String, CommandChainData> entry : availableCommands.entrySet()) {
            if (entry.getValue().getCommands().contains(aCommand)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
```

Add `import java.util.Optional;` to the top of the file.

- [ ] **Step 4: Run to verify it passes**

Run: `mvn test -Dtest=CommandProviderDataTest`
Expected: all four tests PASS.

- [ ] **Step 5: Full suite**

Run: `mvn test`
Expected: all green — this is the check that nothing else (mappers, `ItemEditorView`, `WorkflowEditorView`, `CommandsMenuView`) broke from the behavior change in `get`/`add`, since none of their call sites change signature.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/model/CommandProviderData.java src/test/java/com/pdg/adventure/model/CommandProviderDataTest.java
git commit -m "feat: key CommandProviderData chains by stable id instead of derived spec string"
```

---

### Task 4: Simplify `CommandProviderMapper` to match

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/mapper/CommandProviderMapper.java`
- Test: create `src/test/java/com/pdg/adventure/server/mapper/CommandProviderMapperTest.java`

**Interfaces:**
- Consumes: `CommandChainMapper.mapToBO/mapToDO` (unchanged — already round-trips `id`, per `CommandChainMapper.java:32,42`), `CommandDescriptionMapper.mapToBO` (unchanged).
- Produces: `CommandProviderMapper.mapToBO(CommandProviderData): GenericCommandProvider` and `.mapToDO(GenericCommandProvider): CommandProviderData` — same signatures. `mapToBO` now derives each chain's `CommandDescription` from the chain's own first mapped command (via the properly-vocabulary-resolved `CommandMapper`/`CommandDescriptionMapper` path) instead of parsing the DO map's string key. `mapToDO` keys the output map by `CommandChainData.getId()`. Because `mapToBO` never reads the DO map's *key* at all (only its *values*), loading a legacy document (still keyed by an old `"verb|adjective|noun"` string, from before this refactor) round-trips correctly and is re-keyed to the id-based form the next time it's saved — no separate migration script needed.

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/pdg/adventure/server/mapper/CommandProviderMapperTest.java`:

```java
package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.parser.GenericCommandProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class CommandProviderMapperTest {

    @Test
    void mapToBO_ignoresTheLegacySpecStringKey_readsOnlyChainValues() {
        // given: a legacy document, as if written before this refactor - keyed by spec string
        Vocabulary vocabulary = new Vocabulary();
        Word take = vocabulary.createNewWord("take", Word.Type.VERB);
        Word sword = vocabulary.createNewWord("sword", Word.Type.NOUN);

        MapperSupporter mapperSupporter = mock(MapperSupporter.class);
        when(mapperSupporter.getVocabulary()).thenReturn(vocabulary);

        CommandDescriptionMapper descriptionMapper = new CommandDescriptionMapper(mapperSupporter);
        CommandMapper commandMapper = new CommandMapper(mapperSupporter, descriptionMapper);
        CommandChainMapper chainMapper = new CommandChainMapper(mapperSupporter, commandMapper);
        CommandProviderMapper sut = new CommandProviderMapper(mapperSupporter, chainMapper, descriptionMapper);

        CommandData command = new CommandData(new CommandDescriptionData(take, null, sword));
        command.setActions(List.of(new MessageActionData()));
        CommandChainData chainData = new CommandChainData();
        chainData.getCommands().add(command);

        CommandProviderData legacy = new CommandProviderData();
        legacy.getAvailableCommands().put("take||sword", chainData); // legacy-shaped key

        // when
        GenericCommandProvider bo = sut.mapToBO(legacy);

        // then: exactly one chain, correctly matchable by verb+noun regardless of the legacy key
        assertThat(bo.getAvailableCommands()).hasSize(1);
        CommandChain mappedChain = bo.getAvailableCommands().values().iterator().next();
        assertThat(mappedChain.getCommands()).hasSize(1);
    }

    @Test
    void roundTrip_reKeysToTheChainsOwnStableId_notADerivedString() {
        Vocabulary vocabulary = new Vocabulary();
        Word take = vocabulary.createNewWord("take", Word.Type.VERB);
        Word sword = vocabulary.createNewWord("sword", Word.Type.NOUN);

        MapperSupporter mapperSupporter = mock(MapperSupporter.class);
        when(mapperSupporter.getVocabulary()).thenReturn(vocabulary);

        CommandDescriptionMapper descriptionMapper = new CommandDescriptionMapper(mapperSupporter);
        CommandMapper commandMapper = new CommandMapper(mapperSupporter, descriptionMapper);
        CommandChainMapper chainMapper = new CommandChainMapper(mapperSupporter, commandMapper);
        CommandProviderMapper sut = new CommandProviderMapper(mapperSupporter, chainMapper, descriptionMapper);

        CommandData command = new CommandData(new CommandDescriptionData(take, null, sword));
        command.setActions(List.of(new MessageActionData()));
        CommandChainData chainData = new CommandChainData();
        chainData.getCommands().add(command);
        String originalChainId = chainData.getId();

        CommandProviderData legacy = new CommandProviderData();
        legacy.getAvailableCommands().put("take||sword", chainData);

        GenericCommandProvider bo = sut.mapToBO(legacy);
        CommandProviderData roundTripped = sut.mapToDO(bo);

        assertThat(roundTripped.getAvailableCommands()).containsKey(originalChainId);
        assertThat(roundTripped.getAvailableCommands()).doesNotContainKey("take||sword");
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=CommandProviderMapperTest`
Expected: FAILS on the current implementation — `mapToBO` throws (it calls `new CommandDescriptionData("take||sword")` then `commandDescriptionMapper.mapToBO(...)`, which is a *different*, already-broken path from the one under test, but more importantly `mapToDO` currently re-derives the key from `commandDescriptionMapper.mapToDO(entry.getKey())` — i.e. `"take||sword"` again, not the chain's id — so `doesNotContainKey("take||sword")` fails.

- [ ] **Step 3: Implement**

Replace `CommandProviderMapper.mapToBO`/`mapToDO`:

```java
    @Override
    public GenericCommandProvider mapToBO(CommandProviderData aData) {
        GenericCommandProvider result = new GenericCommandProvider();
        result.setId(aData.getId());
        for (CommandChainData chainData : aData.getAvailableCommands().values()) {
            final CommandChain commandChain = commandChainMapper.mapToBO(chainData);
            final CommandDescription description = commandChain.getCommands().isEmpty()
                    ? new GenericCommandDescription(VocabularyData.EMPTY_STRING)
                    : commandChain.getCommands().getFirst().getDescription();
            result.getAvailableCommands().put(description, commandChain);
        }
        return result;
    }

    @Override
    public CommandProviderData mapToDO(GenericCommandProvider aData) {
        CommandProviderData result = new CommandProviderData();
        result.setId(aData.getId());
        for (CommandChain chain : aData.getAvailableCommands().values()) {
            final CommandChainData chainData = commandChainMapper.mapToDO(chain);
            result.getAvailableCommands().put(chainData.getId(), chainData);
        }
        return result;
    }
```

Add imports: `com.pdg.adventure.model.VocabularyData`, `com.pdg.adventure.server.parser.GenericCommandDescription`. The `commandDescriptionMapper` field/constructor parameter stays (still used elsewhere in this class's public contract and by nothing else internally after this change — leave it; removing the parameter would be a breaking constructor-signature change for a mapper that's `@AutoRegisterMapper`-discovered, out of scope for this task).

- [ ] **Step 4: Run to verify it passes**

Run: `mvn test -Dtest=CommandProviderMapperTest`
Expected: both tests PASS.

- [ ] **Step 5: Full suite**

Run: `mvn test`
Expected: all green.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/mapper/CommandProviderMapper.java src/test/java/com/pdg/adventure/server/mapper/CommandProviderMapperTest.java
git commit -m "feat: derive command provider BO/DO mapping from chain contents, not the map key"
```

---

### Task 5: `CommandEditorView` — route by chain id, fix chain collapse on save

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/CommandEditorView.java` (`swivelTheSaveButton`, `getEditingCommandData`, `populateCommandChain`, `beforeEnter`/`commandId` handling)
- Test: `src/test/java/com/pdg/adventure/view/command/CommandEditorViewTest.java`, `src/test/java/com/pdg/adventure/view/command/CommandEditorViewRoutingTest.java`

**Interfaces:**
- Consumes: `CommandProviderData.get(CommandDescriptionData)`/`.add(CommandData)`/`.findChainIdContaining(CommandData)` from Task 3.
- Produces: `commandId` now holds a **chain id** (opaque, from `CommandChainData.getId()`), not a spec string. Route template unchanged (`.../commands/:commandId/edit`) — only the *value* that flows through it changes. `swivelTheSaveButton()` no longer deletes a whole chain when a command's trigger changes; it updates the command in place within its already-known chain.

- [ ] **Step 1: Write the failing test**

Add to `CommandEditorViewTest` (adapt to however that file currently constructs a populated view + `commandProviderData` with an existing multi-command chain — follow the existing test's setup pattern for loading a command by id):

```java
@Test
void editingOneCommandInAMultiCommandChain_keepsItsSiblings() {
    // given: two commands sharing one chain (mirrors CommandsMenuViewTest's "open||cage" setup)
    Word open = vocabularyWord("open", Word.Type.VERB);
    Word cage = vocabularyWord("cage", Word.Type.NOUN);
    Word close = vocabularyWord("close", Word.Type.VERB);

    CommandData first = new CommandData(new CommandDescriptionData(open, null, cage));
    first.setActions(List.of(new MessageActionData()));
    CommandData second = new CommandData(new CommandDescriptionData(open, null, cage));
    second.setActions(List.of(new MessageActionData()));
    locationData.getCommandProviderData().add(first);
    locationData.getCommandProviderData().add(second);
    String chainId = locationData.getCommandProviderData().findChainIdContaining(first).orElseThrow();

    view = new CommandEditorView(adventureService, itemService, accessService);
    view.beforeEnter(eventWithParams(
            new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
            new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
            new RouteParam(RouteIds.COMMAND_ID.getValue(), chainId)));
    selectCommandInChain(view, first); // existing test helper that loads `first` into the editor, if present - otherwise drive it the way this test class already drives chain selection elsewhere

    // when: the verb is changed and saved
    setVerbInEditor(view, close); // existing test helper for driving the verb picker, per this class's established pattern
    triggerSave(view);

    // then: both commands are still in the SAME chain
    CommandChainData chain = locationData.getCommandProviderData().getAvailableCommands().get(chainId);
    assertThat(chain.getCommands()).hasSize(2);
    assertThat(chain.getCommands()).contains(second);
}
```

> This test's exact helper calls (`selectCommandInChain`, `setVerbInEditor`, `triggerSave`, `vocabularyWord`) must be adapted to whatever `CommandEditorViewTest` already uses to drive the verb `VocabularyPicker` and the Save button — read the existing tests in that file first and reuse its established interaction pattern (it already exercises picker selection and save for other cases). Do not invent a new interaction style for this one test.

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=CommandEditorViewTest`
Expected: FAILS — `chain.getCommands()` has 1 entry (or the lookup by `chainId` returns `null` because the old code deleted that map entry), not 2 containing both `first` and `second`.

- [ ] **Step 3: Implement**

In `CommandEditorView`, replace `swivelTheSaveButton()` and `getEditingCommandData()`:

```java
    private CommandData swivelTheSaveButton() {
        final CommandDescriptionData updatedCommandDescription = cvm.getData();

        CommandData command = (commandData != null) ? commandData : new CommandData();
        command.setCommandDescription(updatedCommandDescription);
        preconditionActionEditor.saveToCommand(command);

        CommandChainData chainData = (commandId != null && !commandId.isEmpty())
                ? commandProviderData.getAvailableCommands().get(commandId)
                : null;

        if (chainData != null) {
            // editing a command whose chain we already know: stays there, in place - the
            // chain's id never changes just because the trigger description did.
            if (!chainData.getCommands().contains(command)) {
                chainData.getCommands().add(command);
            }
        } else {
            // brand-new command: joins an existing chain with a matching trigger, or starts one.
            commandProviderData.add(command);
        }

        commandId = commandProviderData.findChainIdContaining(command).orElse(commandId);
        commandData = command;
        return command;
    }
```

Delete `getEditingCommandData(boolean, CommandDescriptionData)` entirely — its two branches (reuse `commandData` vs. `new CommandData()`) are now the two-line ternary at the top of `swivelTheSaveButton`. Remove its now-unused `@NonNull` import if nothing else in the file uses `org.jspecify.annotations.NonNull`.

In `populateCommandChain()`, `commandChainGrid`'s selection listener, and anywhere else `currentCommandChain = commandProviderData.getAvailableCommands().get(commandId)` is used to load the chain for display — this lookup is unchanged in *shape* (still `Map<String,CommandChainData>.get(commandId)`), it just now correctly finds the chain because `commandId` is the chain's real, stable key instead of a derived string that may not match after a prior edit.

In `beforeEnter`, the block that currently does:

```java
        final Optional<String> optionalCommandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        if (optionalCommandId.isPresent()) {
            commandId = AdventureRouteResolver.decodeRouteParam(optionalCommandId.get());
            pageTitle = "Edit Command: " + ViewSupporter.formatDescription(new CommandDescriptionData(commandId));
        } else {
            pageTitle = "New Command";
        }
```

The `pageTitle` line for an existing command can no longer derive its description from `commandId` (it's an opaque id now, not a parseable spec). Change it to derive the title from the loaded chain's first command instead, after the chain is resolved in `populate()`/`populateCommandChain()` — set `pageTitle` there (once `currentCommandChain` is known) rather than in `beforeEnter` before the chain lookup has happened:

```java
        final Optional<String> optionalCommandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        commandId = optionalCommandId.map(AdventureRouteResolver::decodeRouteParam).orElse(null);
```

and in `populateCommandChain()`, after `currentCommandChain` is resolved (non-null, non-empty), set:

```java
        pageTitle = (currentCommandChain != null && !currentCommandChain.getCommands().isEmpty())
                ? "Edit Command: " + ViewSupporter.formatDescription(
                        currentCommandChain.getCommands().getFirst().getCommandDescription())
                : "New Command";
```

placed at the point where `currentCommandChain` is already known to be resolved (or absent) for both the found-chain and no-chain branches of that method.

- [ ] **Step 4: Run to verify it passes**

Run: `mvn test -Dtest=CommandEditorViewTest`
Expected: PASS, including the new chain-collapse regression test.

- [ ] **Step 5: Fix the routing tests**

`CommandEditorViewRoutingTest` currently feeds literal specs (`"go|north|"`, `"jump||sea"`) as `RouteParam(COMMAND_ID, ...)` values and likely asserts on `commandId`/`pageTitle` shaped around those strings. Update its fixtures to seed a `CommandProviderData` via `.add(CommandData)` (as in Task 3/4's tests) and use the resulting `findChainIdContaining(...)` value as the route param instead of a literal spec string. Re-run and fix each assertion that assumed the old string shape (e.g. any `pageTitle` assertion built from the raw `commandId`).

Run: `mvn test -Dtest=CommandEditorViewRoutingTest`
Expected: PASS after fixture updates.

- [ ] **Step 6: Full suite**

Run: `mvn test`
Expected: all green.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/CommandEditorView.java src/test/java/com/pdg/adventure/view/command/CommandEditorViewTest.java src/test/java/com/pdg/adventure/view/command/CommandEditorViewRoutingTest.java
git commit -m "fix: route/identify commands by stable chain id, not a derived spec string

Fixes a chain-collapse bug where editing one command's verb/adjective/noun
silently discarded any sibling commands sharing its old chain."
```

---

### Task 6: `CommandsMenuView` — navigate/select/delete by chain id

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java` (double-click listener, `CommandContextMenu`'s Edit/Delete, `navigateToCommandEditor`)
- Test: `src/test/java/com/pdg/adventure/view/command/CommandsMenuViewTest.java`, `src/test/java/com/pdg/adventure/view/command/CommandsMenuViewRoutingTest.java`

**Interfaces:**
- Consumes: `CommandProviderData.findChainIdContaining(CommandData)` from Task 3.
- Produces: `navigateToCommandEditor` now receives a chain id (looked up per-row via `findChainIdContaining`), not `command.getCommandDescription().getCommandSpecification()`. Grid rows are still one-per-`CommandData` (unchanged), just resolved to a chain id at the point of navigation/deletion instead of a spec string.

- [ ] **Step 1: Write the failing test**

`CommandsMenuViewTest.java:158-185` already sets up the "two commands, one spec, one chain" scenario this refactor is protecting — extend it (or add alongside it) to assert deletion targets the right chain by id even after a description change, and that double-click navigation passes a chain id, not a spec string:

```java
@Test
void doubleClickNavigatesUsingTheRowsChainId_notASpecString() {
    // given: two commands sharing one chain (existing fixture shape from the "open||cage" test)
    CommandData first = new CommandData(new CommandDescriptionData(openWord, null, cageWord));
    first.setActions(List.of(new MessageActionData()));
    CommandData second = new CommandData(new CommandDescriptionData(openWord, null, cageWord));
    second.setActions(List.of(new MessageActionData()));
    locationData.getCommandProviderData().add(first);
    locationData.getCommandProviderData().add(second);
    String expectedChainId = locationData.getCommandProviderData().findChainIdContaining(first).orElseThrow();

    view = new CommandsMenuView(adventureService, itemService, accessService);
    view.beforeEnter(eventWithParams(
            new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
            new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId())));

    // when/then: exercised via whatever this test class's existing pattern is for asserting
    // navigation targets (e.g. a captured RouteParameters, or a UI navigation spy) - follow
    // the pattern CommandsMenuViewRoutingTest already established for this view, don't invent
    // a new one. The assertion must show the navigated commandId equals expectedChainId, and
    // is NOT "open||cage".
}
```

> Fill in the `when`/`then` using this test class's and `CommandsMenuViewRoutingTest`'s existing navigation-capture mechanism (read both files first) rather than guessing at one — this plan can't predict the exact spy/capture shape already in place.

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=CommandsMenuViewTest,CommandsMenuViewRoutingTest`
Expected: FAILS on the new assertion (navigated id currently equals `"open||cage"`).

- [ ] **Step 3: Implement**

In `CommandsMenuView`, replace the double-click listener:

```java
        grid.addItemDoubleClickListener(e ->
                navigateToCommandEditor(e.getItem().getCommandDescription().getCommandSpecification()));
```

with:

```java
        grid.addItemDoubleClickListener(e ->
                commandProviderData.findChainIdContaining(e.getItem())
                                    .ifPresent(this::navigateToCommandEditor));
```

In `CommandContextMenu`'s `"Edit"` handler, same substitution:

```java
            addItem("Edit", e -> e.getItem().ifPresent(command ->
                    commandProviderData.findChainIdContaining(command)
                                       .ifPresent(CommandsMenuView.this::navigateToCommandEditor)));
```

In the `"Delete"` handler, replace the spec-string-keyed removal:

```java
            addItem("Delete", e -> e.getItem().ifPresent(command -> {
                String commandSpec = command.getCommandDescription().getCommandSpecification();
                CommandChainData chain = commandProviderData.getAvailableCommands().get(commandSpec);
                if (chain != null) {
                    chain.getCommands().remove(command);
                    if (chain.getCommands().isEmpty()) {
                        commandProviderData.getAvailableCommands().remove(commandSpec);
                    }
                }
                gridListDataView.removeItem(command);
                ...
            }));
```

with:

```java
            addItem("Delete", e -> e.getItem().ifPresent(command -> {
                commandProviderData.findChainIdContaining(command).ifPresent(chainId -> {
                    CommandChainData chain = commandProviderData.getAvailableCommands().get(chainId);
                    chain.getCommands().remove(command);
                    if (chain.getCommands().isEmpty()) {
                        commandProviderData.getAvailableCommands().remove(chainId);
                    }
                });
                gridListDataView.removeItem(command);
                if (itemData != null) {
                    itemService.saveItem(itemData);
                } else {
                    adventureService.saveLocationData(locationData);
                }
                gridListDataView.refreshAll();
            }));
```

- [ ] **Step 4: Run to verify it passes**

Run: `mvn test -Dtest=CommandsMenuViewTest,CommandsMenuViewRoutingTest`
Expected: PASS. Fix any other assertions in these two files that still expect a spec-string-shaped navigated id (per the earlier research: `CommandsMenuViewRoutingTest.java:78-86` feeds literal specs into route params — update those fixtures the same way as Task 5 Step 5).

- [ ] **Step 5: Full suite**

Run: `mvn test`
Expected: all green.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java src/test/java/com/pdg/adventure/view/command/CommandsMenuViewTest.java src/test/java/com/pdg/adventure/view/command/CommandsMenuViewRoutingTest.java
git commit -m "fix: navigate/delete commands by chain id instead of a derived spec string"
```

---

### Task 7: Delete confirmed-dead spec-string code

**Files:**
- Delete: `src/main/java/com/pdg/adventure/view/command/GridUnbufferedInlineEditor.java`
- Delete: `src/main/java/com/pdg/adventure/view/command/SimpleCommandDescription.java`
- Delete: their test files (confirm exact names via `grep -rl GridUnbufferedInlineEditor src/test` / `grep -rl SimpleCommandDescription src/test` before deleting — the design doc names them `GridUnbufferedInlineEditorTest`/`SimpleCommandDescriptionTest`, confirm those are the only references)

**Interfaces:** none — by definition, nothing in `src/main` calls either class (confirmed during the design's research phase; re-confirm with a fresh grep immediately before deleting, since Tasks 1-6 may have touched nearby files).

- [ ] **Step 1: Confirm still dead**

Run: `grep -rn "GridUnbufferedInlineEditor\|SimpleCommandDescription" src/main/java`
Expected: no hits outside the two files themselves.

- [ ] **Step 2: Delete**

```bash
git rm src/main/java/com/pdg/adventure/view/command/GridUnbufferedInlineEditor.java
git rm src/main/java/com/pdg/adventure/view/command/SimpleCommandDescription.java
git rm src/test/java/com/pdg/adventure/view/command/GridUnbufferedInlineEditorTest.java
git rm src/test/java/com/pdg/adventure/view/command/SimpleCommandDescriptionTest.java
```

- [ ] **Step 3: Full suite**

Run: `mvn test`
Expected: all green (fewer tests than before by exactly the deleted files' counts, no failures).

- [ ] **Step 4: Commit**

```bash
git commit -m "chore: delete GridUnbufferedInlineEditor/SimpleCommandDescription (dead spec-string parsers)"
```

---

### Task 8: Full verification and demo-adventure check

**Files:** none modified — this task is verification only.

**Interfaces:** none new.

- [ ] **Step 1: Full suite, one more time, clean**

Run: `mvn clean test`
Expected: all green, zero skips beyond the pre-existing 3.

- [ ] **Step 2: Demo adventure self-heal check**

The neoprene suit item (`01kt1n50059gxqmq7q03rt5b0x` in adventure `01kt1mckzwhnqa44w0f9xsf4zd`) currently holds `get|neoprene|suit`/`drop|neoprene|suit` (already cleaned of the *orphaned* pair by the earlier bugfix, but still spec-string-keyed and still carrying the item's adjective/noun in its `commandDescription`, since that data predates this refactor). Confirm it self-heals the next time it's loaded and saved through the app (e.g. open it in `ItemEditorView` and click Save with no changes, or run the adventure through `AdventureService.findAdventureById`/`saveAdventureData` in a scratch check) — after that round-trip, `mongosh` should show the item's `commandProviderData.availableCommands` keyed by ULID-shaped ids, and each command's `commandDescription` should have no `adjective`/`noun` field. Do **not** hand-edit the live document for this — the point of Task 4's lazy re-keying is that a normal load/save does it; use that path to prove it actually does, not a manual `mongosh` mutation.

- [ ] **Step 3: Adversarial review**

Dispatch an independent review pass (Workflow, `code-reviewer`-equivalent findings) over the full diff from Task 1 through Task 7 against this plan and the design doc (`docs/superpowers/specs/2026-07-28-command-identity-refactor-design.md`), specifically checking:
- Every call site that used to read `getCommandSpecification()` as an *identity* (not just for display) was updated — re-grep `getCommandSpecification\(\)` across `src/main/java/com/pdg/adventure/view/` and confirm each remaining hit is either `LocationUsageTracker`/`ItemUsageTracker` (display-only, correctly left alone) or a place this plan explicitly updated.
- No test was weakened (e.g. an assertion loosened or deleted) to make it pass, rather than the production code fixed.
- `Workflow`'s `TreeMap<CommandDescription,Command>` ordering (untouched by this plan, confirmed out of scope) still behaves correctly under Task 1's wildcard change — it doesn't go through `GenericCommandProvider.getMatchingCommandChain`, but confirm no shared helper was accidentally changed underneath it.

- [ ] **Step 4: Final commit if the adversarial review finds anything**

Fix findings task-by-task (repeat that task's RED→GREEN cycle for the fix, don't bundle unrelated fixes into one commit), then re-run `mvn test` and confirm green.
