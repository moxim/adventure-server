# Design: Remove the privileged `action` from `CommandData`

**Date:** 2026-06-01
**Branch base:** `actions-we-want-actions`
**Status:** Draft for review

## 1. Motivation

`CommandData` currently models a command as `{ commandDescription, preConditions,
followUpActions (list), action (single) }`. The single `action` is a *privileged*
primary action: at runtime `GenericCommand.execute()` runs it first (and NPEs if it
is null), then runs `followUpActions` only if the primary succeeded.

A command is conceptually just **preconditions (a gate) + an ordered list of actions**.
The privileged primary action is redundant: anything it does can be the first element
of the action list. Removing it unifies the model, the runtime, the mapper, and the
editor around a single action list.

## 2. Decisions (confirmed with user)

1. **Remove the primary action everywhere** — model *and* runtime (`Command` /
   `GenericCommand`), not just the model. (User: "Remove everywhere".)
2. **Complete the list editor** — finish the in-flight `PreconditionActionEditor`
   action section with a real action-list editor (mirroring `ConditionListEditor`) and
   remove the old single-"Primary Action" editor from `CommandEditorView`.
   (User: "Complete the list editor".)
3. **Rename `followUpActions` → `actions`** (model + runtime + mapper + views + tests).
   "followUp" only made sense relative to a primary action that no longer exists.
4. **Former primary action becomes the first element of `actions`**, preserving
   execution order and the user-visible result message (see §5).

## 3. Current state (as found)

- **Model** `com.pdg.adventure.model.CommandData`: fields `commandDescription`,
  `preConditions: List<PreConditionData>`, `followUpActions: List<? extends ActionData>`,
  `action: ActionData`; Lombok `@Data`; custom `setAction()` with a null check.
- **Runtime API** `com.pdg.adventure.api.Command`: `getAction()`,
  `addFollowUpAction(Action)`, `getFollowUpActions()`, `addPreCondition`, `execute()`.
- **Runtime impl** `com.pdg.adventure.server.parser.GenericCommand`: `final Action mainAction`
  (constructor-required), `followUpActions` list, `preConditions` list. `execute()`:
  preconditions gate → `mainAction.execute()` → on success, `followUpActions` in order.
  `toString()` references `mainAction`.
- **Mapper** `CommandMapper.mapToBO`: reads `getAction()`, builds
  `new GenericCommand(description, actionBO)`, then adds followups + preconditions.
  `mapToDO` is an incomplete TODO (sets preconditions/followups to null).
- **Editor** `CommandEditorView`: edits a single primary action via `actionEditor`;
  grid has "Primary Action" / "First Followup Action" columns. `PreconditionActionEditor`
  (recently added, wired in via the in-flight diff) has a working `ConditionListEditor`
  and a **stubbed** action grid (`addAction()` is an empty TODO).
- **Usage trackers** `ItemUsageTracker`, `LocationUsageTracker`, `MessageUsageTracker`:
  read `getAction()` (plus `getFollowUpActions()`) to scan for references.
- **Construction sites**: `new GenericCommand(description, action)` is called at ~30
  sites (14 in `CommandFactory`, 9 in `MiniAdventureContent`, `MiniAdventure`,
  `CommandHandler`, and ~8 test classes).

## 4. Target design

### 4.1 Model — `CommandData`
- Remove the `action` field and the custom `setAction()`.
- Rename `followUpActions` → `actions`, typed `List<ActionData>` (not
  `List<? extends ActionData>`, so it is appendable). Initialize to `new ArrayList<>()`.
- Add a convenience `addAction(ActionData)` that null-checks (preserving the old
  `setAction` guard) and appends — gives call sites a clean migration target.
- Keep Lombok `@Data` (generated `getActions`/`setActions`).

### 4.2 Runtime — `Command` (API) + `GenericCommand`
- `Command`: remove `getAction()`; rename `addFollowUpAction` → `addAction`,
  `getFollowUpActions` → `getActions`.
- `GenericCommand`: remove the `mainAction` field. Hold a single `List<Action> actions`.
  - **Retain a convenience constructor** `GenericCommand(CommandDescription, Action)`
    that seeds `actions` with the given action (so the ~30 existing construction sites
    compile unchanged). Add `GenericCommand(CommandDescription)` for the empty case.
    This is sugar for "first action", **not** a privileged action.
  - `execute()` becomes: check preconditions (stop on failure) → iterate `actions` in
    order (see §5 for result-message semantics).
  - `toString()` summarizes `actions` instead of `mainAction`.

### 4.3 Mapper — `CommandMapper`
- `mapToBO`: drop the `getAction()` branch; map every element of `getActions()` and add
  via `addAction`. (First element naturally lands first, preserving order.)
- `mapToDO`: it is an incomplete TODO today. Make it **compile** against the new API
  (map `getActions()` → `setActions(...)`, preconditions). Do **not** expand scope to
  fully implement/round-trip it beyond compiling + satisfying existing tests.

### 4.4 Editor — `CommandEditorView` + `PreconditionActionEditor` + new `ActionListEditor`
- New `ActionListEditor` (in `view.command.action`, mirroring
  `view.command.condition.ConditionListEditor`): renders the command's `actions` as a
  list with add / remove / reorder, each row backed by the existing `ActionEditorFactory`
  / `ActionSelector` leaf editors. Exposes `setActions(List)` / `getActions()`.
- `PreconditionActionEditor`: replace the stubbed `actionGrid` with `ActionListEditor`;
  wire `setCommand` (load `commandData.getActions()`) and `saveToCommand`
  (`commandData.setActions(...)`).
- `CommandEditorView`: remove the single-action editor machinery (`actionEditor`,
  `actionEditorContainer`, `originalActionData`, `showActionEditorForCommand`,
  `createActionEditor`, `showActionSelector`, related reset/validation). Drive
  preconditions+actions through `PreconditionActionEditor`. In the chain grid, rename the
  "Primary Action" column to "First Action" showing
  `getActions().isEmpty() ? "none" : getActions().getFirst().getActionName()`, and drop
  the now-redundant "First Followup Action" column.

### 4.5 Usage trackers
- `ItemUsageTracker`, `LocationUsageTracker`, `MessageUsageTracker`: replace the
  `getAction()` branch with iteration over `getActions()` (merge with the existing
  `getFollowUpActions()` loop). Behavior identical (the former primary is now the first
  list element).

### 4.6 Hand-built adventures + direction/item editors
- `CommandFactory`, `MiniAdventure`, `MiniAdventureContent`: construction via the retained
  `GenericCommand(desc, action)` overload — **no change required** beyond the
  `addFollowUpAction` → `addAction` rename (3 call sites).
- `DirectionEditorView`, `ItemEditorView`: replace `commandData.setAction(x)` with
  `commandData.addAction(x)` (or `setActions(...)`); replace `getAction()` reads with
  `getActions()`.

## 5. Execution-message semantics (must preserve)

Today, on full success the player sees the **primary action's** result message; followup
*success* messages are discarded (the outer result only adopts a followup message on
*failure*). The unified loop must preserve this so messages don't silently change:

```
result = checkPreconditions()           // stop on failure
if SUCCESS:
    first = true
    for action in actions:
        tmp = action.execute()
        if first:
            result.message = tmp.message         // first action's message (success or failure)
            first = false
            if tmp == FAILURE: break
        else if tmp == FAILURE:
            result.message = tmp.message; break    // failing action's message
        // else success: discard message (preserve old behavior)
```

Equivalent to the old behavior when `actions[0]` is the former primary action.
Empty `actions` no longer NPEs — it simply runs the preconditions and returns success.

## 6. Non-goals

- No rework of preconditions, `ConditionListEditor`, or the condition mappers.
- No full implementation of `mapToDO` beyond compiling + existing tests.
- No new action *types* or changes to existing `ActionData` subclasses / action mappers.
- No persistence/migration of already-stored adventures (in-memory/Mongo dev data; the
  field rename is a code-level change).

## 7. Affected files (grouped by fan-out slice)

- **Foundation (must land first, atomic):**
  `model/CommandData.java`, `api/Command.java`, `server/parser/GenericCommand.java`,
  `server/mapper/CommandMapper.java`, and `server/mapper/CommandMapperTest.java`.
- **Slice A — Command editor + action list UI:** new `view/command/action/ActionListEditor.java`,
  `view/command/PreconditionActionEditor.java`, `view/command/CommandEditorView.java`,
  `view/command/PreconditionActionEditorTest.java`.
- **Slice B — usage trackers:** `view/item/ItemUsageTracker.java`,
  `view/location/LocationUsageTracker.java`, `view/message/MessageUsageTracker.java`,
  and their tests (`LocationUsageTrackerTest`, `MessageUsageTrackerTest`).
- **Slice C — direction/item editor views:** `view/direction/DirectionEditorView.java`,
  `view/item/ItemEditorView.java`, `view/direction/DirectionEditorViewDataIntegrityTest.java`.
- **Slice D — runtime/test construction sites (rename only):** `addFollowUpAction` →
  `addAction` in `CommandFactory`, `MiniAdventureContent`; verify `GenericCommand`
  call sites across `*Test` compile (overload retained means most are untouched).

Exclude `view/login/LoginView.java:20` `setAction("login")` — that is Vaadin's
`LoginForm`, not `CommandData`.

## 8. Testing strategy

- Update `CommandMapperTest`, `DirectionEditorViewDataIntegrityTest`,
  `LocationUsageTrackerTest`, `MessageUsageTrackerTest`, `PreconditionActionEditorTest`
  to the list API; keep their assertions' intent.
- Add a `GenericCommand.execute()` test asserting the §5 message semantics
  (first-action message on success; failing-action message + short-circuit on failure;
  empty-actions = success, no NPE).
- Green gate: full `mvn test` in the worktree (baseline was ~647 tests passing).

## 9. Coordination plan (worktree + agent team)

Everything compiles against the foundation, so this does **not** parallelize as
independent worktrees from the start:

1. **Prep:** commit the current dirty working tree (the `PreconditionActionEditor`
   wiring + build/generated files) on a feature branch so a worktree inherits a
   consistent base.
2. **Foundation (lead, in the feature worktree):** implement §4.1–§4.3 + §5 and get
   `CommandMapperTest` green. The retained overload keeps construction sites compiling.
3. **Fan-out (agent team, disjoint files in the same worktree):** Slices A–D in
   parallel — they touch non-overlapping files. Lead integrates by compiling + running
   the full suite centrally (agents make edits; lead owns the build).
4. **Verify + finish:** full `mvn test` green, then report / open PR per user preference.

## 10. Risks

- **Editor scope (Slice A)** is the largest and least mechanical; `ActionListEditor` must
  mirror `ConditionListEditor` faithfully (leaf editors, reorder, validation, change
  tracking for the save/reset buttons).
- **Message semantics (§5)** likely uncovered by tests — the new `execute()` test guards it.
- **Generated Vaadin imports** in the dirty tree are build artifacts; keep them out of the
  refactor's conceptual diff.
