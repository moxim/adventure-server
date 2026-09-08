# 04 — Runtime Engine

## Purpose

This chapter explains how the system *plays* an adventure: how a typed line becomes
a *sequence* of parsed sub-commands, how each is dispatched, and how `Action` and
`PreCondition` co-operate through the engine's data model. It also catalogs every
concrete `Action` (17) and `PreCondition` (11) so a rebuild reproduces the
behaviour faithfully. Of these, 16 Actions and 10 PreConditions are directly
selectable in the authoring UI (see
[`07-ui-and-navigation.md` § Action editor factory](07-ui-and-navigation.md#action-editor-factory));
`LoadAdventureAction` is engine-managed rather than author-placed, and
`NotCondition` is applied structurally via a per-row **Negate** toggle
instead of being one of the 11 selectable kinds.

The data shapes that back this chapter are documented in
[`03-domain-model.md`](03-domain-model.md). The persistence path that loads them
is in [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md).

## Top-level turn pipeline

A single player turn flows through the engine like this:

```
input string ("take sword and kill ogre.  drop it.")
   │
   ▼
Parser.handle(line) → CommandSequence
   ├─ withSpacedTerminators: split "." into its own token
   ├─ tokenise (Scanner, lowercase); unknown tokens skipped
   ├─ vocabulary.findWord(token) → Word; resolve synonym → canonical word
   ├─ CONJUNCTION word (and / then) or "." closes the current sub-command
   ├─ per sub-command, populate a SimpleSentence(verb, adjective, noun);
   │    PRONOUN "it" → last noun+adjective (else UnresolvedReferenceException)
   └─ closeSentence: infer a missing verb from the previous sub-command
   ▼
List<GenericCommandDescription>       ← one per conjunction/period-separated segment
   │
   ▼  for each sub-command, in order (stop at the first that fails):
   │
   ├─ GameContext.runProcesses()         ← Workflow Processes (+ the "What now?" (SM2) prompt)
   │                                        run once BEFORE each sub-command,
   │                                        not once per typed line
   ├─ empty-verb check → SM6 ("I was not able to understand any of that…"); stop the sequence
   ├─ CommandExecutor(pocket, location).execute(cmd)      ← LOCAL dispatch, tried first
   │      ├─ pocket.getMatchingCommandChain(cmd)  +  location.getMatchingCommandChain(cmd)
   │      ├─ reduceCommandChains: drop empty → reduce by adjective → reduce by noun
   │      │                       → keep only the best-ranked tier
   │      ├─ 0 chains  → FAILURE, message = SM8 ("I can't do that.")
   │      ├─ >1 chains → FAILURE, SM60 ("What should I <verb>?") / SM61 (input noun given)
   │      └─ exactly 1 → chain.execute()  (SUCCESS if any command in the chain applied, else FAILURE)
   ├─ FALLBACK — only if local dispatch returned FAILURE whose message == SM8.defaultText()
   │      (i.e. nothing local matched the verb at all — both the gate here and the
   │       0-chains path use SM8.defaultText(), so an author-edited SM8 can't break it):
   │      └─ GameContext.respondTo(cmd)  ← Workflow Responses table
   │             (built-ins: help, inventory, quit, describe, "describe here")
   │             exact (verb,adjective,noun) match; non-FAILURE ends the sub-command
   ▼
per sub-command, in GameLoop.runOneCommandSucceeded:
   non-FAILURE → tell(resultMessage); continue the sequence
   FAILURE     → tell(resultMessage) if non-empty, else tell(SM8); stop the sequence
```

Ordering note: `CommandExecutor` (the current location + the player's pocket)
is consulted **first**; Workflow **Responses** are a *fallback* reached only
when no local command chain matched the verb at all — `CommandExecutor`
returned `FAILURE` whose message equals `SystemMessageKey.SM8.defaultText()`
(*"I can't do that."*), the sentinel the 0-chains path writes. Both the
0-chains write and `GameLoop`'s gate use `.defaultText()`, so an author who
edits `SM8` in their catalog does not disturb the fallback. A local command that
*does* match — even one that then fails its preconditions, or is ambiguous
(`SM60`/`SM61`) — is reported as-is and the Response is never tried. The
built-in `help` / `inventory` / `quit` / `describe` Responses still work
because no location or item normally defines those verbs; but an authored
**location or item** command now *shadows* a Workflow Response that shares
its `(verb, adjective, noun)` — previously the Response always pre-empted
local dispatch. (Earlier revisions ran Responses before local dispatch, and
the Process pass sat above `Parser.handle` and fired once per typed line.)

`QuitException`, `ReloadAdventureException` and `UnresolvedReferenceException`
short-circuit:

- `QuitException` — caught in `GameLoop.processCommand`: tell the message,
  return `QUIT` (the loop ends).
- `UnresolvedReferenceException` — thrown by `Parser` when `it` has no
  antecedent: caught in `GameLoop.processCommand`, tell the message, `CONTINUE`.
- `ReloadAdventureException` — rethrown by `GameLoop.processCommand`;
  `AdventureRunSessionFactory.loadIntoSharedEngine` catches it (it is
  `LoadAdventureAction`'s inverted success signal — see
  [§ AdventureRunSession](#adventurerunsession-the-in-browser-play-surface)).
- Any other `RuntimeException` is logged at ERROR and returns `ERROR`.

`GameLoop.processCommand(String)` is the **only** per-line entry point. The
former `GameLoop.run(BufferedReader)` console read-loop has been deleted with
the CLI runner; `processCommand` now calls `GameContext.runProcesses()`
itself — once for **each** parsed sub-command of the line — rather than
relying on the caller to do it first. A conjunction-joined turn
("take x and drop y") therefore runs every Workflow Process once per
sub-command.

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
| `ExecutionResult` | `{ State, String resultMessage, boolean commandHasMatched }`. `State` is declared `FAILURE, SUCCESS` (a commented-out `UNSPECIFIED` placeholder sits first, reserved for a future tri-state; nothing depends on the ordinal order today). Every `PreCondition`, the `Wear`/`Remove` actions, `GenericCommandChain.execute` and `Location.applyCommand` now set the state **explicitly on both the pass and fail branch** instead of leaning on `CommandExecutionResult`'s `FAILURE` default — a readability/robustness pass with no behavioural change. |

## The Parser

`server/parser/Parser.java` owns line → `CommandSequence` translation. A
`CommandSequence` (`server/parser/CommandSequence.java`) is an immutable
`record(List<GenericCommandDescription> commands)` — one entry per
conjunction- or period-separated **sub-command**, in execution order (not to
be confused with a `CommandChain`, which is several candidates for the *same*
verb/adjective/noun).

`Parser.handle(String)`:

1. `withSpacedTerminators` rewrites each `"."` to `" . "` so the whitespace
   `Scanner` yields it as its own token (a `"."` can't be a vocabulary word).
2. Lower-case, then tokenise with `java.util.Scanner`.
3. For each token: `Vocabulary.findWord(token)` → `Optional<Word>`; unknown
   tokens are silently skipped. A word with a synonym is replaced by its
   canonical form.
4. A `"."` token, or a resolved word of type `CONJUNCTION` (`and`; `then` is
   its synonym), **closes the current sub-command** — if it has any content,
   it is finished via `closeSentence` and a fresh `SimpleSentence` starts.
5. Otherwise `populate` the current `SimpleSentence` by `Word.Type`:
   `VERB → verb`, `NOUN → noun`, `ADJECTIVE → adjective`; `PRONOUN` (`it`)
   sets `noun`/`adjective` from the parser's remembered last noun+adjective,
   or throws `UnresolvedReferenceException` if there is none yet.
6. `closeSentence` builds each `GenericCommandDescription(verb, adj, noun)`:
   if the sub-command has a noun but no verb and a previous sub-command had
   a verb, that **verb is inferred** from the previous one. It then updates
   the remembered `lastVerb` / `lastNoun` / `lastAdjective` from what was
   parsed (regardless of whether execution later succeeds).
7. A turn with nothing recognisable still yields exactly one (empty)
   sub-command, preserving `GameLoop`'s bare-verb contract.

The parser now **does** split on conjunctions/periods and resolves `it`, but
still does not handle multi-noun objects, prepositions ("put X in Y"), or
articles — see [Known gaps](#known-gaps).

An unrecognisable sub-command surfaces as a `GenericCommandDescription` with
an empty verb; `GameLoop.runOneCommandSucceeded` checks for that and emits
`SystemMessageKey.SM6` (*"I was not able to understand any of that. Please try
again."*), then stops the rest of the sequence.

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

`server/parser/CommandExecutor.java` dispatches **one sub-command**:

1. Collect chains matching it from the **player's pocket**.
2. Append chains from the **current location** (which descends into directions
   and items).
3. `reduceCommandChains` narrows the candidates in four passes:
   - **drop empty chains** (no commands);
   - **`reduceByAdjective`** — `GenericCommandProvider` treats an empty
     stored adjective as a wildcard. Drop chains with a non-empty,
     non-matching adjective; then, if any *exact*-adjective chain remains,
     also drop the wildcards so the specific one wins;
   - **`reduceByNoun`** — same wildcard/specificity rule for the noun (a
     bare `jump` chain must lose to a `jump sea` chain when the input names
     the sea);
   - **`reduceToBestRankedChains`** — when several candidates still overlap,
     rank each by how strongly its *own* currently-satisfied,
     precondition-gated commands back it and keep only the best tier:
     **1** = has a gated command whose actions really do something;
     **2** = nothing discriminating (an unconditional branch, or a real
     branch with no precondition of its own); **3** = has only a gated
     *excuse* (informational-only actions explaining why this candidate is
     wrong). Uses `PreCondition.isDeterministic()` (a `ChanceCondition` is
     skipped, never pre-rolled) and `Action.isInformationalOnly()`. A tie
     across all candidates is left as genuine ambiguity.
4. 0 chains → failure, `SystemMessageKey.SM8` (*"I can't do that."*).
5. >1 chains → failure; `SM60` (*"What should I &lt;verb&gt;?"*, no input
   noun) or `SM61` (input noun given), each `String.format`-filled.
6. exactly 1 → `chain.execute()`, returned as-is (no `clarifyExecutionOutcome`
   on this path). `GenericCommandChain.execute` returns `SUCCESS` if **any**
   command in the chain applied — joining only the non-blank result messages
   of the commands that succeeded — otherwise `FAILURE` carrying the last
   command's failure message.
7. On the 0/>1 paths only, `clarifyExecutionOutcome` fills an empty
   `resultMessage` (FAILURE → SM8; its `SUCCESS → SM15 "OK."` branch is
   unreachable, since those paths always carry `FAILURE`). The single-match
   path is no longer normalised: `GameLoop.runOneCommandSucceeded` returns
   early for any non-FAILURE result and tells its message verbatim, so a
   successful command whose actions produced no text now prints a **blank
   line** where the old `GameLoop` tail would have substituted `SM15
   "OK."`. An *empty* FAILURE message is still replaced with `SM8`.

The message templates are deliberately generic so the surrounding game text
supplies most of the narrative.

## Workflow: Processes and Responses

The `Workflow` (`server/engine/Workflow.java`) holds two
`TreeMap<CommandDescription, Command>`s. The domain names for the *authored*
entries in each are **Processes** and **Responses**
([`03-domain-model.md` § Workflow](03-domain-model.md#workflow)); the engine
names for the maps are `processes` and `responses`.

- **`processes` (Processes + the prompt)** — every entry executed once
  *before each parsed sub-command* (`GameLoop.processCommand` calls
  `GameContext.runProcesses()` at the top of its per-sub-command
  loop), in a deterministic order: alphabetical by verb, then adjective,
  then noun (an explicit `Comparator`, not the `TreeMap`'s own key
  ordering). `CommandFactory` plants one built-in: a
  `MessageAction(SystemMessageKey.SM2)` — *"What now?"* — keyed
  `("~", "~", "~")` so it sorts last and never collides with a real
  command. The author's Processes are layered on by
  `WorkflowMapper.populate`.
- **`responses` (Responses)** — consulted only as a *fallback*,
  after `CommandExecutor` (pocket + current location) has been tried and
  returned `FAILURE` with the `SM8` sentinel text — i.e. only when nothing
  local matched the sub-command's verb at all. Match by exact
  `CommandDescription`. `CommandFactory` plants these built-ins:

| Verb | Default Response behaviour |
|------|---------------------------|
| `help` | `MessageAction` printing the canned help text. |
| `inventory` | `InventoryAction` listing the player's pocket. |
| `quit` | `QuitAction` (raises `QuitException`). |
| `describe` (and `describe here`) | `DescribeAction` printing the current location's long description. |

  The author's Responses are layered on by `WorkflowMapper.populate`; one
  whose verb matches a built-in **replaces** it for that adventure (still
  true — no location or item defines these verbs, so local dispatch fails
  with `SM8` and the fallback reaches the Response). There is no built-in
  `load` Response — cross-adventure loading is done only by
  `AdventureRunSessionFactory` via `LoadAdventureAction`.

Because Responses are now a post-`CommandExecutor` fallback rather than an
interceptor, an authored **location or item** command that shares a
Response's `(verb, adjective, noun)` wins over that Response (local dispatch
succeeds, or fails with a *specific* message, so the fallback is never
reached). A Response is consulted only for verbs with no local handler; when
it is reached, a non-FAILURE result ends the sub-command, and a FAILURE
result (including the default empty `CommandExecutionResult` when no Response
matched) falls through to `GameLoop`'s FAILURE handling (its message if any,
else `SM8`). The runtime map is named `responses` and its lookup is
`Workflow.respondTo(cmd)` / `GameContext.respondTo(cmd)`. (The *persisted*
model field on `WorkflowData` keeps its older name `interceptorCommands` — a
document-schema rename is a separate migration.)

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

1. **`get` (already-carried)** — `MessageAction(SystemMessageKey.SM25, filled
   with the item description)` — *"I already have the %s."*. Pre-condition:
   `CarriedCondition`.
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

`setUpWorkflowCommands(workflow)` adds `help`, `inventory`, `quit`,
`describe` and `describe here` as **Responses** (`workflow.addResponse`), plus a
`MessageAction(SystemMessageKey.SM2)` — *"What now?"* — as a **Process**
(`workflow.addProcess`) keyed `("~", "~", "~")`. The author's own Processes and
Responses are layered on afterwards by `WorkflowMapper.populate` (see
[§ Workflow: Processes and Responses](#workflow-processes-and-responses)).

The built-in `describe` / `describe here` Response wraps a `DescribeAction`
that forces the **full first-visit** long description: it reads the current
`Location`'s `timesVisited`, temporarily sets it to `0` (so
`Location.getLongDescription()` returns `super.getLongDescription()` rather
than the abbreviated re-visit form), reads the description, then **restores
the real count**. An explicit `look` therefore no longer resets the
location's visit counter — earlier code restored it to `0`, which made every
subsequent auto-describe on re-entry behave as a first visit.

## Action catalog

Every `Action` extends `AbstractAction` (which extends `IdedAction`) and is
constructed with a `MessagesHolder`. `getActionName()` returns the simple class
name; equality is name-based, intentionally allowing two distinct instances of
the same kind to behave equivalently. Fixed engine feedback text now comes from
`SystemMessageKey.SMnn.defaultText().formatted(...)` rather than
`MessagesHolder` negative-id lookups; the SM ids below are the current wiring.

17 concrete kinds; all except `LoadAdventureAction` (engine-managed) are
author-placeable:

| Action | One-line role |
|--------|---------------|
| `MessageAction(text)` | Emit a literal text as SUCCESS. |
| `DescribeAction(supplier)` | Emit `supplier.get()` (used for thing & location descriptions). AI augmentation is wired but commented out. |
| `TakeAction(item, pocket, msgs)` | Move `item` into the pocket via `MoveItemAction`; emits `SM36` (taken) / `SM26` (not here). Used by the `get` command. |
| `DropAction(item, container, msgs)` | Move `item` into the supplied container via `MoveItemAction`, and automatically remove it from its parent container; emits `SM39` + the item description. Used by the `drop` commands. |
| `MoveItemAction(item, dest, msgs)` | The primitive: remove the item from its parent if any, add it to `dest` if not full. Emits `SM54` (moved) / `SM55` (full) / `SM56` (can't). |
| `WearAction(wearable, msgs)` | If `isWearable && !isWorn`, set `isWorn=true`, `SUCCESS` + `SM37`; else `FAILURE` + `SM40`. Interpolates `thing.getStrippedBasicDescription()` (article-less — the `SMnn` texts already carry *"the %s"*). |
| `RemoveAction(wearable, msgs)` | Inverse of `WearAction`; clears `isWorn`; `SUCCESS` + `SM38` / `FAILURE` + `SM41`. Also interpolates the *stripped* (article-less) description. |
| `MovePlayerAction(destination, msgs, gameContext)` | Set `gameContext.currentLocation = destination`, run `DescribeAction(destination::getLongDescription)`, increment `timesVisited`. |
| `InventoryAction(consumer, pocketSupplier, msgs)` | Print the carried-items header (`SM9`) followed by `pocket.listContents()`. |
| `QuitAction(msgs)` | Throw `QuitException` carrying the supplied bye message. |
| `LoadAdventureAction(service, mapper, config, gameContext)` | Resolve an `adventureId`, load and map the `AdventureData`, throw `ReloadAdventureException` on success (returns normally on failure). Engine-managed — no DO, no editor. |
| `SetVariableAction(name, value, vars, msgs)` | Write `Variable(name, value)` into the `VariableProvider`. |
| `IncrementVariableAction(name, vars, msgs)` | Read the variable, parse it as an integer, write `+1`. |
| `DecrementVariableAction(name, vars, msgs)` | Same, `-1`. |
| `CreateAction(thing, containerSupplier, msgs)` | Add `thing` to the supplied container; emits `SM58`. Authorable via the "Create Item" action editor. |
| `DestroyAction(thing, msgs)` | Remove `thing` from its current parent container; emits `SM57`. Authorable via the "Destroy" action editor. |
| `BreakAction(msgs)` | Stop execution of the current command chain immediately and return the supplied message as SUCCESS. Used to short-circuit multi-action chains when a condition is met. Authorable via the "Break" action editor. |

`ExamineFallbackAction` (in `server/parser/`) is the synthetic action used by
`CommandHandler.getMatchingCommandChain` when no authored command matches the
configured examine verb; it has no DO and is never persisted.

### Action behavioural detail

- **`MessageAction`** has two forms in practice: a literal string supplied at
  construction (used widely) and a runtime lookup against `MessagesHolder.getMessage(id)`.
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
- **`LoadAdventureAction`** is the engine's adventure-switching primitive,
  now called only by `AdventureRunSessionFactory.loadIntoSharedEngine` to
  load the chosen adventure into the shared engine. It signals success by
  **throwing** `ReloadAdventureException` and failure (bad id / not found /
  no locations) by **returning normally** — the factory unwraps this into a
  plain `IllegalStateException`. The old player-facing `load <ulid>` command
  went away with the CLI runner.

## PreCondition catalog

All conditions extend `AbstractCondition` (which extends `IdedAction` and
implements `PreCondition`). `getName()` returns the simple class name. Composite
conditions wrap others.

### Item / location predicates

| Condition | Returns SUCCESS when… |
|-----------|----------------------|
| `CarriedCondition(item, gc)` | `gc.pocket.contains(item)`. Failure message SM28 (default text `"I don't have one of those."`). |
| `WornCondition(wearable)` | `wearable.isWorn() == true`. Failure message SM50 (default text `"I'm not wearing the %s."`), filled with `getStrippedBasicDescription()` (article-less). |
| `HereCondition(item, gc)` | `gc.currentLocation.contains(item)`. Failure message SM26 (default text `"There isn't one of those here."`). |
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
- `workflowData: WorkflowData` — the author's Processes/Responses, awaiting `WorkflowMapper.populate`.
- `tell(String)` / `show(Describable)` — output sinks.
- `setOutputSink(Consumer<String>)` — redirects `tell()`; default
  `java.lang.IO::println` (JDK 25's built-in `java.lang.IO`, implicitly
  imported). Passing `null` restores the default.
- `setUpWorkflows()` — instantiates a fresh `Workflow`.
- `runProcesses()` / `respondTo(cmd)` — delegate to the workflow.

`Workflow.runProcesses()` walks all `processes` **in alphabetical (verb,
adjective, noun) order** and tells each result; `GameLoop.processCommand`
invokes it once per parsed sub-command. `Workflow.respondTo(cmd)` looks up an
exact `CommandDescription` match in `responses` and returns its `execute()`
result, or a default `FAILURE` `CommandExecutionResult` when nothing matches —
`GameLoop` calls this only after `CommandExecutor` has failed with the `SM8`
sentinel.

There is **no CLI runner** anymore — `MiniAdventure` and `AdventureClient`
were deleted, and there is no custom `IO` class (`Adventure.run()` survives
as a vestigial stub with its old `GameLoop` wiring commented out). The only
adventure boot today is `AdventureRunSessionFactory.start(AdventureData)` —
see the next section.

## AdventureRunSession: the in-browser play surface

`server/engine/AdventureRunSession.java` and `AdventureRunSessionFactory.java`
give `AdventureRunView` (the Vaadin play screen — see
[`07-ui-and-navigation.md`](07-ui-and-navigation.md)) a turn-based API over the
engine, without touching `GameLoop`/`GameContext` directly:

1. `AdventureRunSessionFactory.start(AdventureData)`:
   - Loads the adventure into the shared engine via `LoadAdventureAction`
     (its inverted success signal — throwing `ReloadAdventureException` on
     success, returning normally on failure — is unwrapped into a plain
     `IllegalStateException` here so the Vaadin view doesn't have to know
     about it).
   - `registerBaseVerbs` adds a small set of always-available words directly
     on the `Vocabulary` — `quit`/`exit`/`bye`,
     `describe`/`look`/`l`/`desc`/`examine`/`x`, `help`, `inventory`/`i`,
     plus `and` (`CONJUNCTION`, synonym `then`) and `it` (`PRONOUN`) — so
     compound commands and pronoun back-references work regardless of the
     author's own vocabulary/special-word setup. A run session is scoped to
     one adventure, so there is no adventure-switching / `load X` wiring.
   - Calls `commandFactory.setUpWorkflowCommands(workflow)` and
     `workflowMapper.populate(gameContext.getWorkflowData(), workflow)` —
     the author's own Processes and Responses
     (§ [Workflow](03-domain-model.md#workflow)) are layered on top of the
     built-in ones.
   - Returns an `AdventureRunSession` wrapping a fresh `GameLoop`. The
     caller must still submit the opening `look` to render the starting
     room — the factory does not do this itself. `AdventureRunView` does it
     via its own `handleInput("look")` (the same path a typed command
     takes).
2. `AdventureRunSession.submit(String input)`:
   - Installs a capturing `Consumer<String>` via `gameContext.setOutputSink(...)`,
     runs `gameLoop.processCommand(input)` (which now fires
     `gameContext.runProcesses()` itself, once per parsed
     sub-command — `submit` no longer calls it), collects the
     non-blank/non-prompt lines, and **always** clears the sink
     (`setOutputSink(null)`) in a `finally` block before returning.
   - Returns a `RunResult(List<String> lines, boolean gameOver)`.

**This reuses the process-wide `GameContext`/`AdventureConfig` singleton
beans** — there is no per-session engine isolation. It is visible now
that multiple browser users can each trigger a session concurrently. See
[Known gaps](#known-gaps).

## Exceptions used as control flow

| Exception | Where thrown | What it means |
|-----------|--------------|---------------|
| `QuitException` | `QuitAction` | The player has quit; `GameLoop.processCommand` tells the message and returns `QUIT`. Carries an optional bye message. |
| `ReloadAdventureException` | `LoadAdventureAction` | `LoadAdventureAction`'s inverted **success** signal. Rethrown by `GameLoop.processCommand`; caught by `AdventureRunSessionFactory.loadIntoSharedEngine`. |
| `UnresolvedReferenceException` | `Parser.populate` | The pronoun `it` was used with no antecedent noun this session. Caught by `GameLoop.processCommand`, which tells the message and continues. |
| `AmbiguousCommandException` | (declared, used in domain helpers) | Multiple matches reduce to ambiguity; today the dispatcher emits a clarification message (SM60 / SM61) rather than throwing. |
| `ConfigurationException` | `AbstractVariableCondition.getVariable` and other setup paths | The adventure's data is internally inconsistent (missing variable, missing reference). |
| `ContainerFullException` | `GenericContainer.add` | A container is at capacity. |
| `ItemNotFoundException` / `NotContainableException` | container ops | Self-descriptive. |

`QuitException`, `ReloadAdventureException` and `UnresolvedReferenceException`
are intentionally used as control flow; the others are genuine error
conditions caught at the call site (no global `@ControllerAdvice`).

## Source pointers

- `src/main/java/com/pdg/adventure/api/{Action,PreCondition,Command,CommandDescription,CommandChain,Container,Containable,Wearable,Visitable,Actionable,HasCommands,ExecutionResult}.java`
- `src/main/java/com/pdg/adventure/server/parser/{Parser,CommandSequence,CommandHandler,CommandExecutor,CommandMatcher,GenericCommand,GenericCommandDescription,GenericCommandProvider,GenericCommandChain,CommandExecutionResult,ExamineFallbackAction}.java`
- `src/main/java/com/pdg/adventure/server/engine/{GameLoop,GameContext,Workflow,ContainerSupplier,AdventureRunSession,AdventureRunSessionFactory}.java` — note there is no `IO` class (JDK 25's `java.lang.IO` is the default sink).
- `src/main/java/com/pdg/adventure/server/action/*.java`
- `src/main/java/com/pdg/adventure/server/condition/*.java`
- `src/main/java/com/pdg/adventure/server/exception/*.java` — incl. `UnresolvedReferenceException`.
- `src/main/java/com/pdg/adventure/server/storage/message/SystemMessageKey.java` — engine-text catalog.
- `src/main/java/com/pdg/adventure/CommandFactory.java`
- `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java` —
  the Vaadin consumer of `AdventureRunSession` (see
  [`07-ui-and-navigation.md`](07-ui-and-navigation.md)).

## Known gaps

- **Spring AI / Ollama enhancement.** `DescribeAction.fillThroughAI` is wired
  but never called (`DescribeAction.java:32`, the call is commented). The
  base URL is hardcoded (`http://www.pdg-software.com:11434`). A rebuild MUST
  inject the base URL via configuration, and SHOULD make the AI augmentation
  pluggable / opt-in per Adventure or per Location.
- **NLP parser.** The parser now splits a line into a `CommandSequence` on
  `and` / `then` / `.`, infers a missing verb from the previous sub-command,
  and resolves `it` to the last-mentioned noun. It is still a token-bag with
  verb/adjective/noun slots: multi-noun objects, prepositions ("put X in Y"),
  and articles are unsupported. A pluggable interface should be defined so a
  future implementation can replace `Parser.handle` without ripple changes.
- **Save / Load game state.** `VocabularyData.saveWord` and `loadWord` slots
  exist; `LoadAdventureAction` covers adventure-level reloading. There is no
  per-game *save state* (variables, container snapshot) yet, and
  `AdventureRunView`/`AdventureRunSession` do not wire `save`/`load` at all
  — a run session is one continuous sitting.
- **`AmbiguousCommandException`** is declared but not used by `CommandExecutor`,
  which emits a literal clarification string instead. Either retire the
  exception or route the message through it.
- **`GameContext`/`AdventureConfig` are process-wide singletons — no
  per-session engine isolation.** `AdventureRunSessionFactory`
  (§ [AdventureRunSession](#adventurerunsession-the-in-browser-play-surface))
  drives these shared beans, so at most one Test/Run session is meaningfully
  active across the whole server at a time; a second concurrent session
  (another author testing, another player's tab) mutates the same
  `currentLocation`/`pocket`/`outputSink` state. `GameContext.setOutputSink`'s
  own doc comment flags this explicitly. A rebuild that wants concurrent
  play MUST scope `GameContext` (and the vocabulary/message/variable state
  it reaches through `AdventureConfig`) per session — e.g. request- or
  session-scoped beans, or an explicit session object threaded through the
  engine instead of singleton injection.
