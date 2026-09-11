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

**B — Re-point Processes to fire on the describe/arrival event, matching PAW's Process 1 exactly.**
Remove the `gameContext.runProcesses()` call from `GameLoop.processCommand` entirely; call it instead from wherever a location is actually (re)described:
- `MovePlayerAction.execute()` (`MovePlayerAction.java:27-39`), after `gameContext.setCurrentLocation(destination)` (line 29) — i.e. exactly where `destination.getArrivalDescription()` is already built.
- The built-in "describe"/"look" Response (`CommandFactory.setUpWorkflowCommands`, `server/src/main/java/com/pdg/adventure/CommandFactory.java:60-68`), which calls `gameContext.getCurrentLocation().getLongDescription()`.

This is the literal PAW Process 1 mapping: fires once per describe (arrival *and* explicit "look"), never on a non-describing command. Matches the trip-wire's "announced right as you walk in" framing precisely, and the guide's own NPC-presence example verbatim. But it changes what "Process" *means* for every existing entry, including `heavy_sand` — dropping an item at the dunes no longer triggers a describe, so `heavy_sand` would stop firing at all unless it's re-authored as a **Response** on "drop" instead (which is a strictly better fit for it anyway, and the exact pattern `stirring_up_sand` already uses correctly in the same adventure's `interceptorCommands`). No schema change (`Command`/`PreCondition`/`Action` types are untouched, `WorkflowData.commands` keeps its current shape) — but it is a behavior change for any other adventure's existing Processes, and requires auditing/re-authoring any that rely on "fires on a specific verb, anywhere" rather than "fires on describe."

**C — Authoring-only workaround: attach entry consequences directly to the incoming movement command(s), zero engine change.**
Each `DirectionData.commandData` already carries its own `preConditions`/`actions` alongside its `MovePlayerActionData` (confirmed in the jetty/dunes location documents — e.g. the jetty→dunes direction's `commandData.actions` list). An author can add `NOT_CARRIED torch`, `MessageAction`, `DecrementVariableAction` directly onto every direction (and any other move-producing command, e.g. "jump sea") that leads into the hazardous location. Fires exactly once, exactly on the specific move, correctly timed by construction — no engine change at all. Downside: has to be duplicated on *every* entry path into that location; nothing declares "this Location has this rule" once, so a new entrance added later silently lacks the guard, and `PlayerAtCondition`-style Processes stay broken for anyone who keeps using them this way.

## Recommendation

**B**, with **A as a fallback if the `heavy_sand`-style re-authoring is judged too disruptive to do now.**

B is the one that actually matches the proven, bug-free design this project is modeled on, and gets the *scope* right, not just the timing — a Process becomes "this fires when the player sees this location described," which is what every example so far (welcome message, NPC announcement, trip wire) actually wants, without asking authors to add their own "not already tripped"-style guards just to avoid re-firing on unrelated turns. The cost is bounded and already has a proven precedent in this exact adventure: `heavy_sand` moving from `commands` to `interceptorCommands` is the same migration `stirring_up_sand` already went through correctly.

A is cheaper and strictly safer (no re-authoring, no behavior change for `heavy_sand`), and it does fix the reported bug. Worth doing as an interim/fallback only — it leaves AdventureBuilder's Process table matching neither PAW table's cadence exactly (broader than Process 1, narrower in spirit than Process 2's "any uncontrolled event" framing), and reintroduces a milder version of the same class of surprise (a hazard re-firing on "inventory" or "wait" while standing in the room) that this whole investigation started from.

C is not recommended as the primary fix — it doesn't compose (duplication per entry point, easy to miss one) — but is worth documenting as a legitimate today-only workaround, and as the fallback authors already have for anything not yet covered even after B ships.

**Not recommended, either alternative:** doing nothing, or a narrower "only re-check `PlayerAtCondition` specifically" special case in `GameLoop` — the user's own cave/trip-wire example generalizes the primitive beyond location checks (any condition + any actions), so a location-specific patch would leave the same class of bug for the next non-location Process that happens to read state the current sub-command is about to change.

## Open questions to settle before implementation (both A and B)

1. **Does anything currently rely on a Process running *before* its sub-command for causal reasons** (e.g. a Process setting a variable a same-turn command's precondition then reads)? `Workflow.runProcesses()` never returns anything `GameLoop` checks (`Workflow.java:61-68`, `GameLoop.java:42`), so nothing today can use a Process to gate/block the following command — but a variable side-effect read same-turn is not ruled out by code inspection alone. Grep all `SetVariableActionData`/`IncrementVariableActionData` Process entries across existing Mongo adventures, not just "The Demo," before reordering.
2. **(B only) Full inventory of every adventure's existing Process entries**, to classify each as "wants describe-timing" (move to the new arrival hook, no data change needed since it's still `Workflow.processes`) vs. "wants verb-timing" (needs re-authoring into `interceptorCommands`, like `heavy_sand`). This is a one-time audit, not an automated migration — a Process's *intent* isn't mechanically derivable from its shape (both `welcome_message` and `heavy_sand` use an identical `PlayerAtCondition`+`MessageAction` shape today).
3. **(B only) Does the built-in `"~ ~ ~"` catch-all Process** (`CommandFactory.java:70-73`, prints `SM2` every turn, filtered out of `AdventureRunSession.submit()`'s output at `AdventureRunSession.java:33`) **need to keep running every turn regardless of describe events?** If so, it either stays specifically wired to the old per-command call site (a second, narrower call GameLoop keeps making just for this one built-in), or gets reclassified — needs a decision either way before removing `GameLoop`'s `runProcesses()` call outright.
4. **Multi-sub-command turns** ("go north and look"): under B, does "go north" firing its own describe/arrival hook, immediately followed by "look" firing the *response* describe hook again, double-announce anything that isn't naturally idempotent (e.g. `timesVisited` increment happens once in `MovePlayerAction.execute()` regardless, but a Process's own message would print twice)? Worth an explicit test either way, not just reasoning about it.
