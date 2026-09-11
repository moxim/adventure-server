# Process table: fire location-gated entries on arrival, not on every pre-move turn

## Problem

An author-configured Workflow **Process** (`workflowData.commands`, `Workflow.processes`) whose only precondition is "the player is at location X" (`PlayerAtConditionData`/`PlayerAtCondition`) is meant to represent "something that is true, or something that happens, while the player is standing there" — a welcome banner at the start location, an NPC-presence announcement, a trip-wire hazard in a dark cave. Confirmed live in "The Demo" adventure (`01kt1mckzwhnqa44w0f9xsf4zd`):

```
workflowData.commands[0]:
  commandDescription.verb = "jump"        // unused for matching — see below
  preConditions = [ PlayerAtCondition(locationId = 01kt1mt3t3jmjc18kw23sv8g08) ]  // the jetty = start location
  actions       = [ MessageAction(welcome_message) ]                              // "Hello, welcome to the demo adventure."
```

This fires correctly while the player is at the jetty, but also fires on the turn the player issues the command that **leaves** the jetty — the welcome message appears interleaved with the new location's arrival text, reading as if it fired after the player left.

### Root cause, traced in code

`GameLoop.processCommand` (`server/src/main/java/com/pdg/adventure/server/engine/GameLoop.java:41-46`) runs Processes **before** dispatching each sub-command:

```java
for (GenericCommandDescription command : sequence.commands()) {
    gameContext.runProcesses();                 // ALL Processes run first
    if (!runOneCommandSucceeded(command)) {      // THEN the command (e.g. move) executes
        break;
    }
}
```

`Workflow.runProcesses()` (`server/src/main/java/com/pdg/adventure/server/engine/Workflow.java:61-68`) runs **every** entry in the `processes` map unconditionally, every call — a Process's `commandDescription` is used only to sort iteration order (`ALPHABETICAL`, lines 26-33), never to filter against what the player typed. `PlayerAtCondition.check()` (`server/src/main/java/com/pdg/adventure/server/condition/PlayerAtCondition.java:24-32`) reads `gameContext.getCurrentLocation()` live — there's no caching bug. The only actual location move happens later, inside `MovePlayerAction.execute()` (`server/src/main/java/com/pdg/adventure/server/action/MovePlayerAction.java:29`), which is reached from `runOneCommandSucceeded` → `CommandExecutor` (`GameLoop.java:84-85`) — strictly *after* that sub-command's `runProcesses()` call already ran.

So on the turn the player types e.g. "go north": `runProcesses()` evaluates `PlayerAtCondition(jetty)` while the player is still, technically, at the jetty (the move hasn't happened yet) → true → welcome message fires → *then* the move executes and the new location's arrival text prints. Same mechanism, mirrored: on the turn the player *enters* a location, the Process runs first, while the player is still at the *old* location — so it does **not** fire on arrival, only from the following turn onward. Both directions are wrong; a "trip wire in a dark cave" Process would announce nothing on entry and then wrongly re-trigger on the turn the player leaves.

A second, identically-shaped entry already exists in the same adventure — `heavy_sand` (`workflowData.commands[1]`: `ChanceCondition(40%)` + `PlayerAtCondition(dunes)` → message), nominally tied to the "drop" verb but, per the above, actually evaluated on *every* turn spent at the dunes regardless of verb. Not the reported bug, but the same root cause, second instance.

### What the reference design got right

This project's origin, `server/docs/specs/ProfessionalAdventureWriter_TechnicalGuide.html`, describes the same primitive under a different, split shape, and — per the user, verified against the original system — it never had this bug:

> **Process 1** "is scanned by PAW after a location is described, to allow any additional information which forms a part of the location description to be displayed."
>
> **Process 2** "is scanned by PAW after every time frame. That is after every phrase extracted from the player's input, or after every time-out on input" — used for "the movements and actions of PSI's [NPCs], the uncontrolled events such as bridges collapsing."
>
> "The Verb and Noun used for each entry in Process 1 and 2 ... have no meaning as they are ignored" — confirming AdventureBuilder's "ignore the verb" behavior is *intentional and faithful*, not itself the bug.

Main-loop shape: `Describe Current Location → Search Process Table 1 → [loop: Get Phrase → Search Response → Search Process Table 2 → loop]`. The `DESC` action (used by movement) "will cancel ... and make a jump to describe the current location" — i.e. back to the top, so Process 1 always runs immediately after the (re)describe that follows a move, never before it. The guide's own worked example is structurally identical to this bug: an NPC's presence announcement, `SANEC / SAME 20 38 [is he here?] / MESSAGE 5 [he's here]`, is explicitly placed in Process 1 "which is called after every describe of a location" — the same "AT location → announce/react" shape as `welcome_message` and the cave/trip-wire example the user described:

```
AT cave
NOT_CARRIED torch
MESSAGE trip_wire_tripped
DECREMENT_VARIABLE Health 25
```

AdventureBuilder collapsed PAW's two tables into one (`workflowData.commands`) and hooked it to the wrong event — "before every parsed sub-command" instead of "after the location is (re)described."

## Approaches considered

**A — Reorder only: run Processes after the sub-command dispatches, not before.**
Swap the two lines in `GameLoop.processCommand`'s loop (`GameLoop.java:41-46`):
```java
for (GenericCommandDescription command : sequence.commands()) {
    if (!runOneCommandSucceeded(command)) {
        break;
    }
    gameContext.runProcesses();               // now evaluates the resulting state
}
```
Fixes both directions: leaving the jetty, `runProcesses()` now sees the new location → welcome message correctly stops firing; entering the cave, it now sees the cave → trip wire correctly fires on arrival instead of one turn late. Two-line change, no schema/model change, no data migration. Does **not** change *scope*: a location-gated Process still re-evaluates after literally every sub-command spent there (e.g. typing "inventory" while in the cave still re-decrements Health), same as today — matching PAW's stated "verb is ignored" behavior for *both* its tables, but not matching PAW's actual per-table cadence (Process 1 only re-fires on a redescribe, not on every unrelated command). `heavy_sand` keeps working unmodified — no re-authoring needed anywhere.

**B1 — Re-point the existing Processes table to fire on the describe/arrival event, matching PAW's Process 1 exactly.**
Remove the `gameContext.runProcesses()` call from `GameLoop.processCommand` entirely; call it instead from wherever a location is actually (re)described:
- `MovePlayerAction.execute()` (`MovePlayerAction.java:27-39`), after `gameContext.setCurrentLocation(destination)` (line 29) — i.e. exactly where `destination.getArrivalDescription()` is already built.
- The built-in "describe"/"look" Response (`CommandFactory.setUpWorkflowCommands`, `server/src/main/java/com/pdg/adventure/CommandFactory.java:60-68`), which calls `gameContext.getCurrentLocation().getLongDescription()`.

This is the literal PAW Process 1 mapping: fires once per describe (arrival *and* explicit "look"), never on a non-describing command. Matches the trip-wire's "announced right as you walk in" framing precisely, and the guide's own NPC-presence example verbatim. But it changes what "Process" *means* for every existing entry, including `heavy_sand` — dropping an item at the dunes no longer triggers a describe, so `heavy_sand` would stop firing at all unless it's re-authored as a **Response** on "drop" instead (which is a strictly better fit for it anyway, and the exact pattern `stirring_up_sand` already uses correctly in the same adventure's `interceptorCommands`). No schema change (`Command`/`PreCondition`/`Action` types are untouched, `WorkflowData.commands` keeps its current shape) — but it is a behavior change for any other adventure's existing Processes, and requires auditing/re-authoring any that rely on "fires on a specific verb, anywhere" rather than "fires on describe." **Superseded by B2 below** — kept here for the record since it's what the recommendation originally was, before the option of a genuinely separate table was raised.

**B2 — Add a new table: a real Process 1 analog, additive alongside the existing one (this is what PAW actually did, and what we should do too).**
Rather than reinterpreting `workflowData.commands`, give it a sibling. Confirmed cheap in this codebase's actual shape, not just in principle:
- `WorkflowData` (`server/src/main/java/com/pdg/adventure/model/WorkflowData.java:9-11`) is a plain Lombok `@Data` class with `List<CommandData> commands` / `interceptorCommands`. A third `List<CommandData> arrivalProcesses` is a one-line addition — Spring Data Mongo defaults a missing field to an empty list on every existing document, so this needs no migration, not even a lazy one.
- `Workflow` (`Workflow.java`) gets a third `Map<CommandDescription, Command> arrivalProcesses`, with `addArrivalProcess`/`removeArrivalProcess` mirroring the existing `addProcess`/`addResponse` pairs (lines 45-59), and a `runArrivalProcesses()` mirroring `runProcesses()` (lines 61-68) — same shape, third instance.
- `WorkflowMapper.populate` (`WorkflowMapper.java:25-36`) gets a third loop over `aWorkflowData.getArrivalProcesses()`, calling `aWorkflow.addArrivalProcess(...)` — copy of the existing two loops.
- `GameContext` gets a `runArrivalProcesses()` delegate (mirroring lines 74-76), called from `MovePlayerAction.execute()` and the built-in describe Response — same two call sites B1 needed, just adding rather than replacing.
- **Authoring UI is templated, not new design.** `WorkflowEditorView`/`ResponsesEditorView` already share a `CommandListEditorView` base parameterized by a `CommandListType` and a `WorkflowData::getX` accessor (`WorkflowEditorView.java:10-24`). A third `ArrivalProcessesEditorView` is the same ~12-line shape as `WorkflowEditorView`, plus a `CommandListType.ARRIVAL_PROCESS` entry and a nav link in `WorkflowMainLayout`/`WorkflowEditorView`'s route siblings.

Nothing existing changes behavior: `heavy_sand` keeps working exactly as today, unmodified, in `commands`. The new table is purely an additional option authors reach for when they want "on arrival" semantics specifically — which is what `welcome_message` and the cave/trip-wire pattern actually are. This still leaves `commands` (the existing table) with its *own* timing bug relative to PAW's Process 2 (which is scanned "after every time frame," i.e. after that turn's action, not before) — so **B2 does not replace A, it complements it**: A's reorder should land regardless, fixing the existing table's timing to match Process 2's actual behavior; B2 then adds the Process 1 equivalent alongside it, correctly timed from day one since it's driven by the describe event directly rather than by `GameLoop`'s turn loop at all.

**C — Authoring-only workaround: attach entry consequences directly to the incoming movement command(s), zero engine change.**
Each `DirectionData.commandData` already carries its own `preConditions`/`actions` alongside its `MovePlayerActionData` (confirmed in the jetty/dunes location documents — e.g. the jetty→dunes direction's `commandData.actions` list). An author can add `NOT_CARRIED torch`, `MessageAction`, `DecrementVariableAction` directly onto every direction (and any other move-producing command, e.g. "jump sea") that leads into the hazardous location. Fires exactly once, exactly on the specific move, correctly timed by construction — no engine change at all. Downside: has to be duplicated on *every* entry path into that location; nothing declares "this Location has this rule" once, so a new entrance added later silently lacks the guard, and `PlayerAtCondition`-style Processes stay broken for anyone who keeps using them this way.

## Recommendation

**A + B2 together, both landing** — this is what PAW itself did (two tables, correctly timed each), and it's the only option with zero regression risk to any existing adventure's content.

- **A** (reorder `GameLoop`'s existing `runProcesses()` call to after dispatch) fixes the *existing* table's timing to actually match what it's already trying to be — PAW's Process 2, "after every time frame." Two-line change, no data model impact, `heavy_sand` and everything else in `commands` keeps working, just correctly timed relative to movement.
- **B2** (a new `arrivalProcesses` table, additive) gives authors the *other* PAW primitive — "fires when the location is described" — for content that's actually arrival-shaped: `welcome_message`, an NPC-presence announcement, the cave/trip-wire example. Confirmed cheap in this codebase specifically: `WorkflowData` is a two-field Lombok `@Data` class, `WorkflowMapper.populate` is two parallel loops, and the authoring UI already has a `CommandListEditorView` base built exactly for "add another list-of-commands editor" — a third table follows an established template on every layer, not a new design.

Together these need no re-authoring of any existing adventure and no audit of "does this Process want describe-timing or turn-timing" — that question is answered going forward by *which table an author puts a new entry in*, not by inspecting existing entries' shape. `welcome_message` moves to the new table (a one-line data edit to try it), `heavy_sand`/`stirring_up_sand` stay exactly where they are.

B1 (repoint the existing table wholesale) is dropped from consideration now that B2 is on the table — it bought nothing B2 doesn't also buy, at the cost of a breaking change and a migration audit across every adventure.

C is not recommended as the primary fix — it doesn't compose (duplication per entry point, easy to miss one) — but is worth documenting as a legitimate today-only workaround, and as the fallback authors already have for anything the two workflow tables don't cover even after A + B2 ship.

**Not recommended, either alternative:** doing nothing, or a narrower "only re-check `PlayerAtCondition` specifically" special case in `GameLoop` — the user's own cave/trip-wire example generalizes the primitive beyond location checks (any condition + any actions), so a location-specific patch would leave the same class of bug for the next non-location Process that happens to read state the current sub-command is about to change.

## Open questions to settle before implementation (both A and B)

1. **Does anything currently rely on a Process running *before* its sub-command for causal reasons** (e.g. a Process setting a variable a same-turn command's precondition then reads)? `Workflow.runProcesses()` never returns anything `GameLoop` checks (`Workflow.java:61-68`, `GameLoop.java:42`), so nothing today can use a Process to gate/block the following command — but a variable side-effect read same-turn is not ruled out by code inspection alone. Grep all `SetVariableActionData`/`IncrementVariableActionData` Process entries across existing Mongo adventures, not just "The Demo," before reordering.
2. **(B2) Naming and UI placement of the new table.** "Process 1"/"Process 2" are PAW's internal names, not necessarily what authors should see — needs a domain-facing name (e.g. "Arrival" vs "Workflow" processes) and a decision on where its editor lives relative to the existing `WorkflowMainLayout` nav (new tab alongside "Workflow"/"Responses", per `WorkflowEditorView.java:10` / `ResponsesEditorView`'s routes).
3. **The built-in `"~ ~ ~"` catch-all Process** (`CommandFactory.java:70-73`, prints `SM2` every turn, filtered out of `AdventureRunSession.submit()`'s output at `AdventureRunSession.java:33`) stays in `commands` and is unaffected by A (still runs every turn, just after dispatch instead of before) — confirm `SM2`'s content doesn't depend on pre-move state before landing A, but this one doesn't need to move to the new table.
4. **Multi-sub-command turns** ("go north and look"): under B2, does "go north" firing the arrival hook, immediately followed by "look" firing the *response* describe hook again, double-announce anything that isn't naturally idempotent (e.g. `timesVisited` increment happens once in `MovePlayerAction.execute()` regardless, but an arrival Process's own message would print twice)? Worth an explicit test either way, not just reasoning about it.
