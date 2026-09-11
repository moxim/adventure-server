# Process arrival timing (A + B2) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix location-gated Workflow Processes (e.g. a "welcome message" or a cave trip-wire) firing on the wrong turn — currently they leak into the turn the player leaves a location and fail to fire on the turn they arrive — by (A) correcting the existing Processes table's timing and (B2) adding a new, additive arrival-triggered Processes table that fires exactly when a location is (re)described, matching the original PAW design this project is modeled on.

**Architecture:** `GameLoop.processCommand` currently runs `Workflow.processes` before dispatching each sub-command, so a `PlayerAtCondition` evaluates the pre-move location. Task 1 reorders that call to run after dispatch instead (matching PAW's Process 2: "after every time frame"). Tasks 2-3 add a second, parallel `Workflow.arrivalProcesses` table wired to fire from `MovePlayerAction.execute()` and the built-in "describe"/"look" Response (matching PAW's Process 1: "after a location is described") — additive, so `WorkflowData.commands`/`interceptorCommands` and every adventure's existing content keep working unmodified. Task 4 adds the author-facing editor for the new table, following the existing `CommandListEditorView` template exactly. Task 5 migrates "The Demo" adventure's `welcome_message` entry into the new table as a real-world proof.

**Tech Stack:** Java 21, Spring Boot, Spring Data MongoDB, Vaadin Flow, Lombok, JUnit 5 + AssertJ + Mockito, `com.vaadin.browserless.BrowserlessTest` for Vaadin view tests, `mongosh` for the data migration.

**Spec:** `docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md`

## Global Constraints

- No schema migration for existing data: `WorkflowData.arrivalProcesses` is a new `List<CommandData>` field: Spring Data Mongo defaults it to an empty list on every existing document.
- No behavior change for any existing Process or Response entry in any of the 3 adventures currently in Mongo (confirmed zero Process entries anywhere mutate variables — see spec, resolved question 1).
- Follow existing test conventions exactly: AssertJ `assertThat`, Mockito `@Mock`/`@ExtendWith(MockitoExtension.class)` for unit tests, `com.vaadin.browserless.BrowserlessTest` + `find(...)`/`test(...)` for Vaadin view tests (see `server/docs/superpowers/specs/reference-adventurebuilder-browserless-testing` conventions already used in `WorkflowEditorViewRoutingTest`).
- Run the full Maven suite after every task (`mvn test` from `server/`), not just the new tests — several existing tests (`GameLoopTest`, `WorkflowTest`, `WorkflowMapperTest`) exercise the exact methods being changed.
- Commit after each task passes.

---

### Task 1: Fix Process timing — run Processes after dispatch, not before

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/engine/GameLoop.java:41-46`
- Test: `src/test/java/com/pdg/adventure/server/engine/GameLoopTest.java`

**Interfaces:**
- Consumes: nothing new — `gameContext.runProcesses()`, `runOneCommandSucceeded(GenericCommandDescription)` already exist.
- Produces: nothing new — this task only changes call order inside an existing method.

- [ ] **Step 1: Write the failing tests**

Add to `GameLoopTest.java` (needs new imports: `com.pdg.adventure.server.condition.PlayerAtCondition`):

```java
import com.pdg.adventure.server.condition.PlayerAtCondition;
```

```java
@Test
void movingAway_doesNotFireAProcessGatedOnTheLocationJustLeft() {
    // Before this fix, GameLoop evaluated Processes BEFORE the sub-command (the move) executed,
    // so a Process gated on the room the player is leaving would still see them "there" and
    // wrongly fire on the very turn they left.
    MessagesHolder messages = new MessagesHolder();
    Location room = gameContext.getCurrentLocation();
    DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "cellar");
    cellarDescription.setLongDescription("A dark, damp cellar.");
    Location cellar = new Location(cellarDescription,
                                   new GenericContainer(new DescriptionProvider("cellar items"), 10));

    GenericCommandDescription roomOnlyDescription = new GenericCommandDescription("throne-room-only");
    GenericCommand roomOnlyProcess = new GenericCommand(roomOnlyDescription,
            new MessageAction("Welcome to the throne room.", messages));
    roomOnlyProcess.addPreCondition(new PlayerAtCondition(room, gameContext));
    workflow.addProcess(roomOnlyDescription, roomOnlyProcess);

    GenericCommandDescription descendDescription = new GenericCommandDescription("descend");
    workflow.addResponse(descendDescription,
            new GenericCommand(descendDescription, new MovePlayerAction(cellar, messages, gameContext)));
    vocabulary.createNewWord("descend", Word.Type.VERB);

    GameLoop.CommandOutcome outcome = gameLoop.processCommand("descend");

    assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
    assertThat(told.toString())
            .contains("A dark, damp cellar.")
            .doesNotContain("Welcome to the throne room.");
}

@Test
void movingInto_firesAProcessGatedOnTheDestination_onTheSameTurnAsTheMove() {
    // Mirror image of the above: a Process gated on the destination must fire on the very turn
    // that arrives, not one turn later.
    MessagesHolder messages = new MessagesHolder();
    DescriptionProvider cellarDescription = new DescriptionProvider("cellar", "cellar");
    cellarDescription.setLongDescription("A dark, damp cellar.");
    Location cellar = new Location(cellarDescription,
                                   new GenericContainer(new DescriptionProvider("cellar items"), 10));

    GenericCommandDescription cellarOnlyDescription = new GenericCommandDescription("cellar-only");
    GenericCommand cellarOnlyProcess = new GenericCommand(cellarOnlyDescription,
            new MessageAction("You shiver in the cold.", messages));
    cellarOnlyProcess.addPreCondition(new PlayerAtCondition(cellar, gameContext));
    workflow.addProcess(cellarOnlyDescription, cellarOnlyProcess);

    GenericCommandDescription descendDescription = new GenericCommandDescription("descend");
    workflow.addResponse(descendDescription,
            new GenericCommand(descendDescription, new MovePlayerAction(cellar, messages, gameContext)));
    vocabulary.createNewWord("descend", Word.Type.VERB);

    GameLoop.CommandOutcome outcome = gameLoop.processCommand("descend");

    assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
    assertThat(told.toString()).contains("You shiver in the cold.");
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn -pl . -am test -Dtest=GameLoopTest#movingAway_doesNotFireAProcessGatedOnTheLocationJustLeft+movingInto_firesAProcessGatedOnTheDestination_onTheSameTurnAsTheMove` (from `server/`)
Expected: `movingAway_...` FAILS (`told` contains "Welcome to the throne room."); `movingInto_...` FAILS (`told` does not contain "You shiver in the cold." on this turn).

- [ ] **Step 3: Reorder the two calls in `GameLoop.processCommand`**

In `GameLoop.java`, replace:

```java
            CommandSequence sequence = parser.handle(anInput);
            for (GenericCommandDescription command : sequence.commands()) {
                gameContext.runProcesses();
                if (!runOneCommandSucceeded(command)) {
                    break; // stop the sequence at the first sub-command that failed
                }
            }
            return CommandOutcome.CONTINUE;
```

with:

```java
            CommandSequence sequence = parser.handle(anInput);
            for (GenericCommandDescription command : sequence.commands()) {
                if (!runOneCommandSucceeded(command)) {
                    break; // stop the sequence at the first sub-command that failed
                }
                gameContext.runProcesses();
            }
            return CommandOutcome.CONTINUE;
```

Also update the class-level javadoc on `processCommand` (currently describes the old order):

```java
    /**
     * Runs one already-obtained line of input through the engine: parses it into a sequence of
     * sub-commands and, for each in turn, dispatches the sub-command and then runs the author's
     * workflow Processes (gameContext.runProcesses()) against the state that command left behind
     * - stopping at the first sub-command that fails. Processes run after dispatch, not before,
     * so a location-gated Process (e.g. PlayerAtCondition) evaluates the location as it stands
     * after that turn's move, not before it. This is the only entry point; the former
     * run(BufferedReader) console loop was removed with the CLI runner, and runProcesses() is
     * now called here rather than by the caller.
     */
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `mvn -pl . -am test -Dtest=GameLoopTest` (from `server/`)
Expected: PASS, all tests in the class including the two new ones.

- [ ] **Step 5: Run the full suite**

Run: `mvn test` (from `server/`)
Expected: PASS, no regressions (Task 1's spec resolution already confirmed no adventure content depends on the old order).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/engine/GameLoop.java src/test/java/com/pdg/adventure/server/engine/GameLoopTest.java
git commit -m "fix: run workflow Processes after sub-command dispatch, not before

A PlayerAtCondition-gated Process was evaluating the pre-move location,
so it fired on the turn the player left a location and not on the turn
they arrived. Swap the two calls in GameLoop.processCommand's loop so
Processes see the state left behind by that turn's own command."
```

---

### Task 2: Add the `arrivalProcesses` table (model, engine, mapper)

**Files:**
- Modify: `src/main/java/com/pdg/adventure/model/WorkflowData.java`
- Modify: `src/main/java/com/pdg/adventure/server/engine/Workflow.java`
- Modify: `src/main/java/com/pdg/adventure/server/engine/GameContext.java`
- Modify: `src/main/java/com/pdg/adventure/server/mapper/WorkflowMapper.java`
- Test: `src/test/java/com/pdg/adventure/server/engine/WorkflowTest.java`
- Test: `src/test/java/com/pdg/adventure/server/mapper/WorkflowMapperTest.java`

**Interfaces:**
- Produces: `WorkflowData.getArrivalProcesses(): List<CommandData>`; `Workflow.addArrivalProcess(GenericCommandDescription, Command)`, `Workflow.removeArrivalProcess(GenericCommandDescription, Command)`, `Workflow.runArrivalProcesses(): void`; `GameContext.runArrivalProcesses(): void`. Task 3 (`MovePlayerAction`, `CommandFactory`) and Task 4 (author UI) consume these exact names.

- [ ] **Step 1: Write the failing tests**

Add to `WorkflowTest.java`:

```java
@Test
void runArrivalProcesses_executesArrivalProcessesInAlphabeticalVerbOrder_regardlessOfInsertionOrder() {
    addArrivalProcess(new GenericCommandDescription("zoo"), "Zoo arrival message.");
    addArrivalProcess(new GenericCommandDescription("apple"), "Apple arrival message.");

    workflow.runArrivalProcesses();

    assertThat(told).containsExactly("Apple arrival message.", "Zoo arrival message.");
}

@Test
void runArrivalProcesses_doesNotExecuteRegularProcesses() {
    addProcess(new GenericCommandDescription("regular"), "Regular process message.");
    addArrivalProcess(new GenericCommandDescription("arrival"), "Arrival process message.");

    workflow.runArrivalProcesses();

    assertThat(told).containsExactly("Arrival process message.");
}

@Test
void runProcesses_doesNotExecuteArrivalProcesses() {
    addProcess(new GenericCommandDescription("regular"), "Regular process message.");
    addArrivalProcess(new GenericCommandDescription("arrival"), "Arrival process message.");

    workflow.runProcesses();

    assertThat(told).containsExactly("Regular process message.");
}

private void addArrivalProcess(GenericCommandDescription aDescription, String aMessage) {
    Command command = mock(Command.class);
    when(command.execute()).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS, aMessage));
    workflow.addArrivalProcess(aDescription, command);
}
```

Add to `WorkflowMapperTest.java`:

```java
@Test
void populate_addsMappedCommandsAsWorkflowArrivalProcesses_soRunArrivalProcessesExecutesThem() {
    CommandData commandData = new CommandData(new CommandDescriptionData("arrive||"));
    WorkflowData workflowData = new WorkflowData();
    workflowData.getArrivalProcesses().add(commandData);

    GenericCommandDescription runtimeDescription = new GenericCommandDescription("arrive", "", "");
    when(commandMapper.mapToBO(commandData)).thenReturn(command);
    when(command.getDescription()).thenReturn(runtimeDescription);
    when(command.execute()).thenReturn(
            new CommandExecutionResult(ExecutionResult.State.SUCCESS, "Welcome!"));

    GameContext gameContext = new GameContext();
    Workflow workflow = gameContext.setUpWorkflows();

    workflowMapper.populate(workflowData, workflow);

    gameContext.runArrivalProcesses();

    verify(commandMapper).mapToBO(commandData);
    verify(command).execute();
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `mvn test -Dtest=WorkflowTest,WorkflowMapperTest` (from `server/`)
Expected: compile failure (`addArrivalProcess`/`runArrivalProcesses`/`getArrivalProcesses` don't exist yet).

- [ ] **Step 3: Add the field to `WorkflowData`**

```java
@Data
public class WorkflowData {
    private List<CommandData> commands = new ArrayList<>();
    private List<CommandData> interceptorCommands = new ArrayList<>();
    private List<CommandData> arrivalProcesses = new ArrayList<>();
}
```

- [ ] **Step 4: Add the map and methods to `Workflow`**

```java
    private final Map<CommandDescription, Command> processes;
    private final Map<CommandDescription, Command> responses;
    private final Map<CommandDescription, Command> arrivalProcesses;
    private final GameContext gameContext;

    public Workflow(GameContext aGameContext) {
        processes = new TreeMap<>();
        responses = new TreeMap<>();
        arrivalProcesses = new TreeMap<>();
        gameContext = aGameContext;
    }

    public void addProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        processes.put(aCommandDescription, aCommand);
    }

    public void addResponse(GenericCommandDescription aCommandDescription, Command aCommand) {
        responses.put(aCommandDescription, aCommand);
    }

    public void addArrivalProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        arrivalProcesses.put(aCommandDescription, aCommand);
    }

    public void removeProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        processes.remove(aCommandDescription, aCommand);
    }

    public void removeResponse(GenericCommandDescription aCommandDescription, Command aCommand) {
        responses.remove(aCommandDescription, aCommand);
    }

    public void removeArrivalProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        arrivalProcesses.remove(aCommandDescription, aCommand);
    }

    public void runProcesses() {
        processes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(ALPHABETICAL))
                .forEach(commandEntry -> {
                    ExecutionResult result = commandEntry.getValue().execute();
                    gameContext.tell(result.getResultMessage());
                });
    }

    public void runArrivalProcesses() {
        arrivalProcesses.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(ALPHABETICAL))
                .forEach(commandEntry -> {
                    ExecutionResult result = commandEntry.getValue().execute();
                    gameContext.tell(result.getResultMessage());
                });
    }

    public ExecutionResult respondTo(CommandDescription aCommand) {
        ExecutionResult result = new CommandExecutionResult();
        Command command = responses.get(aCommand);
        if (command != null) {
            result = command.execute();
        }
        return result;
    }
```

Also update the class javadoc to document the third table:

```java
/**
 * An adventure's global commands, held as three tables:
 * <ul>
 *   <li><b>{@code processes}</b> — run automatically after every parsed sub-command,
 *       regardless of what the player typed ({@link #runProcesses()}).</li>
 *   <li><b>{@code arrivalProcesses}</b> — run automatically whenever the current location's
 *       description is (re)shown: on arrival via movement, and on an explicit look/describe
 *       ({@link #runArrivalProcesses()}). Re-fires on every redescribe of the same location by
 *       design (see docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md); an
 *       author who wants "only once" adds their own guard condition.</li>
 *   <li><b>{@code responses}</b> — a fallback table, consulted by {@link #respondTo} only
 *       when no location/pocket command matched the typed verb. Keyed by exact
 *       {@link CommandDescription}.</li>
 * </ul>
 * The domain terms are <i>Processes</i>, <i>Arrival Processes</i>, and <i>Responses</i> (see the
 * authoring UI).
 */
```

- [ ] **Step 5: Add the delegate to `GameContext`**

```java
    public void runProcesses() {
        workflow.runProcesses();
    }

    public void runArrivalProcesses() {
        workflow.runArrivalProcesses();
    }

    public ExecutionResult respondTo(CommandDescription aCommand) {
        return workflow.respondTo(aCommand);
    }
```

- [ ] **Step 6: Add the third loop to `WorkflowMapper.populate`**

```java
    public void populate(WorkflowData aWorkflowData, Workflow aWorkflow) {
        for (CommandData commandData : aWorkflowData.getCommands()) {
            Command command = commandMapper.mapToBO(commandData);
            CommandDescription description = command.getDescription();
            aWorkflow.addProcess((GenericCommandDescription) description, command);
        }
        for (CommandData commandData : aWorkflowData.getInterceptorCommands()) {
            Command command = commandMapper.mapToBO(commandData);
            CommandDescription description = command.getDescription();
            aWorkflow.addResponse((GenericCommandDescription) description, command);
        }
        for (CommandData commandData : aWorkflowData.getArrivalProcesses()) {
            Command command = commandMapper.mapToBO(commandData);
            CommandDescription description = command.getDescription();
            aWorkflow.addArrivalProcess((GenericCommandDescription) description, command);
        }
    }
```

- [ ] **Step 7: Run the tests to verify they pass**

Run: `mvn test -Dtest=WorkflowTest,WorkflowMapperTest` (from `server/`)
Expected: PASS, all tests in both classes.

- [ ] **Step 8: Run the full suite**

Run: `mvn test` (from `server/`)
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/pdg/adventure/model/WorkflowData.java \
        src/main/java/com/pdg/adventure/server/engine/Workflow.java \
        src/main/java/com/pdg/adventure/server/engine/GameContext.java \
        src/main/java/com/pdg/adventure/server/mapper/WorkflowMapper.java \
        src/test/java/com/pdg/adventure/server/engine/WorkflowTest.java \
        src/test/java/com/pdg/adventure/server/mapper/WorkflowMapperTest.java
git commit -m "feat: add arrivalProcesses as a third, additive Workflow table

New WorkflowData.arrivalProcesses list, Workflow.addArrivalProcess/
runArrivalProcesses, and a GameContext delegate - parallel to the
existing processes/responses tables, no changes to either. Not wired
to fire yet (Task 3); this is the plumbing only."
```

---

### Task 3: Fire arrival Processes on location arrival and explicit look/describe

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/action/MovePlayerAction.java`
- Create: `src/main/java/com/pdg/adventure/server/action/RunArrivalProcessesAction.java`
- Modify: `src/main/java/com/pdg/adventure/CommandFactory.java`
- Test: `src/test/java/com/pdg/adventure/server/action/MovePlayerActionTest.java`
- Test: `src/test/java/com/pdg/adventure/server/engine/GameLoopTest.java`

**Interfaces:**
- Consumes: `gameContext.runArrivalProcesses()` (Task 2).
- Produces: `RunArrivalProcessesAction` — a plain `Action`, constructed as `new RunArrivalProcessesAction(gameContext, messagesHolder)`, used only inside `CommandFactory`.

- [ ] **Step 1: Write the failing `MovePlayerActionTest`**

```java
@Test
void execute_runsArrivalProcesses_afterSettingTheNewLocation() {
    when(destination.getArrivalDescription()).thenReturn("A dark cave.");
    when(destination.getTimesVisited()).thenReturn(0L);

    new MovePlayerAction(destination, messagesHolder, gameContext).execute();

    org.mockito.InOrder inOrder = inOrder(gameContext);
    inOrder.verify(gameContext).setCurrentLocation(destination);
    inOrder.verify(gameContext).runArrivalProcesses();
}
```

Add `import static org.mockito.Mockito.inOrder;` to the existing static imports.

- [ ] **Step 2: Run to verify it fails**

Run: `mvn test -Dtest=MovePlayerActionTest` (from `server/`)
Expected: FAIL (`gameContext.runArrivalProcesses()` doesn't exist as a call in `MovePlayerAction` yet — either a `NoSuchMethodError`-shaped compile issue is avoided since `GameContext` already has the method from Task 2, so this actually fails as `Wanted but not invoked: gameContext.runArrivalProcesses()`).

- [ ] **Step 3: Wire the call into `MovePlayerAction.execute()`**

```java
    @Override
    public ExecutionResult execute() {
        gameContext.setCurrentLocation(destination);
        final DescribeAction describeAction = new DescribeAction(new Supplier<String>() {
            @Override
            public String get() {
                return destination.getArrivalDescription();
            }
        }, messagesHolder);
        ExecutionResult result = describeAction.execute();
        destination.setTimesVisited(destination.getTimesVisited() + 1);
        gameContext.runArrivalProcesses();
        return result;
    }
```

- [ ] **Step 4: Run to verify it passes**

Run: `mvn test -Dtest=MovePlayerActionTest` (from `server/`)
Expected: PASS.

- [ ] **Step 5: Create `RunArrivalProcessesAction`**

```java
package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

/**
 * Fires the adventure's arrival-triggered Processes (Workflow.arrivalProcesses) - appended to the
 * action list of anything that (re)describes the current location without going through
 * MovePlayerAction (e.g. the built-in "describe"/"look" Response), so a PlayerAtCondition-gated
 * arrival Process evaluates against the location actually being described.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RunArrivalProcessesAction extends AbstractAction {
    private final transient GameContext gameContext;

    public RunArrivalProcessesAction(GameContext aGameContext, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        gameContext = aGameContext;
    }

    @Override
    public ExecutionResult execute() {
        gameContext.runArrivalProcesses();
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
```

- [ ] **Step 6: Write the failing `GameLoopTest` cases for the built-in "describe"/"look" path**

```java
@Test
void describingCurrentLocation_firesAnArrivalProcessGatedOnIt() {
    GenericCommandDescription hereDescription = new GenericCommandDescription("hush");
    GenericCommand hereProcess = new GenericCommand(hereDescription,
            new MessageAction("The room is silent.", new MessagesHolder()));
    hereProcess.addPreCondition(new PlayerAtCondition(gameContext.getCurrentLocation(), gameContext));
    workflow.addArrivalProcess(hereDescription, hereProcess);

    GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe");

    assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
    assertThat(told.toString()).contains("The room is silent.");
}

@Test
void describingAgain_reFiresTheArrivalProcess_matchingTheOriginalPawDesign() {
    // Documented, intended behaviour (docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md,
    // resolved question 4): an arrival Process re-fires on every redescribe of its location,
    // including an explicit "describe"/"look" - not just the initial move. An author who wants
    // "only once" adds their own guard; the engine does not de-duplicate.
    GenericCommandDescription hereDescription = new GenericCommandDescription("hush");
    GenericCommand hereProcess = new GenericCommand(hereDescription,
            new MessageAction("The room is silent.", new MessagesHolder()));
    hereProcess.addPreCondition(new PlayerAtCondition(gameContext.getCurrentLocation(), gameContext));
    workflow.addArrivalProcess(hereDescription, hereProcess);

    GameLoop.CommandOutcome outcome = gameLoop.processCommand("describe and describe");

    assertThat(outcome).isEqualTo(GameLoop.CommandOutcome.CONTINUE);
    long occurrences = told.toString().lines().filter(line -> line.equals("The room is silent.")).count();
    assertThat(occurrences).isEqualTo(2);
}
```

- [ ] **Step 7: Run to verify they fail**

Run: `mvn test -Dtest=GameLoopTest#describingCurrentLocation_firesAnArrivalProcessGatedOnIt+describingAgain_reFiresTheArrivalProcess_matchingTheOriginalPawDesign` (from `server/`)
Expected: FAIL (the built-in "describe" Response doesn't run arrival Processes yet).

- [ ] **Step 8: Wire `RunArrivalProcessesAction` into `CommandFactory`'s built-in describe/look Response**

```java
        Action lookLocationAction = new DescribeAction(
                () -> gameContext.getCurrentLocation().getLongDescription(), allMessages);
        Action runArrivalProcessesAction = new RunArrivalProcessesAction(gameContext, allMessages);

        GenericCommandDescription lookCommandDescription = new GenericCommandDescription("describe");
        GenericCommand lookCommand = new GenericCommand(lookCommandDescription, lookLocationAction);
        lookCommand.addAction(runArrivalProcessesAction);
        aWorkflow.addResponse(lookCommandDescription, lookCommand);

        GenericCommandDescription lookCommandDescription2 = new GenericCommandDescription("describe", "here");
        GenericCommand lookCommand2 = new GenericCommand(lookCommandDescription2, lookLocationAction);
        lookCommand2.addAction(runArrivalProcessesAction);
        aWorkflow.addResponse(lookCommandDescription2, lookCommand2);
```

Add the import: `import com.pdg.adventure.server.action.RunArrivalProcessesAction;`

- [ ] **Step 9: Run to verify they pass**

Run: `mvn test -Dtest=GameLoopTest` (from `server/`)
Expected: PASS, all tests in the class including Task 1's and Task 3's new ones.

- [ ] **Step 10: Run the full suite**

Run: `mvn test` (from `server/`)
Expected: PASS.

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/pdg/adventure/server/action/MovePlayerAction.java \
        src/main/java/com/pdg/adventure/server/action/RunArrivalProcessesAction.java \
        src/main/java/com/pdg/adventure/CommandFactory.java \
        src/test/java/com/pdg/adventure/server/action/MovePlayerActionTest.java \
        src/test/java/com/pdg/adventure/server/engine/GameLoopTest.java
git commit -m "feat: fire arrival Processes on move and on explicit describe/look

MovePlayerAction now calls gameContext.runArrivalProcesses() right
after setting the new location; the built-in describe/look Response
does the same via a new RunArrivalProcessesAction. Matches PAW's
Process 1: fires on every (re)describe, including a subsequent
explicit look - by design, not de-duplicated (see the design doc)."
```

---

### Task 4: Author-facing editor for arrival processes

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/workflow/CommandListEditorView.java`
- Create: `src/main/java/com/pdg/adventure/view/workflow/ArrivalProcessesEditorView.java`
- Modify: `src/main/java/com/pdg/adventure/view/adventure/AdventureEditorView.java`
- Test: `src/test/java/com/pdg/adventure/view/workflow/ArrivalProcessesEditorViewRoutingTest.java`

**Interfaces:**
- Consumes: `CommandListEditorView`'s protected constructor (existing), `WorkflowData::getArrivalProcesses` (Task 2).
- Produces: `ArrivalProcessesEditorView` at route `author/adventures/:adventureId/arrival`.

- [ ] **Step 1: Add `ARRIVAL` to `CommandListType` and extend the verb-optional branch**

In `CommandListEditorView.java`:

```java
    public enum CommandListType {
        PROCESS,
        ARRIVAL,
        RESPONSE
    }
```

```java
        if (aCommandListType == CommandListType.PROCESS || aCommandListType == CommandListType.ARRIVAL) {
            verbSelector.setRequired(false);
        }
```

- [ ] **Step 2: Write the failing routing test**

Create `ArrivalProcessesEditorViewRoutingTest.java`, copying `WorkflowEditorViewRoutingTest.java`'s structure exactly (same imports, same `setUp`/`clearSecurityContext`, same `eventWithAdventureId` helper) but:
- `view = new ArrivalProcessesEditorView(adventureService, accessService);`
- a local `adventureWithOneArrivalProcess()` helper using `workflowData.getArrivalProcesses().add(...)` instead of `getCommands()`
- title assertion `"Arrival Processes for The Demo"`

```java
package com.pdg.adventure.view.workflow;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
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
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class ArrivalProcessesEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private ArrivalProcessesEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new ArrivalProcessesEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    private static AdventureData adventureWithOneArrivalProcess() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setLocationData(new HashMap<>());
        WorkflowData workflowData = new WorkflowData();
        workflowData.getArrivalProcesses().add(new CommandData(new CommandDescriptionData("welcome||")));
        adventure.setWorkflowData(workflowData);
        return adventure;
    }

    @SuppressWarnings("unchecked")
    private Grid<CommandData> grid(ArrivalProcessesEditorView view) {
        return (Grid<CommandData>) (Grid<?>) find(Grid.class, view).single();
    }

    @Test
    void beforeEnter_validAdventureId_populatesGridFromArrivalProcesses() {
        AdventureData adventure = adventureWithOneArrivalProcess();
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getPageTitle()).isEqualTo("Arrival Processes for The Demo");
        assertThat(test(grid(view)).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
```

- [ ] **Step 3: Run to verify it fails**

Run: `mvn test -Dtest=ArrivalProcessesEditorViewRoutingTest` (from `server/`)
Expected: compile failure (`ArrivalProcessesEditorView` doesn't exist yet).

- [ ] **Step 4: Create `ArrivalProcessesEditorView`**

```java
package com.pdg.adventure.view.workflow;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;

@Route(value = "author/adventures/:adventureId/arrival", layout = WorkflowMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class ArrivalProcessesEditorView extends CommandListEditorView {

    public ArrivalProcessesEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        super(anAdventureService, anAccessService, "Arrival Process", "Arrival Processes for ",
              "An arrival process runs automatically whenever this location's description is shown - " +
              "when the player arrives by moving here, and every time they explicitly look/describe it. " +
              "Add preconditions to control whether it actually does anything that time; an unmet " +
              "precondition simply does nothing that time, it is not an error, and it does not stop " +
              "the process from being checked again on the next redescribe.",
              "No arrival processes yet. Create one to get started.",
              CommandListType.ARRIVAL,
              WorkflowData::getArrivalProcesses);
    }
}
```

- [ ] **Step 5: Run to verify the routing test passes**

Run: `mvn test -Dtest=ArrivalProcessesEditorViewRoutingTest` (from `server/`)
Expected: PASS.

- [ ] **Step 6: Add the "Manage Arrival" button to `AdventureEditorView`**

Add the import:

```java
import com.pdg.adventure.view.workflow.ArrivalProcessesEditorView;
```

Add the button, alongside `workflowButton`/`responsesButton`:

```java
        Button arrivalButton = new Button("Manage Arrival", _ -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(ArrivalProcessesEditorView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())));
            }
        });
```

Add it to the existing layout:

```java
        final VerticalLayout workflowLayout = new VerticalLayout(workflowButton, responsesButton, arrivalButton);
```

- [ ] **Step 7: Run the full suite**

Run: `mvn test` (from `server/`)
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/workflow/CommandListEditorView.java \
        src/main/java/com/pdg/adventure/view/workflow/ArrivalProcessesEditorView.java \
        src/main/java/com/pdg/adventure/view/adventure/AdventureEditorView.java \
        src/test/java/com/pdg/adventure/view/workflow/ArrivalProcessesEditorViewRoutingTest.java
git commit -m "feat: add Manage Arrival authoring view for arrival processes

ArrivalProcessesEditorView follows the existing WorkflowEditorView/
ResponsesEditorView template exactly (CommandListEditorView subclass,
new CommandListType.ARRIVAL entry), routed at
author/adventures/:adventureId/arrival and reachable from a new
Manage Arrival button on AdventureEditorView."
```

---

### Task 5: Migrate "The Demo" adventure's `welcome_message` to the new table

**Files:**
- No source changes — Mongo data only.

**Interfaces:**
- Consumes: the running application's `adventures` database (`spring.mongodb.database=adventures`, see `server/src/main/resources/application.properties`).

- [ ] **Step 1: Capture the entry to move**

```bash
mongosh "mongodb://advAdmin:example@localhost:27017/adventures?authSource=admin" --quiet --eval '
var entry = db.adventures.findOne(
  { _id: "01kt1mckzwhnqa44w0f9xsf4zd" },
  { "workflowData.commands": { $elemMatch: { _id: "01kxwxgnm6y0azb3nxmwzjzzen" } } }
).workflowData.commands[0];
printjson(entry);
'
```

Expected: prints the `welcome_message` Process subdocument (verb `jump`, `PlayerAtConditionData` on the jetty, `MessageActionData` → `welcome_message`) — confirms the exact document to move before mutating anything.

- [ ] **Step 2: Move it — pull from `commands`, push to `arrivalProcesses`**

```bash
mongosh "mongodb://advAdmin:example@localhost:27017/adventures?authSource=admin" --quiet --eval '
var adventureId = "01kt1mckzwhnqa44w0f9xsf4zd";
var entryId = "01kxwxgnm6y0azb3nxmwzjzzen";
var doc = db.adventures.findOne({ _id: adventureId });
var entry = doc.workflowData.commands.find(c => c._id === entryId);
if (!entry) { throw new Error("entry not found - already moved?"); }

db.adventures.updateOne(
  { _id: adventureId },
  { $pull: { "workflowData.commands": { _id: entryId } } }
);
db.adventures.updateOne(
  { _id: adventureId },
  { $push: { "workflowData.arrivalProcesses": entry } }
);
print("moved.");
'
```

- [ ] **Step 3: Verify the move**

```bash
mongosh "mongodb://advAdmin:example@localhost:27017/adventures?authSource=admin" --quiet --eval '
var doc = db.adventures.findOne({ _id: "01kt1mckzwhnqa44w0f9xsf4zd" });
print("commands still has welcome entry: " + doc.workflowData.commands.some(c => c._id === "01kxwxgnm6y0azb3nxmwzjzzen"));
print("arrivalProcesses has welcome entry: " + (doc.workflowData.arrivalProcesses || []).some(c => c._id === "01kxwxgnm6y0azb3nxmwzjzzen"));
'
```

Expected: `commands still has welcome entry: false`, `arrivalProcesses has welcome entry: true`.

- [ ] **Step 4: Manual verification in the running app**

Start the app (see the project's `run` skill/instructions), log in as an author, open "The Demo," use "Test" to play it:
1. Confirm the welcome message shows on the first turn (at the jetty).
2. Move away from the jetty (the direction toward the dunes) — confirm the welcome message does **not** appear on that turn, only the new location's arrival text.
3. Return to the jetty and type "look"/"describe" — confirm the welcome message reappears (matches the documented re-fire-on-redescribe behavior).

No code or test changes in this step — this is the real-world proof the design doc set out to fix. If any of the three checks fail, stop and re-open investigation rather than adjusting the data further; the engine-level tests in Tasks 1-3 are the source of truth for correctness.

- [ ] **Step 5: No commit needed**

This task changes only the running Mongo instance's data, not the repository. Note the change in the session's own record (e.g. a brief mention in the final report) rather than a git commit.

---

## Self-review notes

- **Spec coverage:** Alternative A (Task 1), Alternative B2 (Tasks 2-3), the authoring UI implied by B2 (Task 4), and the concrete "welcome_message" proof case from the spec's Problem section (Task 5) are all covered. The spec's four resolved open questions are reflected directly: question 1 (no variable-mutating Process anywhere) justified skipping any extra migration-safety work in Task 1; question 2 (naming/routing) is Task 4 exactly; question 3 (the `"~ ~ ~"` sentinel) required no change, confirmed by Task 1's full-suite run still passing `WorkflowTest`'s two sentinel-ordering tests unmodified; question 4 (double-fire is expected) is directly encoded as an assertion in Task 3, not worked around.
- **Placeholder scan:** none — every step has real code or a real, copy-pasteable command.
- **Type consistency:** `runArrivalProcesses()` (no args, `void`) and `addArrivalProcess(GenericCommandDescription, Command)` are used with the same signatures across `Workflow`, `GameContext`, `WorkflowMapper`, `MovePlayerAction`, and `CommandFactory` throughout. `WorkflowData::getArrivalProcesses` matches the `SerializableFunction<WorkflowData, List<CommandData>>` accessor type `CommandListEditorView`'s constructor already requires (same shape as the existing `WorkflowData::getCommands`/`getInterceptorCommands` usages).
