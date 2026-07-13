# Command Output Accumulation — Design

- **Date:** 2026-06-05
- **Status:** Approved (with one open decision, see below)
- **Branch:** actions-we-want-actions

## Problem

When a player command matches, the engine only ever surfaces **one** message, even
when several commands (or several actions within a command) genuinely apply. Two
places drop output:

1. **Within a command** — `GenericCommand.execute()` captures only the **first**
   action's message; later actions run for their side effects but their output is
   discarded. So a command whose actions are `MESSAGE` + `MOVE_PLAYER` + `DESCRIBE`
   shows the message but **swallows the `DESCRIBE` output**.
2. **Within a command chain** — `GenericCommandChain.execute()` loops the chain's
   commands but `break`s on the **first success**, so any later applicable command
   never runs.

The desired behaviour: **consider all execution results and present all output.**

### Motivating example (real authored data)

A location's commands, grouped by specification (verb · noun):

| Chain        | Command ID            | Preconditions                       | Actions                                              |
|--------------|-----------------------|-------------------------------------|------------------------------------------------------|
| `jump`       | `…56mxmdezs`          | —                                   | `MESSAGE jetty_jump`                                 |
| `jump · sea` | `…3vp7vcg`            | WORN neoprene suit; PLAYER_AT jetty | `MESSAGE jump_sea_ok`; `MOVE_PLAYER sea`; `DESCRIBE sea` |
| `jump · sea` | `…ffrqrwpbq`          | NOT_WORN neoprene suit              | `MESSAGE jetty_jump_sea_no_suit`                     |
| `jump · sea` | `…sng0jqvhp`          | —                                   | `MESSAGE jump_sea_also_here`                         |
| `describe · sea` | `…66qazafn`       | —                                   | `MESSAGE inviting_sea`                               |

`CommandChainMapper.mapToBO` builds **one** `GenericCommandChain` per
`CommandChainData`, so the three `jump · sea` rows are a single runtime chain of
three commands (verified). Therefore `jump sea` reaches the executor as **one**
chain — no disambiguation involved.

Expected: typing **`jump sea`** with **no suit on** should run `…ffrqrwpbq`
(NOT_WORN ✓) **and** `…sng0jqvhp` (no precondition) → **two messages**. With the
suit on at the jetty, `…3vp7vcg` (incl. its `DESCRIBE sea`) **and** `…sng0jqvhp`.

The author encodes mutual exclusivity with preconditions (WORN / NOT_WORN) and
uses a no-precondition command as "this also always happens." The engine must
honour that.

## Scope

Two central methods change. Every execution path (the live
`GameLoop` → `CommandExecutor.execute`, plus `Location.applyCommand` and
`CommandHandler.applyCommand`) inherits the fix because they all ultimately call
`CommandChain.execute()` / `Command.execute()`.

- **Level 1** — `GenericCommand.execute()`: run all actions, accumulate every
  non-blank message.
- **Level 2** — `GenericCommandChain.execute()`: run every command whose
  preconditions pass, accumulate all their messages.

### Out of scope

- **Level 3** — the inter-object *"Which xyz do you want to abc?"* false-duplicate
  (one physical object collected as multiple chains via the location's
  `super` + `itemContainer` + `findItems` aggregation). This is a **separate**
  mechanism and gets its own debugging cycle. Genuine disambiguation between
  *different* objects stays as-is.
- Structured multi-message results (a `List<String>` on `ExecutionResult`) — not
  needed; the game prints a single string per turn.
- The pre-existing duplication across the three `applyCommand` / `execute` paths.

## Decisions (from brainstorming)

- Genuinely different objects (e.g. two different "key"s for `take key`) **keep**
  the `Which X?` prompt. That is Level 3 and out of scope here.
- A command chain runs **every** command whose preconditions pass (replaces
  stop-at-first-success). Mutual exclusivity is the author's responsibility via
  preconditions.
- **Carrying output: Approach A** — accumulate into the existing single-string
  `resultMessage` (newline-joined). No `ExecutionResult` interface change, no
  `GameLoop` change.

## Detailed design

### Level 1 — `GenericCommand.execute()`

```java
@Override
public ExecutionResult execute() {
    ExecutionResult result = checkPreconditions();
    if (result.getExecutionState() != ExecutionResult.State.SUCCESS) {
        return result;                 // preconditions unmet → command doesn't apply
    }
    List<String> messages = new ArrayList<>();
    for (Action action : actions) {
        ExecutionResult fromAction = action.execute();
        if (fromAction.getExecutionState() == ExecutionResult.State.FAILURE) {
            result.setExecutionState(ExecutionResult.State.FAILURE);
            result.setResultMessage(fromAction.getResultMessage());
            return result;             // a failing action fails the whole command (SEE OPEN DECISION)
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

### Level 2 — `GenericCommandChain.execute()`

```java
@Override
public ExecutionResult execute() {
    List<String> messages = new ArrayList<>();
    boolean anyApplied = false;
    ExecutionResult last = new CommandExecutionResult();   // FAILURE / empty
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

### Behaviour on the motivating example

- `jump sea`, no suit → `…3vp7vcg` precondition fails (skipped); `…ffrqrwpbq`
  succeeds (`jetty_jump_sea_no_suit`); `…sng0jqvhp` succeeds
  (`jump_sea_also_here`) → `jetty_jump_sea_no_suit` + newline + `jump_sea_also_here`.
- `jump sea`, suit on at jetty → `…3vp7vcg` succeeds and now returns
  `jump_sea_ok` + the sea description (Level 1 fix); `…ffrqrwpbq` skipped;
  `…sng0jqvhp` succeeds → all joined.

## Edge-case decisions

- **Separator:** `System.lineSeparator()` (matches `Location.getLongDescription`).
- **Blank messages skipped** so side-effect-only actions/commands (e.g.
  `MOVE_PLAYER`) don't add empty lines.
- **Order:** chain order (top to bottom, as in the grid).
- **Nothing applies in a chain:** result is FAILURE carrying the last command's
  message; if empty, the engine's existing `clarifyExecutionOutcome` turns it into
  `"You can't do that."`.

## Open decision — failing action within a command

The proposed Level 1 behaviour: a failing action **stops** the command and
**discards** that command's already-accumulated output, marking the command
FAILURE. This was flagged as uncertain and should be settled by tests, not
assumed final. Alternatives to evaluate during implementation:

1. **Stop + discard (proposed default).** Simple; matches the old
   "break on failure" intent. Risk: hides useful output emitted before the
   failure.
2. **Stop + keep prior messages.** Preserve messages from actions that already
   succeeded, then surface the failure too. More forgiving; ambiguous SUCCESS vs
   FAILURE state.
3. **Continue past the failure.** Run remaining actions anyway. Risky — later
   actions may depend on the failed one.

Start with (1); revisit if automated or manual tests reveal lost output or odd
behaviour. The image data never fails an action, so this does not block the
motivating example.

## Testing

- **`GenericCommandChain`** unit tests: multiple applicable commands → joined
  messages; mutually-exclusive WORN/NOT_WORN → the right one plus the always-on
  command; no command applies → FAILURE.
- **`GenericCommand`** unit tests: multiple actions → all messages joined;
  failing action stops the command (per the open decision); failing precondition
  skips actions.
- **Scenario test** mirroring the image: build the three `jump · sea` commands
  and assert two messages with no suit on.
- **Migration:** review and update existing tests that assumed first-success-only
  output — `CommandExecuterTest`, `GenericCommandExecuteTest`,
  `GenericCommandProviderTest`, `LocationTest`. This is the bulk of the work.
- Full `mvn test` green before completion.
