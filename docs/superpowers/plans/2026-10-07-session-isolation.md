# Per-session engine isolation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let several players run games at the same time, each with their own engine state, by scoping `GameContext` and the `AdventureConfig` registries to the Vaadin session.

**Architecture:** A composed Spring scope annotation (`@PerBrowserSession` = `@Scope("vaadin-session", proxyMode = TARGET_CLASS)`) is put on `GameContext` and on the six registry `@Bean`s, so every injected singleton holds a proxy that resolves per browser session. A session-scoped `ActiveRun` guard allows one active run per browser session (with an explicit, user-confirmed takeover), and `SystemMessageKey`'s static override map becomes a thread-local that `AdventureRunSession` binds around each turn.

**Tech Stack:** Java 25, Spring Boot (Spring scopes / scoped proxies), Vaadin 25.2 (`vaadin-session` scope, `ConfirmDialog`, browserless tests), JUnit 5, Mockito, AssertJ, Maven.

**Spec:** `docs/superpowers/specs/2026-10-07-session-isolation-design.md` (amended in the same commit as this plan; the amendments are listed in the handoff message).

## Global Constraints

- All commands run from the git root `server/` (`/Users/mafw/workroom/projects/adventurebuilder/server`).
- Maven needs `export JAVA_HOME=/opt/homebrew/opt/openjdk@25` (JDK 21/26/27 fail).
- Run a single test class: `mvn test -Dtest=ClassName -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR|FAIL" | tail -15`.
- Every commit message ends with a separate paragraph containing exactly these two lines:
  `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>` and `Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ`.
- The scope name is exactly `"vaadin-session"`; the scoped beans are `GameContext` and the six `AdventureConfig` beans (`allLocations`, `allItems`, `allContainers`, `allWords`, `allMessages`, `allVariables`) plus the new `ActiveRun`. Nothing else becomes scoped.
- Code style: match the surrounding code (`a`/`an` parameter prefixes like `aGameContext`, unnamed lambda params `_ ->`, Lombok where already used, Javadoc style of the neighbouring classes).
- No new code path may run on a thread without a bound Vaadin session; scoped proxies throw there.
- Out of scope: save/load, clustering/session persistence, several concurrent games in one browser session, editor-flow changes.

## Review Focus

Failure modes the spec implies that a person would hit (each has a test in the named task; the list grew to seven because planning found two more than the usual five):

1. **F5 / crashed tab on the run page**: the old view still looks alive for ~15 minutes, so a pure block would lock the player out. Expected: a dialog offers "End the other game and start here" (Task 6 view test, Task 5 factory test).
2. **A late detach of an old view after a newer run started** must not wipe the newer game's registries (Task 5 `release_byAStaleOwner_keepsTheNewerRunsRegistries`).
3. **The opening room is rendered outside `submit()`**; it must still see the adventure's system-message overrides (Task 3 `runBound_...`, Task 6 view test verifies `runBound` is used).
4. **A turn that throws** must leave no overrides bound on the pooled servlet thread (Task 3 `submit_unbindsEvenWhenTheTurnThrows`, Task 2 `closingAfterAnException_stillUnbinds`).
5. **Text built while the adventure loads**: anything constructed during load/mapping/workflow set-up that reads system-message text must see the adventure's overrides, as it did when they were installed globally (Task 5 `start_hasTheOverridesBoundWhileTheAdventureIsMapped`).
6. **The router reuses the view instance** (same route entered again, e.g. another adventure id): the view's own earlier run must not count as a conflict, and a conflict on an already-attached view must open the dialog right away (Task 6 `enteringAgainOnTheSameViewInstance_...`, `aConflictOnAnAlreadyAttachedView_...`).
7. **The superseded tab keeps typing**: its next input must report the game was ended elsewhere and disable itself, not play on against the new game (Task 3 `supersede_...`, Task 5 `startReplacingActive_supersedesTheOldSession`).

---

## File Structure

Create (main):
- `src/main/java/com/pdg/adventure/server/engine/PerBrowserSession.java` — composed scope annotation.
- `src/main/java/com/pdg/adventure/server/engine/RunOwner.java` — Vaadin-free "is my view still there" handle.
- `src/main/java/com/pdg/adventure/server/engine/ActiveRun.java` — session-scoped guard holding the active run.
- `src/main/java/com/pdg/adventure/server/engine/RunAlreadyActiveException.java`

Create (test):
- `src/test/java/com/pdg/adventure/support/FakeSessionScope.java`, `FakeSessionScopeConfig.java`, `FakeSessionScopeConfigTest.java`
- `src/test/java/com/pdg/adventure/server/storage/message/SystemMessageKeyOverridesTest.java`
- `src/test/java/com/pdg/adventure/server/engine/AdventureRunSessionOverridesTest.java`
- `src/test/java/com/pdg/adventure/server/engine/SessionIsolationTest.java`
- `src/test/java/com/pdg/adventure/server/engine/ActiveRunTest.java`

Modify (main): `SystemMessageKey`, `LoadAdventureAction`, `AdventureRunSession`, `AdventureRunSessionFactory`, `GameContext`, `AdventureConfig`, `AdventureRunView`.
Modify (tests): `VariableProviderWiringTest`, `AutoMapperRegistrationTest`, `AdventureMapperReferenceResolutionTest`, `AutoTakeDropRealDispatchTest`, `BreakActionRealDispatchTest`, `WorkflowMapperRealDispatchTest`, `AutoMapperRegistrationProcessorTest`, `AdventureRunSessionFactoryTest`, `AdventureRunViewTest`.
Modify (docs): `docs/specs/02-functional-requirements.md`, `04-runtime-engine.md`, `09-rebuild-blueprint.md`.

---

### Task 1: Fake `vaadin-session` scope for tests

**Files:**
- Create: `src/test/java/com/pdg/adventure/support/FakeSessionScope.java`
- Create: `src/test/java/com/pdg/adventure/support/FakeSessionScopeConfig.java`
- Test: `src/test/java/com/pdg/adventure/support/FakeSessionScopeConfigTest.java`

**Interfaces:**
- Produces: `FakeSessionScope.NAME` (`"vaadin-session"`), `FakeSessionScope#useSession(String)`; importable `@Configuration` `FakeSessionScopeConfig` that registers the scope and exposes the `FakeSessionScope` as a bean (`context.getBean(FakeSessionScope.class)`). Later tasks add `FakeSessionScopeConfig.class` to plain-Spring test contexts and switch sessions with `useSession`.

- [ ] **Step 1: Write the failing test**

```java
package com.pdg.adventure.support;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import static org.assertj.core.api.Assertions.assertThat;

class FakeSessionScopeConfigTest {

    @Scope(value = FakeSessionScope.NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public static class Counter {
        private int count;

        public int next() {
            return ++count;
        }
    }

    @Test
    void eachFakeSessionGetsItsOwnBeanBehindOneProxy() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(FakeSessionScopeConfig.class, Counter.class);
            context.refresh();
            Counter counter = context.getBean(Counter.class);
            FakeSessionScope sessions = context.getBean(FakeSessionScope.class);

            sessions.useSession("a");
            counter.next();
            counter.next();
            sessions.useSession("b");
            assertThat(counter.next()).isEqualTo(1);
            sessions.useSession("a");
            assertThat(counter.next()).isEqualTo(3);
        }
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `mvn test -Dtest=FakeSessionScopeConfigTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -8`
Expected: compilation error (`FakeSessionScope` / `FakeSessionScopeConfig` do not exist).

- [ ] **Step 3: Implement the helper**

```java
package com.pdg.adventure.support;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Test stand-in for Vaadin's "vaadin-session" scope: one bean store per fake session id, switched with
 * {@link #useSession(String)}. Single-threaded on purpose - tests interleave "sessions" by switching.
 */
public class FakeSessionScope implements Scope {

    public static final String NAME = "vaadin-session";

    private final Map<String, Map<String, Object>> storesBySession = new HashMap<>();
    private String currentSession = "default";

    public void useSession(String aSessionId) {
        currentSession = aSessionId;
    }

    @Override
    public Object get(String aName, ObjectFactory<?> anObjectFactory) {
        Map<String, Object> store = storesBySession.computeIfAbsent(currentSession, _ -> new HashMap<>());
        Object bean = store.get(aName);
        if (bean == null) {
            bean = anObjectFactory.getObject();
            store.put(aName, bean);
        }
        return bean;
    }

    @Override
    public Object remove(String aName) {
        Map<String, Object> store = storesBySession.get(currentSession);
        return store == null ? null : store.remove(aName);
    }

    @Override
    public void registerDestructionCallback(String aName, Runnable aCallback) {
        // destruction is not simulated
    }

    @Override
    public Object resolveContextualObject(String aKey) {
        return null;
    }

    @Override
    public String getConversationId() {
        return currentSession;
    }
}
```

```java
package com.pdg.adventure.support;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Import into a plain Spring test context so beans scoped to "vaadin-session" can be created and switched. */
@Configuration
public class FakeSessionScopeConfig {

    @Bean
    public static FakeSessionScope fakeSessionScope() {
        return new FakeSessionScope();
    }

    @Bean
    public static CustomScopeConfigurer fakeSessionScopeRegistration(FakeSessionScope aScope) {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope(FakeSessionScope.NAME, aScope);
        return configurer;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn test -Dtest=FakeSessionScopeConfigTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -8`
Expected: `Tests run: 1, Failures: 0` and `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/pdg/adventure/support
git commit -m "add fake vaadin-session scope for plain-Spring tests" -m "Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ"
```

---

### Task 2: System-message overrides become a thread-local binding

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/storage/message/SystemMessageKey.java` (field at ~line 120-127, `defaultText()` ~line 154, `installOverrides` ~line 170)
- Modify: `src/main/java/com/pdg/adventure/server/action/LoadAdventureAction.java:82-87` plus now-unused imports
- Test: `src/test/java/com/pdg/adventure/server/storage/message/SystemMessageKeyOverridesTest.java`

**Interfaces:**
- Produces: `SystemMessageKey.bindOverrides(Map<String,String>)` returning `SystemMessageKey.Binding` (an `AutoCloseable` whose `close()` throws nothing). `defaultText()` returns the bound override for the key id, else the built-in text. `installOverrides` is removed.

- [ ] **Step 1: Write the failing test**

```java
package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemMessageKeyOverridesTest {

    private static final String ID = SystemMessageKey.SM9.id();
    private static final String BUILT_IN = SystemMessageKey.SM9.defaultText();

    @Test
    void unbound_returnsTheBuiltInText() {
        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void bound_returnsTheOverride_andRestoresTheBuiltInTextOnClose() {
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
            assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo("Carrying:");
        }

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void aKeyWithoutAnOverride_keepsItsBuiltInTextWhileOthersAreBound() {
        String builtIn8 = SystemMessageKey.SM8.defaultText();

        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
            assertThat(SystemMessageKey.SM8.defaultText()).isEqualTo(builtIn8);
        }
    }

    @Test
    void nestedBindings_restoreTheOuterOneWhenTheInnerCloses() {
        try (SystemMessageKey.Binding outer = SystemMessageKey.bindOverrides(Map.of(ID, "outer"))) {
            try (SystemMessageKey.Binding inner = SystemMessageKey.bindOverrides(Map.of(ID, "inner"))) {
                assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo("inner");
            }
            assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo("outer");
        }

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void closingAfterAnException_stillUnbinds() {
        assertThatThrownBy(() -> {
            try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
                throw new IllegalStateException("boom");
            }
        }).isInstanceOf(IllegalStateException.class);

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void aBinding_isInvisibleToOtherThreads() throws Exception {
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
            AtomicReference<String> seenByOtherThread = new AtomicReference<>();
            Thread other = new Thread(() -> seenByOtherThread.set(SystemMessageKey.SM9.defaultText()));
            other.start();
            other.join();

            assertThat(seenByOtherThread.get()).isEqualTo(BUILT_IN);
        }
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `mvn test -Dtest=SystemMessageKeyOverridesTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -8`
Expected: compilation error (`bindOverrides` / `Binding` do not exist).

- [ ] **Step 3: Implement**

In `SystemMessageKey.java` replace the static field and its Javadoc (the block starting `/** The currently loaded adventure's own edits` through `private static Map<String, String> overridesByKeyId = Map.of();`) with:

```java
    /**
     * The overrides (id -> text) of the adventure whose turn is running on this thread, sparse - a key with no
     * entry reads as {@link #defaultText()}'s built-in text. Overrides belong to an adventure definition (the same
     * for every player and saved game of it), but different adventures can run at the same time, so they are
     * bound per thread for the duration of a run's turn (see {@link #bindOverrides}) instead of process-wide.
     */
    private static final ThreadLocal<Map<String, String>> BOUND_OVERRIDES = new ThreadLocal<>();
```

Replace the body of `defaultText()`:

```java
    public String defaultText() {
        Map<String, String> bound = BOUND_OVERRIDES.get();
        return bound == null ? defaultText : bound.getOrDefault(id, defaultText);
    }
```

Replace `installOverrides` (the Javadoc line and method at the end of the file) with:

```java
    /**
     * Binds the given overrides to the current thread until the returned binding is closed. Closing restores
     * whatever was bound before, so nested binds are safe. Always use try-with-resources: servlet threads are
     * reused, and an unclosed binding would leak this adventure's wording into the next request on the thread.
     */
    public static Binding bindOverrides(Map<String, String> anOverridesByKeyId) {
        Map<String, String> previous = BOUND_OVERRIDES.get();
        BOUND_OVERRIDES.set(Map.copyOf(anOverridesByKeyId));
        return () -> {
            if (previous == null) {
                BOUND_OVERRIDES.remove();
            } else {
                BOUND_OVERRIDES.set(previous);
            }
        };
    }

    /** Undoes a {@link #bindOverrides} call; unlike {@link AutoCloseable#close()} it throws nothing. */
    @FunctionalInterface
    public interface Binding extends AutoCloseable {
        @Override
        void close();
    }
```

In `LoadAdventureAction.java` delete this block (lines 82-87):

```java
        Map<String, String> systemMessageOverrides = new HashMap<>();
        for (SystemMessageData override : adventureData.getSystemMessages().values()) {
            systemMessageOverrides.put(override.getKey(), override.getText());
        }
        SystemMessageKey.installOverrides(systemMessageOverrides);
```

and delete the four imports that become unused: `java.util.HashMap`, `java.util.Map`, `com.pdg.adventure.model.SystemMessageData`, `com.pdg.adventure.server.storage.message.SystemMessageKey` (keep any that the compiler still needs: check with the next step).

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='SystemMessageKey*Test,LoadAdventureActionTest,AdventureRunSessionFactoryTest' -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -10`
Expected: `BUILD SUCCESS`, no failures. If `LoadAdventureAction.java` fails to compile because an import was still needed, restore only that import.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/storage/message/SystemMessageKey.java src/main/java/com/pdg/adventure/server/action/LoadAdventureAction.java src/test/java/com/pdg/adventure/server/storage/message/SystemMessageKeyOverridesTest.java
git commit -m "bind system-message overrides per thread instead of process-wide" -m "Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ"
```

---

### Task 3: `AdventureRunSession` carries and binds its overrides, and can be superseded

**Files:**
- Modify (replace whole file): `src/main/java/com/pdg/adventure/server/engine/AdventureRunSession.java`
- Test: `src/test/java/com/pdg/adventure/server/engine/AdventureRunSessionOverridesTest.java`

**Interfaces:**
- Consumes: `SystemMessageKey.bindOverrides` / `Binding` (Task 2).
- Produces: package-private constructor `AdventureRunSession(GameLoop, GameContext, Map<String,String> overrides)` (the 2-arg one stays and delegates with `Map.of()`); `public <T> T runBound(Supplier<T>)`; `public void supersede()`; package-visible constant `AdventureRunSession.ENDED_ELSEWHERE_TEXT` (`"This game was ended in another tab."`). After `supersede()`, `isGameOver()` is true and every `submit` returns `new RunResult(List.of(ENDED_ELSEWHERE_TEXT), true)`.

- [ ] **Step 1: Write the failing test**

```java
package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class AdventureRunSessionOverridesTest {

    private static final String ID = SystemMessageKey.SM9.id();
    private static final String BUILT_IN = SystemMessageKey.SM9.defaultText();

    private GameContext gameContext;
    private Vocabulary vocabulary;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        vocabulary = new Vocabulary();
    }

    // A loop that records what SM9 reads as while the turn runs, instead of parsing anything.
    private AdventureRunSession sessionWhoseTurnReads(List<String> seen, Map<String, String> overrides) {
        GameLoop recordingLoop = new GameLoop(new Parser(vocabulary), gameContext) {
            @Override
            public CommandOutcome processCommand(String anInput) {
                seen.add(SystemMessageKey.SM9.defaultText());
                return CommandOutcome.CONTINUE;
            }
        };
        return new AdventureRunSession(recordingLoop, gameContext, overrides);
    }

    @Test
    void submit_bindsTheSessionsOverridesForTheTurn_andUnbindsAfterwards() {
        List<String> seen = new ArrayList<>();
        AdventureRunSession session = sessionWhoseTurnReads(seen, Map.of(ID, "Carrying:"));

        session.submit("anything");

        assertThat(seen).containsExactly("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void submit_unbindsEvenWhenTheTurnThrows() {
        GameLoop throwingLoop = new GameLoop(new Parser(vocabulary), gameContext) {
            @Override
            public CommandOutcome processCommand(String anInput) {
                throw new IllegalStateException("boom");
            }
        };
        AdventureRunSession session = new AdventureRunSession(throwingLoop, gameContext, Map.of(ID, "Carrying:"));

        assertThatThrownBy(() -> session.submit("anything")).isInstanceOf(IllegalStateException.class);

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void runBound_bindsForTheStepOnly() {
        AdventureRunSession session = sessionWhoseTurnReads(new ArrayList<>(), Map.of(ID, "Carrying:"));

        String inside = session.runBound(SystemMessageKey.SM9::defaultText);

        assertThat(inside).isEqualTo("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void twoSessionsWithDifferentOverrides_eachSeeTheirOwn() {
        List<String> seenByA = new ArrayList<>();
        List<String> seenByB = new ArrayList<>();
        AdventureRunSession a = sessionWhoseTurnReads(seenByA, Map.of(ID, "Tragen:"));
        AdventureRunSession b = sessionWhoseTurnReads(seenByB, Map.of(ID, "Portant:"));

        a.submit("x");
        b.submit("x");
        a.submit("x");

        assertThat(seenByA).containsExactly("Tragen:", "Tragen:");
        assertThat(seenByB).containsExactly("Portant:");
    }

    @Test
    void supersede_makesTheNextSubmitReportTheGameEndedElsewhere() {
        List<String> seen = new ArrayList<>();
        AdventureRunSession session = sessionWhoseTurnReads(seen, Map.of());

        session.supersede();
        RunResult result = session.submit("look");

        assertThat(session.isGameOver()).isTrue();
        assertThat(result.gameOver()).isTrue();
        assertThat(result.lines()).containsExactly(AdventureRunSession.ENDED_ELSEWHERE_TEXT);
        assertThat(seen).isEmpty();
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `mvn test -Dtest=AdventureRunSessionOverridesTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -8`
Expected: compilation error (3-arg constructor, `runBound`, `supersede`, `ENDED_ELSEWHERE_TEXT` do not exist).

- [ ] **Step 3: Implement** — replace the whole file:

```java
package com.pdg.adventure.server.engine;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

/**
 * A single interactive play session, driving the browser session's GameLoop/GameContext one command at a
 * time. Used both when an author clicks "Test" on their own adventure and when a player clicks "Run Adventure"
 * on one they're assigned to. Only created by {@link AdventureRunSessionFactory}; the caller
 * (AdventureRunView) still renders the opening room itself, through {@link #runBound}.
 * <p>
 * The session carries its adventure's system-message overrides and binds them around every turn (and around
 * anything else run through {@link #runBound}), so different adventures can be played at the same time.
 */
public class AdventureRunSession {

    static final String ENDED_ELSEWHERE_TEXT = "This game was ended in another tab.";

    private final GameLoop gameLoop;
    @Getter
    private final GameContext gameContext;
    private final Map<String, String> systemMessageOverrides;
    private boolean gameOver;
    private boolean supersededElsewhere;

    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext) {
        this(aGameLoop, aGameContext, Map.of());
    }

    AdventureRunSession(GameLoop aGameLoop, GameContext aGameContext, Map<String, String> anOverridesByKeyId) {
        gameLoop = aGameLoop;
        gameContext = aGameContext;
        systemMessageOverrides = Map.copyOf(anOverridesByKeyId);
    }

    public RunResult submit(String rawInput) {
        if (supersededElsewhere) {
            return new RunResult(List.of(ENDED_ELSEWHERE_TEXT), true);
        }
        if (gameOver) {
            return new RunResult(List.of(), true);
        }
        return runBound(() -> playTurn(rawInput));
    }

    /**
     * Runs the step with this session's system-message overrides bound to the current thread. For work done
     * outside {@link #submit} that still prints engine text, e.g. rendering the opening room.
     */
    public <T> T runBound(Supplier<T> aStep) {
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(systemMessageOverrides)) {
            return aStep.get();
        }
    }

    /** Ends this session because the player started the same game anew in another tab. */
    public void supersede() {
        supersededElsewhere = true;
        gameOver = true;
    }

    private RunResult playTurn(String rawInput) {
        List<String> lines = new ArrayList<>();
        gameContext.setOutputSink(line -> {
            if (line != null && !line.isBlank() && !SystemMessageKey.SM2.defaultText().equals(line)) {
                lines.add(line);
            }
        });
        try {
            GameLoop.CommandOutcome outcome = gameLoop.processCommand(rawInput);
            gameOver = outcome != GameLoop.CommandOutcome.CONTINUE;
        } catch (ReloadAdventureException unexpected) {
            // A run session never registers the cross-adventure "load X" command, so this
            // should be unreachable — guarded so a surprise author workflow command can't leak
            // an uncaught exception into the Vaadin listener.
            lines.add("Something interrupted the game unexpectedly. Ending this session.");
            gameOver = true;
        } finally {
            gameContext.setOutputSink(null);
        }
        return new RunResult(lines, gameOver);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public record RunResult(List<String> lines, boolean gameOver) {
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn test -Dtest='AdventureRunSession*Test' -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -10`
Expected: `BUILD SUCCESS` (the new tests and the existing `AdventureRunSessionTest` / `AdventureRunSessionFactoryTest` pass).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/engine/AdventureRunSession.java src/test/java/com/pdg/adventure/server/engine/AdventureRunSessionOverridesTest.java
git commit -m "bind overrides around each turn and let a run be superseded" -m "Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ"
```

---

### Task 4: Scope `GameContext` and the registries per browser session

**Files:**
- Create: `src/main/java/com/pdg/adventure/server/engine/PerBrowserSession.java`
- Modify: `src/main/java/com/pdg/adventure/server/engine/GameContext.java` (class annotations, Javadoc of `setOutputSink`)
- Modify: `src/main/java/com/pdg/adventure/server/AdventureConfig.java` (six `@Bean` methods)
- Modify tests (add `FakeSessionScopeConfig`): `VariableProviderWiringTest`, `AutoMapperRegistrationTest`, `AdventureMapperReferenceResolutionTest`, `AutoTakeDropRealDispatchTest`, `BreakActionRealDispatchTest`, `WorkflowMapperRealDispatchTest`, `AutoMapperRegistrationProcessorTest`
- Test: `src/test/java/com/pdg/adventure/server/engine/SessionIsolationTest.java`

**Interfaces:**
- Consumes: `FakeSessionScope`, `FakeSessionScopeConfig` (Task 1).
- Produces: annotation `@PerBrowserSession` (`com.pdg.adventure.server.engine`), usable on types and `@Bean` methods; after this task `GameContext` and `AdventureConfig.allX()` beans are scoped proxies. `AdventureConfig`'s public method signatures do not change.

- [ ] **Step 1: Write the failing test**

```java
package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.support.FakeSessionScope;
import com.pdg.adventure.support.FakeSessionScopeConfig;

/** Two fake browser sessions against the real scoped beans: nothing one session does shows up in the other. */
class SessionIsolationTest {

    private AnnotationConfigApplicationContext context;
    private FakeSessionScope sessions;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.register(FakeSessionScopeConfig.class, GameContext.class, AdventureConfig.class,
                         MapperSupporter.class);
        context.refresh();
        sessions = context.getBean(FakeSessionScope.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void gameContextFields_areIndependentPerSession() {
        GameContext gameContext = context.getBean(GameContext.class);

        sessions.useSession("anna");
        gameContext.setCurrentNoun("lamp");
        sessions.useSession("ben");
        assertThat(gameContext.getCurrentNoun()).isEqualTo(VocabularyData.EMPTY_STRING);
        gameContext.setCurrentNoun("key");
        sessions.useSession("anna");

        assertThat(gameContext.getCurrentNoun()).isEqualTo("lamp");
    }

    @Test
    void outputSink_ofOneSessionNeverReceivesTheOtherSessionsOutput() {
        GameContext gameContext = context.getBean(GameContext.class);
        List<String> annasOutput = new ArrayList<>();

        sessions.useSession("anna");
        gameContext.setOutputSink(annasOutput::add);
        sessions.useSession("ben");
        gameContext.tell("for ben");

        assertThat(annasOutput).isEmpty();
    }

    @Test
    void variables_areIndependentPerSession() {
        AdventureConfig config = context.getBean(AdventureConfig.class);

        sessions.useSession("anna");
        config.allVariables().set("score", 5);
        sessions.useSession("ben");

        assertThat(config.allVariables().isDefined("score")).isFalse();
    }

    @Test
    void mapperSupportersRegistries_followTheCurrentSession() {
        MapperSupporter supporter = context.getBean(MapperSupporter.class);
        Location annasRoom = mock(Location.class);
        when(annasRoom.getId()).thenReturn("room");

        sessions.useSession("anna");
        supporter.addMappedLocation(annasRoom);
        sessions.useSession("ben");
        assertThat(supporter.getMappedLocation("room")).isNull();
        sessions.useSession("anna");

        assertThat(supporter.getMappedLocation("room")).isSameAs(annasRoom);
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `mvn test -Dtest=SessionIsolationTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR|FAIL" | tail -10`
Expected: all four tests FAIL (the beans are still shared singletons, so anna's values show up for ben).

- [ ] **Step 3: Implement the scoping**

Create `PerBrowserSession.java`:

```java
package com.pdg.adventure.server.engine;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a bean to the Vaadin session (the same "vaadin-session" scope as {@code @VaadinSessionScope}) and injects
 * a scoped proxy, so singletons can hold the bean and still reach the calling browser session's instance on every
 * call. {@code @VaadinSessionScope} itself has no proxy mode, hence this composed annotation.
 * <p>
 * Beans with this scope can only be used on a thread that has a bound VaadinSession (UI event handlers, not
 * background threads).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public @interface PerBrowserSession {
}
```

`GameContext.java`: add the annotation under `@Component`:

```java
@Component
@PerBrowserSession
public class GameContext {
```

and in the Javadoc of `setOutputSink` replace "GameContext is a process-wide singleton, so callers\n     * must install the sink immediately before driving the engine" with "GameContext is scoped to the browser session, so callers\n     * must install the sink immediately before driving the engine" (keep the rest of the sentence unchanged).

`AdventureConfig.java`: add `@PerBrowserSession` (import `com.pdg.adventure.server.engine.PerBrowserSession`) above `@Bean` on each of the six methods, e.g.:

```java
    @Bean
    @Lazy
    @PerBrowserSession
    public Map<String, Location> allLocations() {
        return new HashMap<>();
    }
```
Do the same for `allItems`, `allContainers`, `allWords`, `allMessages`, `allVariables`.

- [ ] **Step 4: Run the isolation test to verify it passes**

Run: `mvn test -Dtest=SessionIsolationTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR|FAIL" | tail -10`
Expected: `Tests run: 4, Failures: 0`, `BUILD SUCCESS`. (If the `Map` registry beans fail to proxy, stop and report: the spec's fallback is to wrap each registry in a small named class.)

- [ ] **Step 5: Register the fake scope in the existing plain-Spring test contexts**

For the four tests that call `context.register(GameContext.class, AdventureConfig.class, MapperSupporter.class,` (`AutoTakeDropRealDispatchTest`, `BreakActionRealDispatchTest`, `WorkflowMapperRealDispatchTest`, `AutoMapperRegistrationProcessorTest`), run:

```bash
for f in src/test/java/com/pdg/adventure/server/mapper/AutoTakeDropRealDispatchTest.java \
         src/test/java/com/pdg/adventure/server/mapper/BreakActionRealDispatchTest.java \
         src/test/java/com/pdg/adventure/server/mapper/WorkflowMapperRealDispatchTest.java \
         src/test/java/com/pdg/adventure/server/annotation/AutoMapperRegistrationProcessorTest.java; do
  perl -0pi -e 's/context\.register\(GameContext\.class, AdventureConfig\.class, MapperSupporter\.class,/context.register(FakeSessionScopeConfig.class, GameContext.class, AdventureConfig.class,\n                         MapperSupporter.class,/; s/((?:import com\.pdg\.adventure[^\n]*\n)+)/$1import com.pdg.adventure.support.FakeSessionScopeConfig;\n/' "$f"
done
```

For the three `@Import`-based tests make these exact edits:
- `AutoMapperRegistrationTest.java:23`: `@Import({AdventureConfig.class, MapperSupporter.class, VocabularyMapper.class,` → `@Import({FakeSessionScopeConfig.class, AdventureConfig.class, MapperSupporter.class, VocabularyMapper.class,`
- `AdventureMapperReferenceResolutionTest.java:44`: `@Import({AdventureConfig.class, MapperSupporter.class, AutoMapperRegistrationProcessor.class,` → `@Import({FakeSessionScopeConfig.class, AdventureConfig.class, MapperSupporter.class, AutoMapperRegistrationProcessor.class,`
- `VariableProviderWiringTest.java`: `@Import(AdventureConfig.class)` → `@Import({FakeSessionScopeConfig.class, AdventureConfig.class})`, and change the assertion because a scoped proxy adds a second, non-candidate definition `scopedTarget.allVariables`:

```java
            String[] names = context.getBeanNamesForType(VariableProvider.class);
            assertThat(names).filteredOn(name -> !name.startsWith("scopedTarget.")).hasSize(1);
```

Add `import com.pdg.adventure.support.FakeSessionScopeConfig;` to those three files (after the existing `com.pdg.adventure` imports).

- [ ] **Step 6: Run the affected tests**

Run: `mvn test -Dtest='VariableProviderWiringTest,AutoMapperRegistration*Test,AdventureMapperReferenceResolutionTest,AutoTakeDropRealDispatchTest,BreakActionRealDispatchTest,WorkflowMapperRealDispatchTest,SessionIsolationTest' -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR|FAIL" | tail -15`
Expected: `BUILD SUCCESS`. A failure mentioning `No Scope registered for scope name 'vaadin-session'` means that context is missing `FakeSessionScopeConfig`.

- [ ] **Step 7: Commit**

```bash
git add src/main src/test
git commit -m "scope GameContext and the adventure registries to the browser session" -m "Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ"
```

---

### Task 5: One active run per browser session, owner-checked release, takeover

**Files:**
- Create: `src/main/java/com/pdg/adventure/server/engine/RunOwner.java`, `RunAlreadyActiveException.java`, `ActiveRun.java`
- Modify (replace whole file): `src/main/java/com/pdg/adventure/server/engine/AdventureRunSessionFactory.java`
- Modify (minimal, to keep the module compiling): `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java`, `src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`
- Test: `src/test/java/com/pdg/adventure/server/engine/ActiveRunTest.java`
- Modify test: `src/test/java/com/pdg/adventure/server/engine/AdventureRunSessionFactoryTest.java`

**Interfaces:**
- Consumes: `AdventureRunSession#supersede`, `#isGameOver`, 3-arg constructor, `ENDED_ELSEWHERE_TEXT` (Task 3); `@PerBrowserSession` (Task 4).
- Produces:
  - `RunOwner`: `boolean isGone()`, `void markGone()`.
  - `RunAlreadyActiveException extends RuntimeException` (no-arg constructor; message `"You already have a game running in another tab."`).
  - `ActiveRun` (public class, public no-arg constructor): `boolean isActive()`, `void register(AdventureRunSession, RunOwner)`, `void supersedeActive()`, `boolean release(RunOwner)`.
  - `AdventureRunSessionFactory(AdventureService, AdventureMapper, WorkflowMapper, AdventureConfig, GameContext, ActiveRun)`; `AdventureRunSession start(AdventureData, RunOwner)` (throws `RunAlreadyActiveException` before touching any state); `AdventureRunSession startReplacingActive(AdventureData, RunOwner)`; `void release(RunOwner)`.

- [ ] **Step 1: Write the failing `ActiveRunTest`**

```java
package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class ActiveRunTest {

    private final ActiveRun activeRun = new ActiveRun();
    private final GameContext gameContext = new GameContext();

    private AdventureRunSession newSession() {
        return new AdventureRunSession(new GameLoop(new Parser(new Vocabulary()), gameContext), gameContext);
    }

    @Test
    void nothingRegistered_isNotActive() {
        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void aRegisteredRunWithALivingOwner_isActive() {
        activeRun.register(newSession(), new RunOwner());

        assertThat(activeRun.isActive()).isTrue();
    }

    @Test
    void whenTheOwnerIsGone_theRunIsNoLongerActive() {
        RunOwner owner = new RunOwner();
        activeRun.register(newSession(), owner);

        owner.markGone();

        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void whenTheGameIsOver_theRunIsNoLongerActive() {
        AdventureRunSession session = newSession();
        activeRun.register(session, new RunOwner());

        session.supersede();

        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void release_byTheOwner_clearsTheRun_andReportsTrue() {
        RunOwner owner = new RunOwner();
        activeRun.register(newSession(), owner);

        assertThat(activeRun.release(owner)).isTrue();
        assertThat(activeRun.isActive()).isFalse();
    }

    @Test
    void release_byAnyoneElse_keepsTheRun_andReportsFalse() {
        activeRun.register(newSession(), new RunOwner());

        assertThat(activeRun.release(new RunOwner())).isFalse();
        assertThat(activeRun.isActive()).isTrue();
    }

    @Test
    void supersedeActive_endsTheActiveSession() {
        AdventureRunSession session = newSession();
        activeRun.register(session, new RunOwner());

        activeRun.supersedeActive();

        assertThat(session.isGameOver()).isTrue();
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `mvn test -Dtest=ActiveRunTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -8`
Expected: compilation error (`ActiveRun`, `RunOwner` do not exist).

- [ ] **Step 3: Implement the three small classes**

```java
package com.pdg.adventure.server.engine;

/**
 * A handle a run's view keeps to say "I'm still here". The engine layer sees only this, never a Vaadin type.
 * The view marks it gone when it detaches.
 */
public final class RunOwner {

    private volatile boolean gone;

    public boolean isGone() {
        return gone;
    }

    public void markGone() {
        gone = true;
    }
}
```

```java
package com.pdg.adventure.server.engine;

/** A run was requested while this browser session already has an active one. */
public class RunAlreadyActiveException extends RuntimeException {

    public RunAlreadyActiveException() {
        super("You already have a game running in another tab.");
    }
}
```

```java
package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Component;

/**
 * The run currently active in this browser session, if any. Session-scoped (one per Vaadin session), so it
 * is what makes "one game per browser session" enforceable. A run is active while its owner's view is still
 * there and the game isn't over; a refreshed or crashed tab can look alive for a while (Vaadin only notices
 * dead UIs after missed heartbeats), which is why the view offers an explicit takeover.
 */
@Component
@PerBrowserSession
public class ActiveRun {

    private AdventureRunSession session;
    private RunOwner owner;

    public synchronized boolean isActive() {
        return session != null && !owner.isGone() && !session.isGameOver();
    }

    public synchronized void register(AdventureRunSession aSession, RunOwner anOwner) {
        session = aSession;
        owner = anOwner;
    }

    /** Ends the active run, if there is one, as superseded by a newer start. */
    public synchronized void supersedeActive() {
        if (isActive()) {
            session.supersede();
        }
    }

    /**
     * Clears the run, but only if {@code anOwner} still owns it - a late release from an old view must not
     * clear a newer run.
     *
     * @return true if the run was cleared
     */
    public synchronized boolean release(RunOwner anOwner) {
        if (owner != anOwner) {
            return false;
        }
        session = null;
        owner = null;
        return true;
    }
}
```

- [ ] **Step 4: Run `ActiveRunTest`**

Run: `mvn test -Dtest=ActiveRunTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -8`
Expected: `Tests run: 7, Failures: 0`.

- [ ] **Step 5: Write the failing factory tests**

In `AdventureRunSessionFactoryTest.java`:

1. Replace every `factory.start(adventureData)` with `factory.start(adventureData, new RunOwner())`:
   `perl -pi -e 's/factory\.start\(adventureData\)/factory.start(adventureData, new RunOwner())/g' src/test/java/com/pdg/adventure/server/engine/AdventureRunSessionFactoryTest.java`
2. In `setUp()` keep the registry maps in fields so tests can inspect them, stub `allVariables()`, and pass an `ActiveRun`:

```java
    private final Map<String, Item> items = new HashMap<>();
    private ActiveRun activeRun;
```
and in `setUp()` replace the lines for `allItems` and the factory construction:

```java
        lenient().when(adventureConfig.allItems()).thenReturn(items);
        lenient().when(adventureConfig.allVariables()).thenReturn(new VariableProvider());
        ...
        activeRun = new ActiveRun();
        factory = new AdventureRunSessionFactory(adventureService, adventureMapper, workflowMapper, adventureConfig,
                                                  gameContext, activeRun);
```
Add imports: `java.util.ArrayList`, `java.util.Map`, `static org.mockito.Mockito.mock`, `static org.mockito.Mockito.times`, `static org.mockito.Mockito.verify`, `com.pdg.adventure.model.SystemMessageData`, `com.pdg.adventure.server.support.VariableProvider`, `com.pdg.adventure.server.tangible.Item`.
3. Add a helper next to `adventureWithOneLocation`:

```java
    private AdventureData startableAdventure() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        when(adventureMapper.mapToBO(adventureData)).thenReturn(adventure);
        return adventureData;
    }
```
4. Add the tests:

```java
    @Test
    void start_whileAnotherRunIsActive_throwsAndNeverTouchesTheEngineAgain() {
        AdventureData adventureData = startableAdventure();
        factory.start(adventureData, new RunOwner());

        assertThatThrownBy(() -> factory.start(adventureData, new RunOwner()))
                .isInstanceOf(RunAlreadyActiveException.class);

        verify(adventureMapper, times(1)).mapToBO(adventureData);
    }

    @Test
    void start_afterTheOwnerIsGone_succeeds() {
        AdventureData adventureData = startableAdventure();
        RunOwner first = new RunOwner();
        factory.start(adventureData, first);
        first.markGone();

        AdventureRunSession second = factory.start(adventureData, new RunOwner());

        assertThat(second.isGameOver()).isFalse();
        verify(adventureMapper, times(2)).mapToBO(adventureData);
    }

    @Test
    void start_afterTheRunEnded_succeeds() {
        AdventureData adventureData = startableAdventure();
        AdventureRunSession first = factory.start(adventureData, new RunOwner());
        first.submit("quit");

        AdventureRunSession second = factory.start(adventureData, new RunOwner());

        assertThat(second.isGameOver()).isFalse();
    }

    @Test
    void startReplacingActive_supersedesTheOldSession() {
        AdventureData adventureData = startableAdventure();
        AdventureRunSession old = factory.start(adventureData, new RunOwner());

        AdventureRunSession replacement = factory.startReplacingActive(adventureData, new RunOwner());
        RunResult oldResult = old.submit("look");

        assertThat(oldResult.gameOver()).isTrue();
        assertThat(oldResult.lines()).containsExactly(AdventureRunSession.ENDED_ELSEWHERE_TEXT);
        assertThat(replacement.isGameOver()).isFalse();
    }

    @Test
    void release_byTheCurrentOwner_clearsTheRegistriesAndTheContext_andAllowsANewStart() {
        AdventureData adventureData = startableAdventure();
        RunOwner owner = new RunOwner();
        factory.start(adventureData, owner);
        items.put("sword", mock(Item.class));
        gameContext.setCurrentPictureId("pic-1");

        factory.release(owner);

        assertThat(items).isEmpty();
        assertThat(gameContext.getCurrentLocation()).isNull();
        assertThat(gameContext.getCurrentPictureId()).isNull();
        assertThat(factory.start(adventureData, new RunOwner()).isGameOver()).isFalse();
    }

    @Test
    void release_byAStaleOwner_keepsTheNewerRunsRegistries() {
        AdventureData adventureData = startableAdventure();
        RunOwner stale = new RunOwner();
        factory.start(adventureData, stale);
        stale.markGone();
        factory.start(adventureData, new RunOwner());
        items.put("sword", mock(Item.class));

        factory.release(stale);

        assertThat(items).containsKey("sword");
        assertThatThrownBy(() -> factory.start(adventureData, new RunOwner()))
                .isInstanceOf(RunAlreadyActiveException.class);
    }

    @Test
    void start_bindsTheAdventuresSystemMessageOverridesIntoTheSession() {
        AdventureData adventureData = startableAdventure();
        adventureData.getSystemMessages().put("9", new SystemMessageData("9", "Carrying:"));

        AdventureRunSession session = factory.start(adventureData, new RunOwner());

        assertThat(session.runBound(SystemMessageKey.SM9::defaultText)).isEqualTo("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isNotEqualTo("Carrying:");
    }

    @Test
    void start_hasTheOverridesBoundWhileTheAdventureIsMapped() {
        AdventureData adventureData = adventureWithOneLocation("adv-1", "loc-1");
        adventureData.getSystemMessages().put("9", new SystemMessageData("9", "Carrying:"));
        when(adventureService.findAdventureById("adv-1")).thenReturn(Optional.of(adventureData));
        when(startLocation.getId()).thenReturn("loc-1");
        Adventure adventure = adventureBoundTo(startLocation, "loc-1");
        List<String> seenWhileMapping = new ArrayList<>();
        when(adventureMapper.mapToBO(adventureData)).thenAnswer(invocation -> {
            seenWhileMapping.add(SystemMessageKey.SM9.defaultText());
            return adventure;
        });

        factory.start(adventureData, new RunOwner());

        assertThat(seenWhileMapping).containsExactly("Carrying:");
        assertThat(SystemMessageKey.SM9.defaultText()).isNotEqualTo("Carrying:");
    }
```

- [ ] **Step 6: Run to verify the factory tests fail**

Run: `mvn test -Dtest=AdventureRunSessionFactoryTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -8`
Expected: compilation error (factory constructor / `start(AdventureData, RunOwner)` / `release` do not exist).

- [ ] **Step 7: Implement the factory** — replace the whole file:

```java
package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.CommandFactory;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.LoadAdventureAction;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.mapper.AdventureMapper;
import com.pdg.adventure.server.mapper.WorkflowMapper;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.vocabulary.Vocabulary;

/**
 * Bootstraps a browser-playable game session for a single, already-saved adventure.
 * Serves both the author's "Test" flow and a player's "Run Adventure" flow; access control
 * (who may load which adventure) is handled by the caller via AdventureAccessService, not here.
 * <p>
 * The GameContext and the AdventureConfig registries injected here are scoped-proxy beans bound to the
 * calling Vaadin session, so every browser session has its own engine state and several players can play
 * at once. Within one browser session only one run is active at a time ({@link ActiveRun}); a second
 * {@link #start} is refused with {@link RunAlreadyActiveException} before anything is touched, and
 * {@link #startReplacingActive} is the explicit takeover.
 * <p>
 * The returned session has not shown the opening room yet - the caller renders it, running the rendering
 * through {@link AdventureRunSession#runBound} so the adventure's system-message overrides apply.
 */
@Service
public class AdventureRunSessionFactory {

    private final AdventureService adventureService;
    private final AdventureMapper adventureMapper;
    private final WorkflowMapper workflowMapper;
    private final AdventureConfig adventureConfig;
    private final GameContext gameContext;
    private final ActiveRun activeRun;

    public AdventureRunSessionFactory(AdventureService anAdventureService, AdventureMapper anAdventureMapper,
                                      WorkflowMapper aWorkflowMapper, AdventureConfig anAdventureConfig,
                                      GameContext aGameContext, ActiveRun anActiveRun) {
        adventureService = anAdventureService;
        adventureMapper = anAdventureMapper;
        workflowMapper = aWorkflowMapper;
        adventureConfig = anAdventureConfig;
        gameContext = aGameContext;
        activeRun = anActiveRun;
    }

    /**
     * Starts a run for the given owner.
     *
     * @throws RunAlreadyActiveException if this browser session already has an active run; nothing has been
     *                                   changed in that case
     */
    public AdventureRunSession start(AdventureData anAdventureData, RunOwner anOwner) {
        if (activeRun.isActive()) {
            throw new RunAlreadyActiveException();
        }
        return startRun(anAdventureData, anOwner);
    }

    /** The explicit takeover: ends the active run (its next input reports "ended in another tab") and starts anew. */
    public AdventureRunSession startReplacingActive(AdventureData anAdventureData, RunOwner anOwner) {
        activeRun.supersedeActive();
        return startRun(anAdventureData, anOwner);
    }

    /**
     * Releases the run if {@code anOwner} still owns it and clears this browser session's registries so a
     * finished game doesn't pin its adventure in memory. A no-op for an owner that no longer owns the run, so a
     * late release from an old view can't wipe a newer game.
     */
    public void release(RunOwner anOwner) {
        if (activeRun.release(anOwner)) {
            clearRegistries();
        }
    }

    private AdventureRunSession startRun(AdventureData anAdventureData, RunOwner anOwner) {
        Map<String, String> overrides = systemMessageOverrides(anAdventureData);
        // Loading, mapping and workflow set-up run with the adventure's overrides bound, exactly as when
        // LoadAdventureAction used to install them process-wide before mapping: anything constructed on the way
        // that reads SystemMessageKey text keeps seeing the adventure's own wording.
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(overrides)) {
            loadIntoSharedEngine(anAdventureData);

            Vocabulary vocabulary = adventureConfig.allWords();
            registerBaseVerbs(vocabulary);

            Workflow workflow = gameContext.setUpWorkflows();
            CommandFactory commandFactory = new CommandFactory(gameContext, anAdventureData.getVocabularyData());
            commandFactory.setUpWorkflowCommands(workflow);
            workflowMapper.populate(gameContext.getWorkflowData(), workflow);

            GameLoop gameLoop = new GameLoop(new Parser(vocabulary), gameContext);
            AdventureRunSession session = new AdventureRunSession(gameLoop, gameContext, overrides);
            activeRun.register(session, anOwner);
            return session;
        }
    }

    private void clearRegistries() {
        adventureConfig.allLocations().clear();
        adventureConfig.allItems().clear();
        adventureConfig.allContainers().clear();
        adventureConfig.allMessages().clear();
        adventureConfig.allVariables().clear();
        adventureConfig.allWords().setWords(List.of());
        // Also drop what the finished game left on the shared-per-session context, so it neither pins the old
        // adventure nor shows the old picture at the start of the next run in this browser session.
        gameContext.setCurrentLocation(null);
        gameContext.setPocket(null);
        gameContext.setCurrentPictureId(null);
    }

    private static Map<String, String> systemMessageOverrides(AdventureData anAdventureData) {
        Map<String, String> overrides = new HashMap<>();
        for (SystemMessageData override : anAdventureData.getSystemMessages().values()) {
            overrides.put(override.getKey(), override.getText());
        }
        return overrides;
    }

    // LoadAdventureAction signals success by throwing ReloadAdventureException and failure (bad
    // id, adventure not found, no locations) by returning normally — inverted from what you'd
    // expect.
    private void loadIntoSharedEngine(AdventureData anAdventureData) {
        LoadAdventureAction loadAdventureAction = new LoadAdventureAction(adventureService, adventureMapper,
                                                                          adventureConfig, gameContext);
        try {
            loadAdventureAction.loadAdventure(anAdventureData.getId());
        } catch (ReloadAdventureException expectedOnSuccess) {
            return;
        }
        throw new IllegalStateException(
                "Adventure '%s' could not be loaded to run — check it has at least one location."
                        .formatted(anAdventureData.getId()));
    }

    // A run session is scoped to one adventure.
    private void registerBaseVerbs(Vocabulary aVocabulary) {
        aVocabulary.createNewWord("quit", Word.Type.VERB);
        aVocabulary.createSynonym("exit", "quit");
        aVocabulary.createSynonym("bye", "quit");
        aVocabulary.createNewWord("describe", Word.Type.VERB);
        aVocabulary.createSynonym("look", "describe");
        aVocabulary.createSynonym("l", "describe");
        aVocabulary.createSynonym("desc", "describe");
        aVocabulary.createSynonym("examine", "describe");
        aVocabulary.createSynonym("x", "describe");
        aVocabulary.createNewWord("help", Word.Type.VERB);
        aVocabulary.createNewWord("inventory", Word.Type.VERB);
        aVocabulary.createSynonym("i", "inventory");
        aVocabulary.createNewWord("and", Word.Type.CONJUNCTION);
        aVocabulary.createSynonym("then", "and");
        aVocabulary.createNewWord("it", Word.Type.PRONOUN);
    }
}
```

- [ ] **Step 8: Keep the module compiling - minimal view and view-test adaptation**

`AdventureRunView` is the only caller of `start`, and Maven compiles the whole module, so adapt it now (the dialog, release and bound rendering come in Task 6).

In `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java`:
- add `import com.pdg.adventure.server.engine.RunOwner;`
- add the field `private final RunOwner runOwner = new RunOwner();` next to the other fields
- change `session = sessionFactory.start(adventureData);` to `session = sessionFactory.start(adventureData, runOwner);`

In `src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`:
- add `import com.pdg.adventure.server.engine.RunOwner;`
- run: `perl -pi -e 's/sessionFactory\.start\(adventureData\)/sessionFactory.start(eq(adventureData), any(RunOwner.class))/g' src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`

- [ ] **Step 9: Run the engine and view tests**

Run: `mvn test -Dtest='ActiveRunTest,AdventureRunSession*Test,SessionIsolationTest,AdventureRunViewTest' -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR|FAIL" | tail -12`
Expected: `BUILD SUCCESS`.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/engine src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java src/test/java/com/pdg/adventure/server/engine src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java
git commit -m "allow one active run per browser session, with owner-checked release and takeover" -m "Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ"
```

---

### Task 6: `AdventureRunView` uses the owner handle, the takeover dialog and bound rendering

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java`
- Modify test: `src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java`

**Interfaces:**
- Consumes: `RunOwner`, `RunAlreadyActiveException`, `AdventureRunSessionFactory#start(AdventureData, RunOwner)` / `#startReplacingActive` / `#release`, `AdventureRunSession#runBound` (Tasks 3, 5).
- Produces: a view that owns one `RunOwner`, shows a `ConfirmDialog` on conflict, releases on detach and on game over.

- [ ] **Step 1: Update the existing test file and write the failing tests**

In `AdventureRunViewTest.java`:

1. (Already done in Task 5 Step 8: the `start(eq(adventureData), any(RunOwner.class))` stubs and the `RunOwner` import.)
2. Add imports: `com.vaadin.flow.component.confirmdialog.ConfirmDialog`, `com.pdg.adventure.server.engine.RunAlreadyActiveException`, `java.util.function.Supplier`, `org.mockito.InOrder`.
3. In `stubOpeningRoom(String description, String pictureId)` add, after `when(session.getGameContext()).thenReturn(gameContext);`:

```java
        when(session.runBound(any())).thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
```
4. Add these tests:

```java
    // Mirrors production order: beforeEnter runs before the view is attached, so the conflict dialog is
    // opened from onAttach.
    private AdventureRunView enterWhileAnotherRunIsActive() {
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenThrow(new RunAlreadyActiveException());
        AdventureRunView conflicted = new AdventureRunView(sessionFactory, accessService, new VariableProvider());
        conflicted.beforeEnter(eventFor("author/adventures/adv-1/test"));
        UI.getCurrent().add(conflicted);
        return conflicted;
    }

    @Test
    void beforeEnter_whenAnotherRunIsActive_disablesInputAndOffersTheTakeover() {
        AdventureRunView conflicted = enterWhileAnotherRunIsActive();

        assertThat(find(MessageInput.class, conflicted).single().isEnabled()).isFalse();
        ConfirmDialog dialog = find(ConfirmDialog.class).single();
        assertThat(test(dialog).getText()).contains("another tab");
    }

    @Test
    void confirmingTheTakeover_startsReplacingTheActiveRun_andRendersTheOpeningRoom() {
        stubOpeningRoom("A grand throne room.");
        when(sessionFactory.startReplacingActive(eq(adventureData), any(RunOwner.class))).thenReturn(session);
        AdventureRunView conflicted = enterWhileAnotherRunIsActive();

        test(find(ConfirmDialog.class).single()).confirm();

        verify(sessionFactory).startReplacingActive(eq(adventureData), any(RunOwner.class));
        MessageList messageList = find(MessageList.class, conflicted).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
        assertThat(find(MessageInput.class, conflicted).single().isEnabled()).isTrue();
    }

    @Test
    void theOpeningRoom_isRenderedThroughTheSessionsBoundEntryPoint() {
        stubOpeningRoom("A grand throne room.");

        enterViaAuthorRoute();

        verify(session).runBound(any());
    }

    @Test
    void detachingTheView_releasesItsRun() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();

        UI.getCurrent().remove(view);

        verify(sessionFactory).release(any(RunOwner.class));
    }

    @Test
    void enteringAgainOnTheSameViewInstance_releasesItsOwnRunBeforeStartingAnew() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();

        view.beforeEnter(eventFor("author/adventures/adv-1/test"));

        InOrder order = inOrder(sessionFactory);
        order.verify(sessionFactory).start(eq(adventureData), any(RunOwner.class));
        order.verify(sessionFactory).release(any(RunOwner.class));
        order.verify(sessionFactory).start(eq(adventureData), any(RunOwner.class));
    }

    @Test
    void aConflictOnAnAlreadyAttachedView_opensTheDialogImmediately() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenThrow(new RunAlreadyActiveException());

        view.beforeEnter(eventFor("author/adventures/adv-1/test"));

        assertThat(find(ConfirmDialog.class).exists()).isTrue();
        assertThat(find(MessageInput.class, view).single().isEnabled()).isFalse();
    }

    @Test
    void gameOver_releasesTheRun() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(session.submit("quit")).thenReturn(new RunResult(List.of(SystemMessageKey.SM14.defaultText()), true));

        test(find(MessageInput.class, view).single()).send("quit");

        verify(sessionFactory).release(any(RunOwner.class));
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=AdventureRunViewTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR" | tail -10`
Expected: compilation error / failures (the view still calls `sessionFactory.start(adventureData)`).

- [ ] **Step 3: Implement the view changes**

In `AdventureRunView.java`:

Add imports: `com.vaadin.flow.component.AttachEvent`, `com.vaadin.flow.component.DetachEvent`, `com.vaadin.flow.component.confirmdialog.ConfirmDialog`, `com.pdg.adventure.server.engine.RunAlreadyActiveException` (`RunOwner` and the `runOwner` field already exist from Task 5 Step 8).

Add one field next to the other fields:

```java
    private boolean runConflict;
```

In `beforeEnter`, replace everything from the line `try {` that calls `session = sessionFactory.start(adventureData, runOwner);` through the closing `}` of the `beforeEnter` method (that is, the whole `try/catch`, the `MovePlayerAction` rendering and `refreshPictureDisplay();`) with the following, which also adds the new methods right after `beforeEnter`:

```java
        if (session != null) {
            // The router reuses this view instance when the same route is entered again (e.g. with another
            // adventure id). Its own earlier run must not count as "another game running".
            sessionFactory.release(runOwner);
            session = null;
        }
        try {
            session = sessionFactory.start(adventureData, runOwner);
        } catch (RunAlreadyActiveException _) {
            // Another game of this browser session is still active - possibly in a tab that was refreshed or
            // crashed, which Vaadin only notices after missed heartbeats. On a first entry the view is not
            // attached yet, so the dialog is opened from onAttach; a reused, attached instance opens it now.
            messageInput.setEnabled(false);
            if (isAttached()) {
                openRunConflictDialog();
            } else {
                runConflict = true;
            }
            return;
        } catch (RuntimeException e) {
            FlashNotifier.flash("Could not start the adventure: " + e.getMessage());
            forwardToOrigin(event);
            return;
        }
        renderOpeningRoom();
    }

    private void renderOpeningRoom() {
        MovePlayerAction movePlayerAction = new MovePlayerAction(session.getGameContext().getCurrentLocation(),
                                                                 session.getGameContext(), variableProvider);
        ExecutionResult result = session.runBound(movePlayerAction::execute);
        renderNarratorLines(List.of(result.getResultMessage()));
        refreshPictureDisplay();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (runConflict) {
            runConflict = false;
            openRunConflictDialog();
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        runOwner.markGone();
        sessionFactory.release(runOwner);
    }

    private void openRunConflictDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Game already running");
        dialog.setText("You already have a game running in another tab.");
        dialog.setConfirmButton("End the other game and start here", _ -> takeOverRun());
        dialog.setCancelable(true);
        dialog.setCancelButton("Back", _ -> navigateBack());
        dialog.open();
    }

    private void takeOverRun() {
        try {
            session = sessionFactory.startReplacingActive(adventureData, runOwner);
        } catch (RuntimeException e) {
            FlashNotifier.flash("Could not start the adventure: " + e.getMessage());
            navigateBack();
            return;
        }
        messageInput.setEnabled(true);
        renderOpeningRoom();
    }
```
The next original member after `beforeEnter` (the Javadoc of `applyFont`) follows directly; make sure the braces balance.

In `handleInput`, release when the game ends:

```java
    private void handleInput(final String input) {
        RunResult result = session.submit(input);
        renderNarratorLines(result.lines());
        refreshPictureDisplay();
        messageInput.setEnabled(!result.gameOver());
        if (result.gameOver()) {
            sessionFactory.release(runOwner);
        }
    }
```

- [ ] **Step 4: Run the view tests**

Run: `mvn test -Dtest=AdventureRunViewTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "Tests run:|BUILD|ERROR|FAIL" | tail -12`
Expected: `BUILD SUCCESS`, all tests pass. If `find(ConfirmDialog.class)` finds nothing, the dialog was not opened on attach: confirm that `UI.getCurrent().add(conflicted)` happens after `beforeEnter` in the helper (it does), and that `onAttach` ran.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java src/test/java/com/pdg/adventure/view/adventure/AdventureRunViewTest.java
git commit -m "run view: owner handle, takeover dialog, release on detach, bound opening room" -m "Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ"
```

---

### Task 7: Documentation and whole-suite verification

**Files:**
- Modify: `docs/specs/04-runtime-engine.md` (~lines 628-675 `AdventureRunSession` section; ~726-737 known gap)
- Modify: `docs/specs/02-functional-requirements.md` (~lines 484-492, ~550-556)
- Modify: `docs/specs/09-rebuild-blueprint.md` (~lines 214-222, ~366)

- [ ] **Step 1: Update `04-runtime-engine.md`**

In the "AdventureRunSession: the in-browser play surface" section:
- Change `1. \`AdventureRunSessionFactory.start(AdventureData)\`:` to `1. \`AdventureRunSessionFactory.start(AdventureData, RunOwner)\`:` and add as its first bullet: "Refuses with `RunAlreadyActiveException`, before touching any state, when this browser session already has an active run (`ActiveRun`); `startReplacingActive` is the explicit takeover and `release(RunOwner)` ends a run (owner-checked, clears the session's registries)."
- Replace the bullet "Returns an `AdventureRunSession` wrapping a fresh `GameLoop`. The caller must still submit the opening `look` ... takes)." with: "Returns an `AdventureRunSession` wrapping a fresh `GameLoop` and carrying the adventure's system-message overrides. The caller must still render the opening room - `AdventureRunView` executes a `MovePlayerAction` through `session.runBound(...)` so the overrides apply."
- In item 2 (`submit`) add: "Binds the session's system-message overrides (`SystemMessageKey.bindOverrides`) for the whole turn and unbinds in a `finally`."
- Replace the paragraph "**This reuses the process-wide `GameContext`/`AdventureConfig` singleton beans** — ... See [Known gaps](#known-gaps)." with: "`GameContext` and the six `AdventureConfig` registries are `@PerBrowserSession` scoped proxies (`vaadin-session` scope), so every browser session has its own engine state and several players can play at once. One game per browser session is enforced by the session-scoped `ActiveRun`; a refreshed or crashed tab can look alive for ~15 minutes (Vaadin detects dead UIs only through missed heartbeats), so `AdventureRunView` offers 'End the other game and start here'."
- Replace the known-gap bullet starting "**`GameContext`/`AdventureConfig` are process-wide singletons" (the whole bullet through "...instead of singleton injection.") with: "- **Engine state is per Vaadin session, not per tab.** Several concurrent games in one browser session are not supported (a second start is refused or must take over). Scoped beans can only be used on a thread with a bound Vaadin session, and session persistence/clustering (which would require the scoped beans to be serializable) is not supported."

- [ ] **Step 2: Update `02-functional-requirements.md`**

- Replace the "Constraint inherited from the engine" quote block (lines ~486-492, starting `> **Constraint inherited from the engine, not new to this view:**` through the sentence ending with the `See` link it references) with: "> **Isolation:** the engine state is scoped to the Vaadin session, so several players can run games at once. Within one browser session only one run is active; starting a second shows a dialog offering to end the other game."  (Keep the surrounding blank lines and any link target line that follows if it is a separate sentence fragment; read the lines first and remove the orphaned `See ...` remainder.)
- Replace the known-gap bullet "**Single active run session, server-wide.** ..." (lines ~551-556) with: "- **One active run per browser session.** Engine state is per Vaadin session; a second run in the same browser session must explicitly take over the first."

- [ ] **Step 3: Update `09-rebuild-blueprint.md`**

- Replace the sentence "Note that `GameContext` (and the `AdventureConfig` beans it reaches) are ordinary Spring singletons in the current code: there is no per-session isolation, so only one Test/Run session is meaningfully active at a time server-wide. Preserve this constraint knowingly, or design it away — see [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps)." with: "Note that `GameContext` (and the `AdventureConfig` registries it reaches) are `@PerBrowserSession` scoped proxies, so each Vaadin session has its own engine state — see [`04-runtime-engine.md` § AdventureRunSession](04-runtime-engine.md#adventurerunsession-the-in-browser-play-surface)."
- In the known-gaps table, replace the **Medium** row about "process-wide singletons — no per-session engine isolation" with: `| **Low** | Engine state is per Vaadin session, not per tab; no session persistence | [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps) |`.

- [ ] **Step 4: Verify no stale statements remain**

Run: `grep -rnE "process-wide|per-session engine isolation|at most one (Test|Run|run)|installOverrides|overridesByKeyId|start\(AdventureData\)" docs/specs src/main/java | grep -v "docs/superpowers"`
Expected: no output, except the `setOutputSink` Javadoc if it still says process-wide (fix it per Task 4 Step 3) and any `grep` hit that is part of a different, still-true sentence (inspect each).

- [ ] **Step 5: Run the whole suite**

Run: `mvn test 2>&1 | grep -E "Tests run:.*Fail|BUILD|ERROR|FAIL" | tail -25`
Expected: `BUILD SUCCESS`, `Failures: 0, Errors: 0`. A failure containing `No Scope registered for scope name 'vaadin-session'` or `No VaadinSession`/`Scope 'vaadin-session' is not active` points at a context or thread that touches a scoped bean without a bound session; fix it by adding `FakeSessionScopeConfig` (tests) or, if it is production code, stop and report - it means a code path outside a Vaadin session uses the engine.

- [ ] **Step 6: Manual smoke check (needs the running app and two browsers/profiles; not part of the automated gate)**

1. Log in as two different users in two browser profiles; start the same adventure in both; move in one and confirm the other is unaffected.
2. In one profile open the run page in two tabs: the second tab shows "Game already running"; "Back" returns to the library, "End the other game and start here" starts it and the first tab reports "This game was ended in another tab."
3. Press F5 on a running game. Either outcome is correct: if Vaadin has already closed the old UI the game simply starts again; if the old UI still looks alive the takeover dialog appears and confirming restarts the adventure. (Which one happens depends on how Vaadin 25.2 treats page unload, which the docs read while planning did not settle; do not "fix" a direct start.)

- [ ] **Step 7: Commit**

```bash
git add docs/specs
git commit -m "document per-session engine isolation" -m "Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01JpUTBpqGp6irWwjvUFnVgZ"
```

---

## Self-Review Notes

- **Spec coverage:** per-session scoping (Task 4), annotation/proxy mechanics (Task 4), `ActiveRun`/block/takeover/owner-checked release/registry clearing (Task 5), view behaviour incl. refresh case (Task 6), thread-local overrides (Task 2), session binding incl. opening room (Tasks 3, 6), docs (Task 7). Non-goals are untouched.
- **Type consistency:** `RunOwner`, `ActiveRun#release(RunOwner): boolean`, `AdventureRunSessionFactory#release(RunOwner): void`, `start`/`startReplacingActive(AdventureData, RunOwner)`, `AdventureRunSession#runBound(Supplier<T>)`, `#supersede()`, `ENDED_ELSEWHERE_TEXT` are used with identical names in Tasks 3, 5, 6.
- **Known unknown:** whether `Map` registry beans proxy cleanly is verified by Task 4 Step 4; the spec's fallback (small named registry classes) applies if it does not.
