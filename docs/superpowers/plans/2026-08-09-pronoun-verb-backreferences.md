# Parser back-references ("it" / inferred verb) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a player write `"take sword and shield and wear it"` and have it mean `take sword`, `take shield`, `wear shield` — a conjunction-joined sub-command with no verb of its own infers the last verb used, and the new pronoun "it" resolves to the last noun+adjective mentioned (persisting across turns within a session).

**Architecture:** `Parser` (one instance per `AdventureRunSession`, already reused across every `submit()`) gains three instance fields — `lastVerb`, `lastNoun`, `lastAdjective` — updated each time a sub-command finishes parsing. A new `Word.Type.PRONOUN`, seeded as the built-in word "it" (same treatment as "and"/"then"), resolves through those fields instead of literal text. An unresolvable "it" (no noun ever mentioned) throws a new `UnresolvedReferenceException`, caught by `GameLoop` next to the existing `QuitException`/`ReloadAdventureException` catches.

**Tech Stack:** Java 21+, JUnit 5, AssertJ, Mockito — matches the rest of `server/`.

## Global Constraints

- Spec: `docs/superpowers/specs/2026-08-09-pronoun-verb-backreferences-design.md` — this plan implements that design exactly; if anything here conflicts with it, the spec wins and the plan is wrong.
- The unresolved-pronoun message must be exactly `"I don't know what 'it' refers to."` (approved wording from design review).
- No author-facing CRUD or vocabulary-menu visibility for built-in words — that was a separate feature, designed and then explicitly reverted by the user in a prior session. Do not resurrect it.
- Follow the existing "and"/"then" precedent throughout: `PRONOUN` is a reserved, system-seeded `Word.Type`, seeded identically in both `MiniAdventure.createSpecialWords()` and `AdventureRunSessionFactory.registerBaseVerbs()`, and excluded from `WordEditorDialogue`'s type picker.
- TDD: every behavioral change gets a failing test first. A few confirmation tests in this plan are expected to pass immediately because an earlier task's change already provides the behavior — those are called out explicitly as such; they are not exempt from being written, just from driving new production code.
- Run `mvn test` (or the module-scoped equivalent already used in this repo) after every task; do not move to the next task with a red suite.

---

### Task 1: Parser remembers the last verb and infers it for a bare-noun sub-command

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/parser/Parser.java`
- Test: `src/test/java/com/pdg/adventure/server/parser/ParserTest.java`

**Interfaces:**
- Consumes: existing `Parser(Vocabulary)`, `CommandSequence`, `GenericCommandDescription`, `SimpleSentence` (all already in `Parser.java`, unchanged shapes).
- Produces: a new private `Parser` instance field `lastVerb` and a new private method `GenericCommandDescription closeSentence(SimpleSentence aSentence)`, both consumed internally by `Parser.handle()`. `lastNoun`/`lastAdjective` fields are added in this task too (needed by `closeSentence`'s bookkeeping) but stay unused by anything else until Task 2.

- [ ] **Step 1: Write the failing test — bare noun sub-command infers the prior verb**

Add to `ParserTest.java`, alongside the other `handle_*` tests:

```java
    @Test
    void handle_bareNounSecondSubCommand_infersVerbFromFirstSubCommand() {
        // given
        Parser parser = new Parser(vocabularyWithTakeDropSwordAndShield());

        // when
        CommandSequence sequence = parser.handle("take sword and shield");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("shield");
    }
```

Add this helper next to the existing `vocabularyWithTakeSwordAndKillOgre()` helper (do not modify that one — existing tests depend on its exact word list):

```java
    private static Vocabulary vocabularyWithTakeDropSwordAndShield() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("drop", Word.Type.VERB);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("shield", Word.Type.NOUN);
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        return vocabulary;
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ParserTest#handle_bareNounSecondSubCommand_infersVerbFromFirstSubCommand test`
Expected: FAIL — `sequence.commands().get(1).getVerb()` is `""`, not `"take"` (today "shield" alone has no verb and none is inferred).

- [ ] **Step 3: Implement verb inference and state tracking**

In `Parser.java`, add the three fields right after the `vocabulary` field (around line 17):

```java
    private final Vocabulary vocabulary;
    private String lastVerb = VocabularyData.EMPTY_STRING;
    private String lastNoun = VocabularyData.EMPTY_STRING;
    private String lastAdjective = VocabularyData.EMPTY_STRING;
```

Replace both `commands.add(toDescription(currentSentence));` call sites inside `handle()` (the one inside the `if (isSeparator)` branch, and the one after the scanning loop) with `commands.add(closeSentence(currentSentence));`.

Add the new method next to `toDescription`:

```java
    // Closes one sub-command: infers a missing verb from the last one seen, builds the
    // GenericCommandDescription, then updates the back-reference state from what was actually
    // parsed - regardless of whether GameLoop later succeeds in executing it, since Parser has
    // no visibility into execution outcomes.
    private GenericCommandDescription closeSentence(SimpleSentence aSentence) {
        if (aSentence.getVerb().isEmpty() && !aSentence.getNoun().isEmpty() && !lastVerb.isEmpty()) {
            aSentence.setVerb(lastVerb);
        }
        GenericCommandDescription description = toDescription(aSentence);
        if (!description.getVerb().isEmpty()) {
            lastVerb = description.getVerb();
        }
        if (!description.getNoun().isEmpty()) {
            lastNoun = description.getNoun();
            lastAdjective = description.getAdjective();
        }
        return description;
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ParserTest test`
Expected: PASS — all `ParserTest` tests green, including the new one.

- [ ] **Step 5: Add the companion non-regression test — explicit verb is never overridden**

Add to `ParserTest.java`:

```java
    @Test
    void handle_explicitVerbInSecondSubCommand_isNotOverriddenByInference() {
        // given
        Parser parser = new Parser(vocabularyWithTakeDropSwordAndShield());

        // when
        CommandSequence sequence = parser.handle("take sword and drop shield");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(0).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("drop");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("shield");
    }
```

Run: `mvn -Dtest=ParserTest test`
Expected: PASS immediately — this isn't new behavior (an explicit verb was never touched by anything before this task either), but it's the guard that proves Step 3's `if` condition is correctly gated on an *empty* verb, not unconditional. Worth locking in now, next to the feature it protects.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/parser/Parser.java src/test/java/com/pdg/adventure/server/parser/ParserTest.java
git commit -m "Parser infers a bare sub-command's verb from the prior sub-command"
```

---

### Task 2: "it" pronoun resolves to the last noun+adjective, or fails clearly

**Files:**
- Modify: `src/main/java/com/pdg/adventure/model/Word.java`
- Create: `src/main/java/com/pdg/adventure/server/exception/UnresolvedReferenceException.java`
- Modify: `src/main/java/com/pdg/adventure/server/parser/Parser.java`
- Test: `src/test/java/com/pdg/adventure/server/parser/ParserTest.java`

**Interfaces:**
- Consumes: `Parser.lastVerb`/`lastNoun`/`lastAdjective`/`closeSentence()` from Task 1.
- Produces: `Word.Type.PRONOUN` (consumed by Task 4's seeding and picker-exclusion changes); `UnresolvedReferenceException(String message)` (consumed by Task 3's `GameLoop` catch block).

- [ ] **Step 1: Add the PRONOUN constant**

This is pure scaffolding (an enum constant carries no behavior of its own to red/green against) — needed before the test below will even compile. In `Word.java`, change:

```java
    public enum Type {
        VERB,
        NOUN,
        ADJECTIVE,
        CONJUNCTION
    }
```

to:

```java
    public enum Type {
        VERB,
        NOUN,
        ADJECTIVE,
        CONJUNCTION,
        PRONOUN
    }
```

- [ ] **Step 2: Create the exception class**

Same shape as the existing `ReloadAdventureException` in the same package:

```java
package com.pdg.adventure.server.exception;

public class UnresolvedReferenceException extends RuntimeException {
    public UnresolvedReferenceException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3: Write the failing test — "it" with no antecedent throws**

Add to `ParserTest.java`:

```java
    @Test
    void handle_it_withNoAntecedent_throwsUnresolvedReferenceException() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());

        // when / then
        assertThatThrownBy(() -> parser.handle("wear it"))
                .isInstanceOf(UnresolvedReferenceException.class)
                .hasMessage("I don't know what 'it' refers to.");
    }
```

Add the import (alongside the existing `assertThat` static import):

```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

And the import for the exception:

```java
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
```

Add this helper next to `vocabularyWithTakeDropSwordAndShield()`:

```java
    private static Vocabulary vocabularyWithBackReferenceWords() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.createNewWord("take", Word.Type.VERB);
        vocabulary.createNewWord("wear", Word.Type.VERB);
        vocabulary.createNewWord("golden", Word.Type.ADJECTIVE);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("shield", Word.Type.NOUN);
        vocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        vocabulary.createNewWord("it", Word.Type.PRONOUN);
        return vocabulary;
    }
```

- [ ] **Step 4: Run test to verify it fails**

Run: `mvn -Dtest=ParserTest#handle_it_withNoAntecedent_throwsUnresolvedReferenceException test`
Expected: FAIL — today `populate()`'s `switch` has no `PRONOUN` case, so it falls to `default -> throw new IllegalArgumentException("Unknown word type PRONOUN")`. Wrong exception type — confirms the test is exercising the right gap.

- [ ] **Step 5: Implement the PRONOUN case**

In `Parser.java`, add the import:

```java
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
```

Change `populate()`'s switch:

```java
    private void populate(SimpleSentence aSentence, Word aWord) {
        switch (aWord.getType()) {
            case NOUN -> aSentence.setNoun(aWord.getText());
            case VERB -> aSentence.setVerb(aWord.getText());
            case ADJECTIVE -> aSentence.setAdjective(aWord.getText());
            case PRONOUN -> {
                if (lastNoun.isEmpty()) {
                    throw new UnresolvedReferenceException(
                            "I don't know what '" + aWord.getText() + "' refers to.");
                }
                aSentence.setNoun(lastNoun);
                aSentence.setAdjective(lastAdjective);
            }
            default -> throw new IllegalArgumentException("Unknown word type " + aWord.getType());
        }
    }
```

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn -Dtest=ParserTest test`
Expected: PASS.

- [ ] **Step 7: Add the worked-example test**

```java
    @Test
    void handle_it_resolvesToTheLastMentionedNoun_matchingTheWorkedExample() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());

        // when
        CommandSequence sequence = parser.handle("take sword and shield and wear it");

        // then
        assertThat(sequence.commands()).hasSize(3);
        assertThat(sequence.commands().get(0).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(0).getNoun()).isEqualTo("sword");
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("take");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("shield");
        assertThat(sequence.commands().get(2).getVerb()).isEqualTo("wear");
        assertThat(sequence.commands().get(2).getNoun()).isEqualTo("shield");
    }
```

Run: `mvn -Dtest=ParserTest test`
Expected: PASS immediately — Task 1's verb inference and this task's pronoun resolution compose without any further code changes; this test locks in that composition (it is the acceptance test straight from the design doc).

- [ ] **Step 8: Add the adjective-pairing test**

```java
    @Test
    void handle_it_carriesTheAdjectiveOfTheLastMentionedNoun() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());

        // when
        CommandSequence sequence = parser.handle("take golden sword and wear it");

        // then
        assertThat(sequence.commands()).hasSize(2);
        assertThat(sequence.commands().get(1).getVerb()).isEqualTo("wear");
        assertThat(sequence.commands().get(1).getAdjective()).isEqualTo("golden");
        assertThat(sequence.commands().get(1).getNoun()).isEqualTo("sword");
    }
```

Run: `mvn -Dtest=ParserTest test`
Expected: PASS immediately — proves "it" carries noun+adjective as a pair (a bare noun-only antecedent, e.g. "shield" in Step 7's test, correctly leaves `lastAdjective` empty; an adjective-qualified antecedent, e.g. "golden sword" here, correctly carries it forward).

- [ ] **Step 9: Add the cross-turn persistence test**

```java
    @Test
    void handle_it_resolvesAcrossSeparateHandleCalls() {
        // given
        Parser parser = new Parser(vocabularyWithBackReferenceWords());
        parser.handle("take sword");

        // when
        CommandSequence sequence = parser.handle("wear it");

        // then
        assertThat(sequence.commands()).hasSize(1);
        assertThat(sequence.commands().getFirst().getVerb()).isEqualTo("wear");
        assertThat(sequence.commands().getFirst().getNoun()).isEqualTo("sword");
    }
```

Run: `mvn -Dtest=ParserTest test`
Expected: PASS immediately — `lastNoun`/`lastAdjective`/`lastVerb` are plain instance fields on `Parser`, so state naturally survives across separate `handle()` calls on the same instance. This is the test that pins that contract down explicitly, since `AdventureRunSessionFactory` relies on exactly one `Parser` instance living for a whole session (verified in Task 4).

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/pdg/adventure/model/Word.java src/main/java/com/pdg/adventure/server/exception/UnresolvedReferenceException.java src/main/java/com/pdg/adventure/server/parser/Parser.java src/test/java/com/pdg/adventure/server/parser/ParserTest.java
git commit -m "Add PRONOUN word type: 'it' resolves to the last mentioned noun+adjective"
```

---

### Task 3: GameLoop reports the specific message for an unresolved "it"

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/engine/GameLoop.java`
- Test: `src/test/java/com/pdg/adventure/server/engine/GameLoopTest.java`

**Interfaces:**
- Consumes: `UnresolvedReferenceException` (Task 2), `Parser`/`GameLoop` unchanged constructors.
- Produces: nothing new consumed by later tasks — this is the end-to-end integration point.

- [ ] **Step 1: Add "it" to the shared test vocabulary**

In `GameLoopTest.java`'s `setUp()`, add one line alongside the existing `vocabulary.createNewWord(...)` calls:

```java
        vocabulary.createNewWord("it", Word.Type.PRONOUN);
```

- [ ] **Step 2: Write the failing test — unresolved "it" tells the specific message**

```java
    @Test
    void it_withNoAntecedentInTheSession_tellsASpecificMessage() {
        GameLoop.CommandOutcome outcome = gameLoop.processCommand("take it");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("I don't know what 'it' refers to.");
    }
```

- [ ] **Step 3: Run test to verify it fails**

Run: `mvn -Dtest=GameLoopTest#it_withNoAntecedentInTheSession_tellsASpecificMessage test`
Expected: FAIL — `UnresolvedReferenceException` isn't caught by any of `processCommand`'s existing `catch` blocks, so it falls through to `catch (RuntimeException anException)`, which logs and returns `CommandOutcome.ERROR` (not `CONTINUE`) and never calls `gameContext.tell(...)` — `told` stays empty.

- [ ] **Step 4: Implement the catch block**

In `GameLoop.java`, add the import:

```java
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
```

In `processCommand`, add a new `catch` clause before the generic `catch (RuntimeException anException)` (order matters — a more specific catch must come first):

```java
        } catch (QuitException anException) {
            gameContext.tell(anException.getMessage());
            return CommandOutcome.QUIT;
        } catch (UnresolvedReferenceException anException) {
            gameContext.tell(anException.getMessage());
            return CommandOutcome.CONTINUE;
        } catch (ReloadAdventureException e) {
            throw e;
        } catch (RuntimeException anException) {
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn -Dtest=GameLoopTest test`
Expected: PASS — full `GameLoopTest` suite green, including the new test.

- [ ] **Step 6: Add the verb-inference-through-GameLoop test**

```java
    @Test
    void and_verbInference_bareNounSecondSubCommandUsesFirstSubCommandsVerb() {
        vocabulary.createNewWord("examine", Word.Type.VERB);
        vocabulary.createNewWord("sword", Word.Type.NOUN);
        vocabulary.createNewWord("shield", Word.Type.NOUN);
        gameContext.getCurrentLocation().addCommand(new GenericCommand(
                new GenericCommandDescription("examine", "sword"),
                new MessageAction("A sharp sword.", new MessagesHolder())));
        gameContext.getCurrentLocation().addCommand(new GenericCommand(
                new GenericCommandDescription("examine", "shield"),
                new MessageAction("A sturdy shield.", new MessagesHolder())));

        GameLoop.CommandOutcome outcome = gameLoop.processCommand("examine sword and shield");

        assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("A sharp sword.").contains("A sturdy shield.");
    }
```

Run: `mvn -Dtest=GameLoopTest test`
Expected: PASS immediately — Task 1's Parser change already provides this; this test locks in the full-stack contract.

- [ ] **Step 7: Add the demo-adventure-motivated regression test**

This mirrors the real bug report that validated the design: a bare `"wear"` (no noun) can never match a location command whose own noun is `"suit"` — only a fully-resolved `"wear suit"` can. `"it"` must still carry the noun forward even though the preceding `"take suit"` itself fails (there is no command wired for `"take"` in this fixture) — proving Parser tracks what was *said*, not whether GameLoop went on to execute it successfully.

```java
    @Test
    void it_resolvesAcrossSeparateTurns_evenIfThePrecedingSubCommandFailed() {
        vocabulary.createNewWord("wear", Word.Type.VERB);
        gameContext.getCurrentLocation().addCommand(new GenericCommand(
                new GenericCommandDescription("wear", "suit"),
                new MessageAction("You put on the suit.", new MessagesHolder())));

        GameLoop.CommandOutcome firstOutcome = gameLoop.processCommand("take suit");
        GameLoop.CommandOutcome secondOutcome = gameLoop.processCommand("wear it");

        assertThat(firstOutcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("I don't know how to do that.");
        assertThat(secondOutcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
        assertThat(told.toString()).contains("You put on the suit.");
    }
```

Run: `mvn -Dtest=GameLoopTest test`
Expected: PASS immediately — same reasoning as Step 6; this is the acceptance test for the demo-adventure scenario discussed during design review.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/engine/GameLoop.java src/test/java/com/pdg/adventure/server/engine/GameLoopTest.java
git commit -m "GameLoop reports a specific message when 'it' has no antecedent"
```

---

### Task 4: Seed "it" as a built-in word; exclude PRONOUN from the author's type picker

**Files:**
- Modify: `src/main/java/com/pdg/adventure/MiniAdventure.java`
- Modify: `src/main/java/com/pdg/adventure/server/engine/AdventureRunSessionFactory.java`
- Modify: `src/main/java/com/pdg/adventure/view/vocabulary/WordEditorDialogue.java`
- Test: `src/test/java/com/pdg/adventure/server/engine/AdventureRunSessionFactoryTest.java`
- Test: `src/test/java/com/pdg/adventure/view/vocabulary/WordEditorDialogueTest.java`

**Interfaces:**
- Consumes: `Word.Type.PRONOUN` (Task 2).
- Produces: nothing consumed by later tasks — this is the last task.

- [ ] **Step 1: Capture the Vocabulary instance in AdventureRunSessionFactoryTest**

In `AdventureRunSessionFactoryTest.java`, add a field:

```java
    private Vocabulary vocabulary;
```

Change `setUp()` from:

```java
        lenient().when(adventureConfig.allWords()).thenReturn(new Vocabulary());
```

to:

```java
        vocabulary = new Vocabulary();
        lenient().when(adventureConfig.allWords()).thenReturn(vocabulary);
```

Add the import:

```java
import com.pdg.adventure.model.Word;
```

- [ ] **Step 2: Write the failing test**

```java
    @Test
    void start_seedsPronounIt_throughTheRealRegisterBaseVerbsPath() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);

        factory.start(adventureData);

        assertThat(vocabulary.getType("it")).isEqualTo(Word.Type.PRONOUN);
    }
```

- [ ] **Step 3: Run test to verify it fails**

Run: `mvn -Dtest=AdventureRunSessionFactoryTest#start_seedsPronounIt_throughTheRealRegisterBaseVerbsPath test`
Expected: FAIL with `IllegalArgumentException: Unknown word 'it'` (or similar — `Vocabulary.getType` throws when the word was never seeded).

- [ ] **Step 4: Seed "it" in both production entry points**

In `AdventureRunSessionFactory.java`'s `registerBaseVerbs`, change:

```java
        aVocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        aVocabulary.createSynonym("then", "and");
    }
```

to:

```java
        aVocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        aVocabulary.createSynonym("then", "and");
        aVocabulary.createNewWord("it", Word.Type.PRONOUN);
    }
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn -Dtest=AdventureRunSessionFactoryTest test`
Expected: PASS.

- [ ] **Step 6: Write the failing test for the type picker**

Add to `WordEditorDialogueTest.java`, right after the existing `typeSelector_excludesConjunction` test:

```java
    @Test
    @DisplayName("Test 6b: Type selector - excludes PRONOUN, a reserved built-in-only type")
    void typeSelector_excludesPronoun() {
        // PRONOUN ("it") is seeded system-wide (AdventureRunSessionFactory),
        // not author-authored - an author must never be able to hand-create one via this picker.
        assertThat(typeSelector.getListDataView().getItems()).doesNotContain(Word.Type.PRONOUN);
        assertThat(typeSelector.getListDataView().getItems())
                .contains(Word.Type.VERB, Word.Type.NOUN, Word.Type.ADJECTIVE);
    }
```

- [ ] **Step 7: Run test to verify it fails**

Run: `mvn -Dtest=WordEditorDialogueTest#typeSelector_excludesPronoun test`
Expected: FAIL — `typeList` currently contains all five `Word.Type` values except `CONJUNCTION`; `PRONOUN` is still in there.

- [ ] **Step 8: Exclude PRONOUN from the picker**

In `WordEditorDialogue.java`'s `createTypeSelector()`, change:

```java
        // CONJUNCTION ("and"/"then") is a reserved, system-seeded type - never author-authorable.
        typeList.remove(Word.Type.CONJUNCTION);
```

to:

```java
        // CONJUNCTION ("and"/"then") is a reserved, system-seeded type - never author-authorable.
        typeList.remove(Word.Type.CONJUNCTION);
        // PRONOUN ("it") is likewise reserved and system-seeded - never author-authorable.
        typeList.remove(Word.Type.PRONOUN);
```

- [ ] **Step 9: Run test to verify it passes**

Run: `mvn -Dtest=WordEditorDialogueTest test`
Expected: PASS.

- [ ] **Step 10: Run the full suite**

Run: `mvn test`
Expected: PASS — entire suite green, no regressions.

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/engine/AdventureRunSessionFactory.java src/main/java/com/pdg/adventure/view/vocabulary/WordEditorDialogue.java src/test/java/com/pdg/adventure/server/engine/AdventureRunSessionFactoryTest.java src/test/java/com/pdg/adventure/view/vocabulary/WordEditorDialogueTest.java
git commit -m "Seed 'it' as a built-in PRONOUN word; exclude it from the author's type picker"
```
