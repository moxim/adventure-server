# 04 — Runtime Engine

## Purpose

This chapter explains how the system *plays* an adventure: how typed input becomes
a parsed command, how that command is dispatched, and how `Action` and
`PreCondition` co-operate through the engine's data model. It also catalogs every
concrete `Action` (16) and `PreCondition` (11) so a rebuild reproduces the
behaviour faithfully. Of these, 15 Actions and 10 PreConditions are directly
selectable in the authoring UI (see
[`07-ui-and-navigation.md` § Action editor factory](07-ui-and-navigation.md#action-editor-factory));
`LoadAdventureAction` is engine-managed rather than author-placed, and
`NotCondition` is applied structurally via a per-row **Negate** toggle
instead of being one of the 10 selectable kinds.

The data shapes that back this chapter are documented in
[`03-domain-model.md`](03-domain-model.md). The persistence path that loads them
is in [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md).

## Top-level turn pipeline

A single player turn flows through the engine like this:

```
input string
   │
   ▼
Parser.handle(line)
   ├─ tokenise (Scanner, lowercase)
   ├─ vocabulary.findWord(token) → Word
   ├─ resolve synonym → canonical word
   └─ populate SimpleSentence(verb, adjective, noun)
   ▼
GenericCommandDescription (verb, adj, noun)
   │
   ▼
GameContext.preProcessCommands()        ← Workflow.preCommands run BEFORE input is consulted
   │
   ▼
GameContext.interceptCommands(cmd)      ← Workflow.interceptorCommands (help, inventory, quit, look)
   │   if matched (state != FAILURE) tell(message); next turn
   ▼
empty input check ("||")                 ← if all three slots empty, "I don't understand, please rephrase."
   ▼
CommandExecutor(pocket, location).execute(cmd)
   ├─ pocket.getMatchingCommandChain(cmd)
   ├─ location.getMatchingCommandChain(cmd)   (which descends into directions and items)
   ├─ filter chains by adjective if given
   ├─ if 0 matches → "I don't know how to do that."
   ├─ if >1 matches → "What do you want to <verb>?"
   └─ if exactly 1 → chain.execute()
   ▼
ExecutionResult { state, resultMessage, commandHasMatched }
   │
   ▼
GameContext.tell(resultMessage)
```

`QuitException` and `ReloadAdventureException` short-circuit the loop:

- `QuitException` caught in `GameLoop.run`: tell the message and stop looping.
- `ReloadAdventureException` rethrown by `GameLoop`; the outer driver
  (`MiniAdventure.run`) re-enters the loop with the new adventure.
- Any other `IOException` or `RuntimeException` is logged at ERROR and stops
  the loop.

`GameLoop.run` is the canonical implementation; the file is small and worth
keeping close at hand: `server/engine/GameLoop.java:30-71`.

## Core API contracts

The engine talks in terms of small interfaces in `com.pdg.adventure.api`:

| Interface | Contract |
|-----------|---------|
| `Action` | `ExecutionResult execute()`, `String getActionName()`. The unit of side effect. Always returns a non-null result. |
| `PreCondition` | `ExecutionResult check()`, `String getName()`. Returns `SUCCESS`/`FAILURE` and a message. |
| `Command` | A `CommandDescription` + an ordered `List<PreCondition>` + an ordered `List<Action>` (`GenericCommand`, the sole implementation). `execute()` runs preconditions in order and stops at the first failure (returning that failure); if all pass, it runs every action in order — a single failing action aborts the rest and returns that action's failure immediately, otherwise all action result messages are joined with newlines into one SUCCESS result. There is no separate "follow-up actions" concept; every action is just the next entry in the same list. |
| `CommandDescription` | A 3-slot key `(verb, adjective, noun)`; equality is by joined-string spec. |
| `CommandChain` | An ordered list of `Command`s sharing one description; `execute()` runs the first whose preconditions pass. |
| `HasCommands` | Implemented by anything that can match command descriptions: `Thing`, `GenericDirection`. Wraps a `GenericCommandProvider` via `CommandHandler`. |
| `Actionable` | The combination of `HasCommands` and ability to apply a command directly (used for the player's pocket and the current location). |
| `Containable` / `Container` | Items live in containers; containers expose `add`, `remove`, `contains`, `listContents`, `getMatchingCommandChain` (descends into children). |
| `Visitable` | Implemented by `Location`; tracks `timesVisited` and `lumen`. |
| `ExecutionResult` | `{ State (SUCCESS / FAILURE), String resultMessage, boolean commandHasMatched }`. |

## The Parser

`server/parser/Parser.java` owns input → `GenericCommandDescription` translation:

1. Read a line via `BufferedReader.readLine()`.
2. Lower-case and tokenise with `java.util.Scanner`.
3. For each token, `Vocabulary.findWord(token)` returns an `Optional<Word>`;
   unknown tokens are silently skipped.
4. If the word has a synonym, **the synonym replaces it** (canonical
   resolution).
5. Populate a `SimpleSentence` based on `Word.Type`:
   - VERB → `verb`
   - NOUN → `noun`
   - ADJECTIVE → `adjective`
6. Wrap the result in `GenericCommandDescription(verb, adj, noun)`.

The parser **does not** identify clause boundaries, multi-noun objects, or
prepositions. It is deliberately minimal; richer NLP is roadmap (see
[Known gaps](#known-gaps)).

The empty input is represented as `||` (three empty slots joined by the
separator) — `GameLoop` checks for this exact string and emits *"I don't
understand, please rephrase."*.

## CommandHandler and command lookup

Every `Thing` and `GenericDirection` composes a `CommandHandler`
(`server/parser/CommandHandler.java`):

- Wraps a `GenericCommandProvider` (a map of command-spec → `CommandChain`).
- Adds, removes, and queries commands.
- Provides an **examine fallback**: if a verb matches the adventure's
  `examineWord` (configured via `CommandFactory.applyExamineFallback`) and no
  command chain matches, an `ExamineFallbackAction` is returned that emits the
  thing's long description. This makes "examine X" / "look at X" work without
  every authored thing carrying its own examine command.
- `applyCommand(description)` runs every matching chain in turn and returns
  the last result, with `commandHasMatched` flagged. (`CommandExecutor` is the
  primary entry point at runtime; `applyCommand` is used by tests and
  inner-thing dispatch.)

## CommandExecutor

`server/parser/CommandExecutor.java` orchestrates per-turn dispatch:

1. Collect chains matching the input from the **player's pocket**.
2. Append chains from the **current location** (which descends into directions
   and items).
3. `reduceCommandChains` — drop any chain whose first command has an adjective
   different from the input's. Empty chains are removed.
4. If 0 chains remain → return failure with `"I don't know how to do that."`.
5. If >1 chains remain → return failure with `"What do you want to <verb>?"`.
6. If exactly 1 → execute it.
7. Empty `resultMessage` is normalised by `clarifyExecutionOutcome`:
   - SUCCESS empty → `"OK."`
   - FAILURE empty → `"You can't do that."`

This is the "guess what the user meant" routine; the message templates are
deliberately generic so the surrounding game text supplies most of the
narrative.

## Workflow: pre-commands and interceptors

The `Workflow` (`server/engine/Workflow.java`) holds two `TreeMap<CommandDescription, Command>`s:

- **preCommands** — executed at the *start* of every turn, before reading
  input. Used today only for the *prompt* command (`MessageAction("What now? > ")`).
- **interceptorCommands** — consulted *after* parsing input but *before*
  pocket/location dispatch. Match by exact `CommandDescription`. Used for
  global verbs:

| Verb | Default interceptor behaviour |
|------|-------------------------------|
| `help` | `MessageAction` printing the canned help text. |
| `inventory` | `InventoryAction` listing the player's pocket. |
| `quit` | `QuitAction` (raises `QuitException`). |
| `describe` (and `describe here`) | `DescribeAction` printing the current location's long description. |
| `load` (target state) | `LoadAdventureAction` — see `MiniAdventure.addAdventureIdsToNouns`. |

Interceptors that succeed end the turn with their message; failure (the default
`CommandExecutionResult`) lets the dispatcher fall through to pocket/location
matching.

## CommandFactory: wiring conventions

`CommandFactory.java` is the canonical source for *how* the engine assembles
commands for a thing or workflow. A rebuild MUST preserve these wirings — they
are not optional book-keeping; they are part of the game's behaviour.

### Look / describe

```java
thing.addCommand(new GenericCommand(
    new GenericCommandDescription("describe", thing),
    new DescribeAction(thing::getLongDescription, allMessages)));
```

### Take / Drop (with worn handling)

For each item, `setUpTakeCommands(item)` registers four commands on the item:

1. **`get` (already-carried)** — `MessageAction(message[-13])`. Pre-condition:
   `CarriedCondition`. Reads as: *"You already have it."*
2. **`get` (success)** — `TakeAction` (delegates to `MoveItemAction(item, pocket)`).
   Pre-conditions: `Not(Carried)` AND `HereCondition`.
3. **`drop` (worn)** — `DropAction`, then a second action appended to the
   same command's action list, `RemoveAction` (move to current-location
   container, then un-wear). Pre-condition: `WornCondition`.
4. **`drop` (plain)** — `DropAction`. Pre-conditions: `Not(Worn)` AND
   `Carried`.

This is the canonical example of a multi-action command: dropping a worn
item performs the drop AND the unwear in one player turn, because
`RemoveAction` is simply the second entry in that command's `actions` list
(`dropAndRemoveCommand.addAction(...)` in `CommandFactory`) — see
[§ Core API contracts](#core-api-contracts) for how `execute()` walks that
list.

### Wear / Remove

`setUpWearCommands(item)` flips `isWearable=true` and registers:

1. **`wear`** — `WearAction`. Pre-condition: `Carried`.
2. **`remove`** — `RemoveAction`. Pre-condition: `Carried`.

### Examine fallback

`applyExamineFallback(things)` registers, on each thing, a *fallback* triggered
by the configured `examineWord` if no specific command matches; the fallback
emits the thing's long description.

### Workflow

`setUpWorkflowCommands(workflow)` plants the global help, inventory, quit, and
describe-location commands described above, plus a `MessageAction("What now? > ")`
prompt as a pre-command.

## Action catalog

Every `Action` extends `AbstractAction` (which extends `IdedAction`) and is
constructed with a `MessagesHolder`. `getActionName()` returns the simple class
name; equality is name-based, intentionally allowing two distinct instances of
the same kind to behave equivalently.

| Action | One-line role |
|--------|---------------|
| `MessageAction(text)` | Emit a literal text as SUCCESS. |
| `DescribeAction(supplier)` | Emit `supplier.get()` (used for thing & location descriptions). AI augmentation is wired but commented out. |
| `TakeAction(item, pocket, msgs)` | Move `item` into the pocket via `MoveItemAction`. Used by the `get` command. |
| `DropAction(item, container, msgs)` | Move `item` into the supplied container via `MoveItemAction`. Used by the `drop` commands. |
| `MoveItemAction(item, dest, msgs)` | The primitive: remove the item from its parent if any, add it to `dest` if not full. Emits `messages[-9]` (success) or `messages[-8]` (full). |
| `WearAction(wearable, msgs)` | If `isWearable && !isWorn`, set `isWorn=true`. Otherwise emit `messages[-6]`. |
| `RemoveAction(wearable, msgs)` | Inverse of `WearAction`; clears `isWorn`. |
| `MovePlayerAction(destination, msgs, gameContext)` | Set `gameContext.currentLocation = destination`, run `DescribeAction(destination::getLongDescription)`, increment `timesVisited`. |
| `InventoryAction(consumer, pocketSupplier, msgs)` | Print `messages[-10]` followed by `pocket.listContents()`. |
| `QuitAction(msgs)` | Throw `QuitException`. |
| `LoadAdventureAction(service, mapper, config, gameContext)` | Resolve an `adventureId`, load and map the `AdventureData`, throw `ReloadAdventureException` to restart the engine. |
| `SetVariableAction(name, value, vars, msgs)` | Write `Variable(name, value)` into the `VariableProvider`. |
| `IncrementVariableAction(name, vars, msgs)` | Read the variable, parse it as an integer, write `+1`. |
| `DecrementVariableAction(name, vars, msgs)` | Same, `-1`. |
| `CreateAction(thing, containerSupplier, msgs)` | Add `thing` to the supplied container; on success emits `messages[-12]`. Authorable via the "Create Item" action editor. |
| `DestroyAction(thing, msgs)` | Remove `thing` from its current parent container; on success emits `messages[-11]`. Authorable via the "Destroy" action editor. |

`ExamineFallbackAction` (in `server/parser/`) is the synthetic action used by
`CommandHandler.getMatchingCommandChain` when no authored command matches the
configured examine verb; it has no DO and is never persisted.

### Action behavioural detail

- **`MessageAction`** has two forms in practice: a literal string supplied at
  construction (used widely) and a runtime lookup against `MessagesHolder.getMessage(id)`.
  The negative numeric ids (`"-6"`, `"-8"`, `"-9"`, `"-10"`, `"-13"`) are
  reserved engine messages for take/drop/wear feedback.
- **`DescribeAction`** uses a `Supplier<String>` so the description is computed
  at execute time. The current implementation simply returns
  `target.get()`; the commented-out `fillThroughAI(...)` calls Spring AI
  Ollama with a fantasy-novelist system prompt to elaborate on 100 words. A
  rebuild SHOULD make this a configurable enhancement layer rather than an
  inlined branch.
- **`MoveItemAction`** is the only action that mutates the world's container
  graph. Capacity is enforced via `Container.getMaxSize() vs getSize()`.
- **`MovePlayerAction`** also describes the destination — moving and looking
  are deliberately one user-perceptible event.
- **`LoadAdventureAction`** is the engine's adventure-switching primitive. Its
  current invocation is wired by `MiniAdventure.addAdventureIdsToNouns`, which
  registers each known adventure's id as a noun in the active vocabulary so
  the player can `load <ulid>`.

## PreCondition catalog

All conditions extend `AbstractCondition` (which extends `IdedAction` and
implements `PreCondition`). `getName()` returns the simple class name. Composite
conditions wrap others.

### Item / location predicates

| Condition | Returns SUCCESS when… |
|-----------|----------------------|
| `CarriedCondition(item, gc)` | `gc.pocket.contains(item)`. Failure message: `"You don't have a <short>."`. |
| `WornCondition(wearable)` | `wearable.isWorn() == true`. Failure message: `"You are not wearing <enriched>."`. |
| `HereCondition(item, gc)` | `gc.currentLocation.contains(item)`. Failure message: `"There is no <noun> here."`. |
| `ItemAtCondition(item, location, gc)` | The item is at the named location. |
| `PlayerAtCondition(location, gc)` | `gc.currentLocation.equals(location)`. |
| `ChanceCondition(chance)` | A fresh random integer in `[1, 100]` (`new Random().nextInt(100) + 1`, injectable via a package-private constructor for testing) is `<= chance`. Re-rolled on every attempt — a 20% chance is "roughly one in five tries," not "one in five players." No failure message of its own. |

### Variable comparators

All extend `AbstractVariableCondition` which throws `ConfigurationException`
when the variable is not defined.

| Condition | Returns SUCCESS when… |
|-----------|----------------------|
| `EqualsCondition(name, value, vars)` | `vars.get(name).aValue() == value`. |
| `GreaterThanCondition(name, value, vars)` | `vars.get(name)` parsed as integer is > `value`. |
| `LowerThanCondition(name, value, vars)` | `vars.get(name)` parsed as integer is < `value`. |
| `SameCondition(name1, name2, vars)` | The values of two variables are equal. |

### Composites

| Condition | Semantics |
|-----------|-----------|
| `NotCondition(inner)` | Inverts the inner result; clears the inner's message. It is the only composite — there is no `AndCondition` / `OrCondition`. In the authoring UI it is not a directly-selectable condition kind; every condition row carries a **Negate** checkbox, and `ConditionRow.toConditionData()` wraps the picked condition in a `NotConditionData` when checked, so authors never construct one explicitly. |

`Command.execute()` runs the condition list **in order** and stops at the first
failure, surfacing that condition's message. Authors can therefore order
conditions by message-quality, putting the most informative failure first.
All conditions in the list are combined with **AND**; "either of these"
logic is expressed as separate Command Chain variants (see
[§ CommandExecutor](#commandexecutor)) rather than an OR composite.

## GameContext and engine lifecycle

`GameContext` (`server/engine/GameContext.java`) is the runtime carrier:

- `currentLocation: Location`
- `pocket: Container`
- `workflow: Workflow`
- `tell(String)` / `show(Describable)` — IO sinks.
- `setUpWorkflows()` — instantiates a fresh `Workflow`.
- `preProcessCommands()` / `interceptCommands(cmd)` — delegate to the workflow.

`Workflow.preProcess` walks all preCommands and tells each result. Interceptors
are dispatched on exact command-description match.

A typical adventure boot, performed by `MiniAdventure.setup`:

1. Load `AdventureData` via `AdventureService.findAdventureById(...)`.
2. Map to `Adventure` via `AdventureMapper.mapToBO`.
3. `gameContext.setPocket(adventure.getPocket())`.
4. `gameContext.setCurrentLocation(adventure.getLocationMap().get(adventure.getCurrentLocationId()))`.
5. `gameContext.setUpWorkflows()`; `commandFactory.setUpWorkflowCommands(workflow)`.
6. `MiniAdventure.addAdventureIdsToNouns(...)` registers each stored
   adventure's id as a noun and wires `LoadAdventureAction`.
7. Hand off to `GameLoop.run(reader)`.

## IO

`server/engine/IO.java` (referenced by `GameContext.tell` and various actions)
is the static IO sink; in the CLI runner it writes to `System.out`. This is
also `GameContext.outputSink`'s **default** — `GameContext.setOutputSink(Consumer<String>)`
lets a caller redirect `tell()` output elsewhere, which is exactly how the
in-browser play surface captures gameplay text (see below).

## AdventureRunSession: the in-browser play surface

`server/engine/AdventureRunSession.java` and `AdventureRunSessionFactory.java`
give `AdventureRunView` (the Vaadin play screen — see
[`07-ui-and-navigation.md`](07-ui-and-navigation.md)) a turn-based API over the
same engine the CLI uses, without touching `GameLoop`/`GameContext` themselves:

1. `AdventureRunSessionFactory.start(AdventureData)`:
   - Loads the adventure into the shared engine via `LoadAdventureAction`
     (its inverted success signal — throwing `ReloadAdventureException` on
     success, returning normally on failure — is unwrapped into a plain
     `IllegalStateException` here so the Vaadin view doesn't have to know
     about it).
   - Registers a small set of always-available verbs directly on the
     `Vocabulary` (`quit`/`exit`/`bye`, `describe`/`look`/`l`/`desc`/`examine`/`x`,
     `help`, `inventory`/`i`) — independent of whatever special words the
     author has configured. This mirrors `MiniAdventure.createSpecialWords`
     minus adventure-switching (`addAdventureIdsToNouns`) and the
     cross-adventure `load X` workflow command, since a run session is
     scoped to one adventure.
   - Calls `commandFactory.setUpWorkflowCommands(workflow)` and
     `workflowMapper.populate(gameContext.getWorkflowData(), workflow)` —
     the author's own workflow commands (§ [Workflow](03-domain-model.md#workflow))
     are layered on top of the built-in ones.
   - Returns an `AdventureRunSession` wrapping a fresh `GameLoop`. The
     caller must still call `session.submit("look")` to render the opening
     room — the factory does not do this itself.
2. `AdventureRunSession.submit(String input)`:
   - Installs a capturing `Consumer<String>` via `gameContext.setOutputSink(...)`,
     runs `gameContext.preProcessCommands()` then `gameLoop.processCommand(input)`,
     collects the non-blank/non-prompt lines, and **always** clears the sink
     (`setOutputSink(null)`) in a `finally` block before returning.
   - Returns a `RunResult(List<String> lines, boolean gameOver)`.

**This reuses the process-wide `GameContext`/`AdventureConfig` singleton
beans** — there is no per-session engine isolation. That is not a new
limitation introduced by the Vaadin view; it is the same constraint
`MiniAdventure`'s console loop already had. It just becomes more visible now
that multiple browser users can each trigger a session concurrently. See
[Known gaps](#known-gaps).

## Exceptions used as control flow

| Exception | Where thrown | What it means |
|-----------|--------------|---------------|
| `QuitException` | `QuitAction` | The player has quit; the game loop ends. Carries an optional bye message. |
| `ReloadAdventureException` | `LoadAdventureAction` | The current adventure is being replaced; outer driver restarts the loop with the new adventure. |
| `AmbiguousCommandException` | (declared, used in domain helpers) | Multiple matches reduce to ambiguity; today the dispatcher emits a clarification message rather than throwing. |
| `ConfigurationException` | `AbstractVariableCondition.getVariable` and other setup paths | The adventure's data is internally inconsistent (missing variable, missing reference). |
| `ContainerFullException` | `GenericContainer.add` | A container is at capacity. |
| `ItemNotFoundException` / `NotContainableException` | container ops | Self-descriptive. |

`QuitException` and `ReloadAdventureException` are intentionally used as
control flow; the others are genuine error conditions caught at the call site
(no global `@ControllerAdvice`).

## Source pointers

- `src/main/java/com/pdg/adventure/api/{Action,PreCondition,Command,CommandDescription,CommandChain,Container,Containable,Wearable,Visitable,Actionable,HasCommands,ExecutionResult}.java`
- `src/main/java/com/pdg/adventure/server/parser/{Parser,CommandHandler,CommandExecutor,CommandMatcher,GenericCommand,GenericCommandDescription,GenericCommandProvider,GenericCommandChain,CommandExecutionResult,ExamineFallbackAction}.java`
- `src/main/java/com/pdg/adventure/server/engine/{GameLoop,GameContext,Workflow,ContainerSupplier,IO,AdventureRunSession,AdventureRunSessionFactory}.java`
- `src/main/java/com/pdg/adventure/server/action/*.java`
- `src/main/java/com/pdg/adventure/server/condition/*.java`
- `src/main/java/com/pdg/adventure/server/exception/*.java`
- `src/main/java/com/pdg/adventure/CommandFactory.java`
- `src/main/java/com/pdg/adventure/MiniAdventure.java`,
  `src/main/java/com/pdg/adventure/AdventureClient.java` —
  CLI runner and content composition for manual play.
- `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java` —
  the Vaadin consumer of `AdventureRunSession` (see
  [`07-ui-and-navigation.md`](07-ui-and-navigation.md)).

## Known gaps

- **Spring AI / Ollama enhancement.** `DescribeAction.fillThroughAI` is wired
  but never called (`DescribeAction.java:32`, the call is commented). The
  base URL is hardcoded (`http://www.pdg-software.com:11434`). A rebuild MUST
  inject the base URL via configuration, and SHOULD make the AI augmentation
  pluggable / opt-in per Adventure or per Location.
- **NLP parser.** The current parser is a token-bag with verb/adjective/noun
  slots; multi-noun, prepositions ("put X in Y"), and articles are unsupported.
  A pluggable interface should be defined so that a future implementation can
  replace `Parser.handle` without ripple changes.
- **Save / Load game state.** `VocabularyData.saveWord` and `loadWord` slots
  exist; `LoadAdventureAction` covers adventure-level reloading. There is no
  per-game *save state* (variables, container snapshot) yet, and
  `AdventureRunView`/`AdventureRunSession` do not wire `save`/`load` at all
  — a run session is one continuous sitting.
- **`AmbiguousCommandException`** is declared but not used by `CommandExecutor`,
  which emits a literal clarification string instead. Either retire the
  exception or route the message through it.
- **`GameContext`/`AdventureConfig` are process-wide singletons — no
  per-session engine isolation.** Both the CLI runner and
  `AdventureRunSessionFactory` (§ [AdventureRunSession](#adventurerunsession-the-in-browser-play-surface))
  share the same beans, so at most one Test/Run session is meaningfully
  active across the whole server at a time; a second concurrent session
  (another author testing, another player's tab) mutates the same
  `currentLocation`/`pocket`/`outputSink` state. `GameContext.setOutputSink`'s
  own doc comment flags this explicitly. A rebuild that wants concurrent
  play MUST scope `GameContext` (and the vocabulary/message/variable state
  it reaches through `AdventureConfig`) per session — e.g. request- or
  session-scoped beans, or an explicit session object threaded through the
  engine instead of singleton injection.
