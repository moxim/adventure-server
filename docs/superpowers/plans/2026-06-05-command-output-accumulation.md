# Command Output Accumulation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make a command surface output from *all* its actions, and make a command chain run *every* command whose preconditions pass — accumulating all messages instead of stopping at the first success.

**Architecture:** Two central methods change — `GenericCommand.execute()` (Level 1, across a command's actions) and `GenericCommandChain.execute()` (Level 2, across a chain's commands). Both collect non-blank result messages into a list and join them with `System.lineSeparator()` into the existing single-string `ExecutionResult.resultMessage` (Approach A). No interface, mapper, editor, or `GameLoop` changes; every execution path inherits the fix.

**Tech Stack:** Java, JUnit 5, AssertJ, Mockito (plain `mock()` helpers, no `MockitoExtension`), Maven. Run from `server/`.

**Spec:** `server/docs/superpowers/specs/2026-06-05-command-output-accumulation-design.md`

---

## File Structure

- Modify: `server/src/main/java/com/pdg/adventure/server/parser/GenericCommand.java` — `execute()` accumulates all action messages.
- Modify: `server/src/main/java/com/pdg/adventure/server/parser/GenericCommandChain.java` — `execute()` runs all applicable commands and accumulates.
- Modify: `server/src/test/java/com/pdg/adventure/server/parser/GenericCommandExecuteTest.java` — flip the one test that asserted first-success-only; add a blank-skip test.
- Create: `server/src/test/java/com/pdg/adventure/server/parser/GenericCommandChainTest.java` — new unit + scenario tests for chain accumulation.

All commands below assume working directory `/Users/mafw/workroom/projects/adventurebuilder/server`.

---

### Task 1: Level 1 — `GenericCommand.execute()` accumulates all action messages

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/server/parser/GenericCommand.java` (the `execute()` method)
- Test: `server/src/test/java/com/pdg/adventure/server/parser/GenericCommandExecuteTest.java`

- [ ] **Step 1: Update/add the failing tests**

In `GenericCommandExecuteTest.java`, **replace** the existing test `firstActionSuccessMessageSurfaces_laterSuccessesDiscarded` with the two tests below (keep all other tests and the `action(...)` / `precondition(...)` helpers unchanged):

```java
    @Test
    void allActionSuccessMessagesAreAccumulated() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "first"));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "second"));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("first" + System.lineSeparator() + "second");
    }

    @Test
    void blankActionMessagesAreSkippedWhenAccumulating() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "shown"));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, ""));   // side-effect-only action
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "also"));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("shown" + System.lineSeparator() + "also");
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn -q test -Dtest=GenericCommandExecuteTest`
Expected: FAIL — `allActionSuccessMessagesAreAccumulated` expects `"first\nsecond"` but the current code returns only `"first"`.

- [ ] **Step 3: Rewrite `GenericCommand.execute()`**

In `GenericCommand.java`, replace the entire `execute()` method with:

```java
    @Override
    public ExecutionResult execute() {
        ExecutionResult result = checkPreconditions();
        if (result.getExecutionState() != ExecutionResult.State.SUCCESS) {
            return result;                 // preconditions unmet → command does not apply
        }
        List<String> messages = new ArrayList<>();
        for (Action action : actions) {
            ExecutionResult fromAction = action.execute();
            if (fromAction.getExecutionState() == ExecutionResult.State.FAILURE) {
                result.setExecutionState(ExecutionResult.State.FAILURE);
                result.setResultMessage(fromAction.getResultMessage());
                return result;             // a failing action fails the whole command
            }
            String msg = fromAction.getResultMessage();
            if (msg != null && !msg.isBlank()) {
                messages.add(msg);
            }
        }
        result.setResultMessage(String.join(System.lineSeparator(), messages));
        return result;                     // SUCCESS, all action messages joined
    }
```

Leave `checkPreconditions()` and `setExecutionResult(...)` unchanged (`checkPreconditions` still uses `setExecutionResult`). `java.util.List` and `java.util.ArrayList` are already imported.

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn -q test -Dtest=GenericCommandExecuteTest`
Expected: PASS — all tests green, including the unchanged `firstActionFailure_shortCircuits`, `laterActionFailure_surfacesThatFailure` (partial output discarded, failure surfaced), and `preconditionFailure_skipsActions`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/parser/GenericCommand.java \
        src/test/java/com/pdg/adventure/server/parser/GenericCommandExecuteTest.java
git commit -m "feat: accumulate all action messages in GenericCommand.execute

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 2: Level 2 — `GenericCommandChain.execute()` runs every applicable command

**Files:**
- Create: `server/src/test/java/com/pdg/adventure/server/parser/GenericCommandChainTest.java`
- Modify: `server/src/main/java/com/pdg/adventure/server/parser/GenericCommandChain.java` (the `execute()` method)

- [ ] **Step 1: Create the failing test file**

Create `server/src/test/java/com/pdg/adventure/server/parser/GenericCommandChainTest.java`:

```java
package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

class GenericCommandChainTest {

    private static Action action(String message) {
        Action a = mock(Action.class);
        ExecutionResult r = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        r.setResultMessage(message);
        when(a.execute()).thenReturn(r);
        return a;
    }

    private static PreCondition precondition(ExecutionResult.State state) {
        PreCondition p = mock(PreCondition.class);
        when(p.check()).thenReturn(new CommandExecutionResult(state));
        return p;
    }

    /** A single-action command, optionally gated by one precondition. */
    private static GenericCommand command(PreCondition precond, String message) {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        if (precond != null) {
            cmd.addPreCondition(precond);
        }
        cmd.addAction(action(message));
        return cmd;
    }

    @Test
    void runsEveryApplicableCommand_andJoinsMessages() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(null, "one"));
        chain.addCommand(command(null, "two"));

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("one" + System.lineSeparator() + "two");
    }

    @Test
    void skipsCommandsWhosePreconditionsFail_runsTheRest() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(precondition(ExecutionResult.State.FAILURE), "suited"));   // skipped
        chain.addCommand(command(precondition(ExecutionResult.State.SUCCESS), "no-suit"));  // runs
        chain.addCommand(command(null, "also-here"));                                        // always runs

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("no-suit" + System.lineSeparator() + "also-here");
    }

    @Test
    void whenNoCommandApplies_resultIsFailure() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(precondition(ExecutionResult.State.FAILURE), "never"));

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void blankMessagesAreSkippedWhenJoining() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(null, "shown"));
        chain.addCommand(command(null, ""));      // side-effect-only command
        chain.addCommand(command(null, "again"));

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("shown" + System.lineSeparator() + "again");
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q test -Dtest=GenericCommandChainTest`
Expected: FAIL — `runsEveryApplicableCommand_andJoinsMessages` returns only `"one"` because the current `execute()` breaks on first success.

- [ ] **Step 3: Rewrite `GenericCommandChain.execute()`**

In `GenericCommandChain.java`, replace the entire `execute()` method with:

```java
    @Override
    public ExecutionResult execute() {
        List<String> messages = new ArrayList<>();
        boolean anyApplied = false;
        ExecutionResult last = new CommandExecutionResult();   // FAILURE / empty default
        for (Command command : commands) {
            ExecutionResult fromCommand = command.execute();
            last = fromCommand;
            if (fromCommand.getExecutionState() == ExecutionResult.State.SUCCESS) {
                anyApplied = true;
                String msg = fromCommand.getResultMessage();
                if (msg != null && !msg.isBlank()) {
                    messages.add(msg);
                }
            }
        }
        ExecutionResult result = new CommandExecutionResult();
        if (anyApplied) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
            result.setResultMessage(String.join(System.lineSeparator(), messages));
        } else {
            result.setResultMessage(last.getResultMessage());  // surface last failure; empty → "You can't do that."
        }
        return result;
    }
```

`java.util.List`, `java.util.ArrayList`, `Command`, and `ExecutionResult` are already imported; `CommandExecutionResult` is in the same package.

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn -q test -Dtest=GenericCommandChainTest`
Expected: PASS — all four tests green.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/parser/GenericCommandChain.java \
        src/test/java/com/pdg/adventure/server/parser/GenericCommandChainTest.java
git commit -m "feat: run all applicable commands in GenericCommandChain.execute

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 3: Acceptance scenario — the `jump sea` example

**Files:**
- Modify: `server/src/test/java/com/pdg/adventure/server/parser/GenericCommandChainTest.java` (add one scenario test)

- [ ] **Step 1: Add the scenario test mirroring the authored data**

Append this test inside `GenericCommandChainTest` (it reuses the `command(...)` / `precondition(...)` helpers). It models the three `jump · sea` commands from the spec: WORN→ok, NOT_WORN→no-suit, and a no-precondition "also here".

```java
    @Test
    void jumpSeaScenario_noSuit_showsNoSuitAndAlsoHere() {
        // Player is NOT wearing the suit: WORN command is skipped,
        // NOT_WORN command applies, and the no-precondition command always applies.
        GenericCommandChain jumpSea = new GenericCommandChain();
        jumpSea.addCommand(command(precondition(ExecutionResult.State.FAILURE), "jump_sea_ok"));        // WORN: skipped
        jumpSea.addCommand(command(precondition(ExecutionResult.State.SUCCESS), "jetty_jump_sea_no_suit")); // NOT_WORN
        jumpSea.addCommand(command(null, "jump_sea_also_here"));                                         // always

        ExecutionResult result = jumpSea.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("jetty_jump_sea_no_suit" + System.lineSeparator() + "jump_sea_also_here");
    }

    @Test
    void jumpSeaScenario_suitOn_showsOkAndAlsoHere() {
        // Player IS wearing the suit at the jetty: WORN command applies,
        // NOT_WORN is skipped, and the no-precondition command always applies.
        GenericCommandChain jumpSea = new GenericCommandChain();
        jumpSea.addCommand(command(precondition(ExecutionResult.State.SUCCESS), "jump_sea_ok"));         // WORN
        jumpSea.addCommand(command(precondition(ExecutionResult.State.FAILURE), "jetty_jump_sea_no_suit")); // NOT_WORN: skipped
        jumpSea.addCommand(command(null, "jump_sea_also_here"));                                         // always

        ExecutionResult result = jumpSea.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("jump_sea_ok" + System.lineSeparator() + "jump_sea_also_here");
    }
```

- [ ] **Step 2: Run the scenario tests**

Run: `mvn -q test -Dtest=GenericCommandChainTest`
Expected: PASS — both scenario tests green (they pass with the Task 2 implementation; no production change needed).

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/pdg/adventure/server/parser/GenericCommandChainTest.java
git commit -m "test: acceptance scenario for jump-sea multi-message output

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 4: Full-suite verification

**Files:** none (verification only)

- [ ] **Step 1: Run the complete test suite**

Run: `mvn test`
Expected: BUILD SUCCESS, 0 failures, 0 errors. (Prior baseline this session was ~708 tests; this plan adds ~8 and modifies 1.)

- [ ] **Step 2: If anything fails, migrate the assertion**

Any failure here will be a test that asserted the *old* first-success-only behavior (analysis found only `GenericCommandExecuteTest`, already handled in Task 1). For each such failure, update the expectation to the accumulated, `System.lineSeparator()`-joined output — do **not** revert the production change. Re-run `mvn test` until green.

- [ ] **Step 3: Commit any migration fixes (only if Step 2 changed files)**

```bash
git add -A
git commit -m "test: migrate assertions to accumulated command output

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Notes / open decision

- **Failing action within a command** uses the spec's default: stop the command, discard that command's partial output, mark it FAILURE (Task 1, `GenericCommand.execute`). This was flagged as uncertain — if manual play or new tests show useful output being lost, revisit the spec's "Open decision" (keep prior messages, or continue past the failure) before changing it.
- **Out of scope:** the inter-object "Which xyz?" false-duplicate (Level 3), structured `List<String>` results, and the pre-existing duplication across the three `applyCommand`/`execute` paths.
