# Remove privileged `CommandData.action` — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **Executor note:** Tasks are executed by capable subagents WITH full codebase access. Net-new files and the foundation API are given as complete code. Edits to large existing files (e.g. `CommandEditorView`, the usage trackers, test files) are given as precise transformation specs (exact old→new, with line anchors) rather than re-inlining the whole file — apply them by reading the current file.

**Goal:** Remove the privileged single `action` from `CommandData` and the runtime `Command`/`GenericCommand`, unifying every command into preconditions + one ordered `actions` list, and finish the list-based action editor.

**Architecture:** A command becomes `{ description, preConditions, actions }`. `execute()` checks preconditions then runs `actions` in order (stop on first failure), preserving the old user-visible result message (first action's message on success). The `GenericCommand(description, firstAction)` convenience constructor is retained as sugar (seeds the list) so the ~30 existing construction sites compile unchanged. The editor gains an `ActionListEditor` mirroring the existing `ConditionListEditor`.

**Tech Stack:** Java 21, Spring Boot, Vaadin Flow, Lombok, JUnit 5 + Mockito + AssertJ, Maven.

**Git root:** `server/` (NOT the repo-parent dir). Run all `git`/`mvn` from `server/`.

---

## Execution model & coordination (read first)

The model + runtime API changes break ~15 files at compile time, so there is **no intermediate green state** between the foundation and the fan-out. The plan therefore is:

1. **Phase 1 — Foundation (Tasks 1–3, sequential, one feature worktree).** Change `CommandData`, `Command`, `GenericCommand`, `CommandMapper`, and rewrite `CommandMapperTest` + add a `GenericCommand.execute()` test. The project will **not** compile until the fan-out completes (expected).
2. **Phase 2 — Fan-out (Tasks 4–7, parallel subagents, disjoint files, same worktree).** Each slice edits a non-overlapping file set to conform to the new API. Agents make edits; the lead owns the build. **Before dispatching, the lead runs the authoritative break-site grep** (Task 7 Step 2) to confirm the exact file/line list and catch any hidden BO `getFollowUpActions()` caller in `GenericCommandProvider`/`CommandChain`.
3. **Phase 3 — Integrate & verify (Task 8, lead).** `mvn test` green; fix stragglers; final commit.

**Worktree setup (lead, before Task 1):**
```bash
cd server
git switch -c refactor/command-data-remove-action
git worktree add .git/wt/cmd-refactor refactor/command-data-remove-action  # or use the using-git-worktrees skill
```
(Working in the current branch's tree is also acceptable since the baseline is already committed; the worktree just isolates the build.)

**Disjoint file ownership (no two slices touch the same file):**
- Slice A: `view/command/action/ActionListEditor.java` (new), `view/command/action/ActionRow.java` (new), `view/command/PreconditionActionEditor.java`, `view/command/CommandEditorView.java`, `view/command/condition/ConditionListEditor.java` (onChange hook), `test/.../view/command/PreconditionActionEditorTest.java`.
- Slice B: `view/item/ItemUsageTracker.java`, `view/location/LocationUsageTracker.java`, `view/message/MessageUsageTracker.java`, `test/.../view/location/LocationUsageTrackerTest.java`, `test/.../view/message/MessageUsageTrackerTest.java`.
- Slice C: `view/direction/DirectionEditorView.java`, `view/item/ItemEditorView.java`, `test/.../view/direction/DirectionEditorViewDataIntegrityTest.java`.
- Slice D: `CommandFactory.java`, `MiniAdventureContent.java` (and a verifying grep across all `*Test` construction sites).

---

## Phase 1 — Foundation

### Task 1: Model — `CommandData`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/model/CommandData.java`

- [ ] **Step 1: Replace the class body**

Replace the entire file with:

```java
package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.condition.PreConditionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandData extends BasicData {
    private CommandDescriptionData commandDescription;
    private List<PreConditionData> preConditions;
    private List<ActionData> actions;

    public CommandData() {
        this(new CommandDescriptionData());
    }

    public CommandData(CommandDescriptionData aCommandDescriptionData) {
        commandDescription = aCommandDescriptionData;
        preConditions = new ArrayList<>();
        actions = new ArrayList<>();
    }

    /** Append an action. Null-checks, preserving the old setAction guard intent. */
    public void addAction(ActionData anAction) {
        if (anAction == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        actions.add(anAction);
    }
}
```

Notes: `action` field + custom `setAction` removed. `followUpActions` → `actions`, retyped `List<? extends ActionData>` → `List<ActionData>` (must be appendable). Lombok `@Data` generates `getActions()`/`setActions(List<ActionData>)`.

- [ ] **Step 2: Do not compile yet** — dependents are updated in later tasks. Proceed to Task 2.

---

### Task 2: Runtime — `Command` API + `GenericCommand` + `execute()` test

**Files:**
- Modify: `src/main/java/com/pdg/adventure/api/Command.java`
- Modify: `src/main/java/com/pdg/adventure/server/parser/GenericCommand.java`
- Test: `src/test/java/com/pdg/adventure/server/parser/GenericCommandExecuteTest.java` (new)

- [ ] **Step 1: Update the `Command` interface**

Replace `src/main/java/com/pdg/adventure/api/Command.java` with:

```java
package com.pdg.adventure.api;

import java.util.List;

public interface Command extends Ided {

    CommandDescription getDescription();

    ExecutionResult execute();

    void addPreCondition(PreCondition aCondition);

    void addAction(Action anAction);

    List<PreCondition> getPreconditions();

    List<Action> getActions();
}
```

(Removed `getAction()`; `addFollowUpAction`→`addAction`; `getFollowUpActions`→`getActions`.)

- [ ] **Step 2: Update `GenericCommand`**

Replace `src/main/java/com/pdg/adventure/server/parser/GenericCommand.java` with:

```java
package com.pdg.adventure.server.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pdg.adventure.api.*;

public class GenericCommand implements Command {
    private final CommandDescription commandDescription;
    private final List<PreCondition> preConditions;
    private final List<Action> actions;
    private String id;

    public GenericCommand(CommandDescription aCommandDescription) {
        commandDescription = aCommandDescription;
        preConditions = new ArrayList<>();
        actions = new ArrayList<>();
        id = UUID.randomUUID().toString();
    }

    /** Convenience: seed the command with its first action (sugar, not a privileged action). */
    public GenericCommand(CommandDescription aCommandDescription, Action anAction) {
        this(aCommandDescription);
        if (anAction != null) {
            actions.add(anAction);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
    }

    @Override
    public List<PreCondition> getPreconditions() {
        return preConditions;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public CommandDescription getDescription() {
        return commandDescription;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = checkPreconditions();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            boolean first = true;
            for (Action action : actions) {
                ExecutionResult fromAction = action.execute();
                if (first) {
                    setExecutionResult(result, fromAction);
                    first = false;
                    if (fromAction.getExecutionState() == ExecutionResult.State.FAILURE) {
                        break;
                    }
                } else if (fromAction.getExecutionState() == ExecutionResult.State.FAILURE) {
                    setExecutionResult(result, fromAction);
                    break;
                }
            }
        }
        return result;
    }

    private ExecutionResult checkPreconditions() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        for (PreCondition condition : preConditions) {
            ExecutionResult tmp = condition.check();
            if (tmp.getExecutionState() == ExecutionResult.State.FAILURE) {
                setExecutionResult(result, tmp);
                break;
            }
        }
        return result;
    }

    private void setExecutionResult(ExecutionResult aTarget, ExecutionResult aResult) {
        aTarget.setExecutionState(aResult.getExecutionState());
        aTarget.setResultMessage(aResult.getResultMessage());
    }

    @Override
    public void addPreCondition(PreCondition aCondition) {
        preConditions.add(aCondition);
    }

    @Override
    public void addAction(Action anAction) {
        actions.add(anAction);
    }

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof GenericCommand aCommand)) return false;
        return id.equals(aCommand.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return commandDescription.getDescription() + (actions.isEmpty() ? "" : actions.toString());
    }
}
```

**Semantics preserved (see spec §5):** first action's message surfaces on success; later success messages discarded; any failure short-circuits with that action's message; empty `actions` returns precondition success (no NPE). This equals the old behavior when `actions[0]` is the former primary action.

- [ ] **Step 3: Write the failing `execute()` test**

Create `src/test/java/com/pdg/adventure/server/parser/GenericCommandExecuteTest.java`:

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

class GenericCommandExecuteTest {

    private static Action action(ExecutionResult.State state, String message) {
        Action a = mock(Action.class);
        ExecutionResult r = new CommandExecutionResult(state);
        r.setResultMessage(message);
        when(a.execute()).thenReturn(r);
        return a;
    }

    private static PreCondition precondition(ExecutionResult.State state, String message) {
        PreCondition p = mock(PreCondition.class);
        ExecutionResult r = new CommandExecutionResult(state);
        r.setResultMessage(message);
        when(p.check()).thenReturn(r);
        return p;
    }

    @Test
    void emptyActions_doesNotThrow_andSucceeds() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void firstActionSuccessMessageSurfaces_laterSuccessesDiscarded() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "first"));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "second"));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("first");
    }

    @Test
    void firstActionFailure_shortCircuits() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        Action second = mock(Action.class);
        cmd.addAction(action(ExecutionResult.State.FAILURE, "boom"));
        cmd.addAction(second);
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("boom");
    }

    @Test
    void laterActionFailure_surfacesThatFailure() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "ok"));
        cmd.addAction(action(ExecutionResult.State.FAILURE, "later-fail"));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("later-fail");
    }

    @Test
    void preconditionFailure_skipsActions() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addPreCondition(precondition(ExecutionResult.State.FAILURE, "blocked"));
        Action action = mock(Action.class);
        cmd.addAction(action);
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("blocked");
    }
}
```

Verify the constructor of `CommandExecutionResult(ExecutionResult.State)` and `setResultMessage`/`getResultMessage`/`getExecutionState` signatures against `api`/`server.parser` (they are used by the production `GenericCommand`). Adjust mock wiring if a real `ExecutionResult` impl is required.

- [ ] **Step 4: Compile only this slice's classes** if the toolchain allows; otherwise defer compilation to Phase 3. Do NOT run the full suite yet.

---

### Task 3: Mapper — `CommandMapper` + rewrite `CommandMapperTest`

**Files:**
- Modify: `src/main/java/com/pdg/adventure/server/mapper/CommandMapper.java`
- Modify: `src/test/java/com/pdg/adventure/server/mapper/CommandMapperTest.java`

- [ ] **Step 1: Rewrite `CommandMapper` body** (keep package, imports, annotations, constructor):

```java
    public Command mapToBO(CommandData aCommandData) {
        CommandDescription description = commandDescriptionMapper.mapToBO(aCommandData.getCommandDescription());
        Command result = new GenericCommand(description);
        result.setId(aCommandData.getId());
        for (ActionData actionData : aCommandData.getActions()) {
            Mapper<ActionData, Action> actionMapper = mapperSupporter.getMapper(actionData.getClass());
            result.addAction(actionMapper.mapToBO(actionData));
        }
        for (PreConditionData condition : aCommandData.getPreConditions()) {
            Mapper<PreConditionData, PreCondition> conditionMapper = mapperSupporter.getMapper(condition.getClass());
            result.addPreCondition(conditionMapper.mapToBO(condition));
        }
        return result;
    }

    // TODO: round-trip preconditions/actions fully. Currently maps actions only (compile-safe analog of the old getAction mapping).
    public CommandData mapToDO(Command aCommand) {
        CommandData result = new CommandData();
        result.setId(aCommand.getId());
        result.setCommandDescription(commandDescriptionMapper.mapToDO(aCommand.getDescription()));
        result.setPreConditions(null);
        List<ActionData> actions = new ArrayList<>();
        for (Action action : aCommand.getActions()) {
            Mapper<ActionData, Action> actionMapper = mapperSupporter.getMapper(action.getClass());
            actions.add(actionMapper.mapToDO(action));
        }
        result.setActions(actions);
        return result;
    }
```

Add `import java.util.ArrayList;` (and keep `java.util.List`). Remove the old `final ActionData mainActionData = aCommandData.getAction();` branch and the old `mapToDO` `getAction()`/`setAction()`/`setFollowUpActions()` lines.

- [ ] **Step 2: Rewrite `CommandMapperTest`** to the new API. Apply these transformations (the file's structure stays; 8 tests):

  - `commandData.setAction(mainActionData);` → `commandData.addAction(mainActionData);` (Tests 1, 3, 5, 6).
  - `commandData.setFollowUpActions(followUpActions);` → for each element call `commandData.addAction(elem);` **after** the main action, OR build one list and `commandData.setActions(list)` where the list is `[mainActionData, ...followUps]`. Keep the mocked `mapToBO` return order matching list order (e.g. Test 3 expects the two follow-ups; with the main action now first, stub returns `mainAction, followUpAction1, followUpAction2` and assert `result.getActions()` `containsExactly(mainAction, followUpAction1, followUpAction2)`).
  - Test 1 assertions: `result.getAction()` is gone → assert `result.getActions()).containsExactly(mainAction)` and `result.getPreconditions()).isEmpty()`.
  - Test 2 (null main action): there is no null action anymore. Re-purpose as "no actions" → build `CommandData` with empty `actions`, assert `result.getActions()).isEmpty()` and `verify(mapperSupporter, never()).getMapper(any())`.
  - Test 3: assert `result.getActions()).containsExactly(...)` (size 3 incl. main, or keep 2 if you set only follow-ups — pick one and make the stub match). Recommended: actions = `[main, fu1, fu2]`, assert size 3.
  - Test 6 (complex): assert `result.getActions()` has the expected size (main + 1 follow-up = 2) and `result.getPreconditions()).hasSize(1)`.
  - Tests 7 & 8 (`mapToDO`): `new GenericCommand(commandDescription, mainAction)` still compiles (constructor retained). Replace `result.getAction()).isEqualTo(mainActionData)` with `result.getActions()).containsExactly(mainActionData)`. Remove the `result.getFollowUpActions()).isNull()` assertion (no such accessor); `result.getPreConditions()).isNull()` stays (mapToDO sets it null). Stub `actionMapper.mapToDO(mainAction)` → `mainActionData` (already present).
  - Update the `@Mock Mapper<? extends ActionData, ?> actionMapper` usage unchanged; `mainActionData`/`mainAction` mocks stay.

- [ ] **Step 3: Defer compilation/run to Phase 3** (the suite won't build until fan-out lands). Commit Phase 1:

```bash
git add src/main/java/com/pdg/adventure/model/CommandData.java \
        src/main/java/com/pdg/adventure/api/Command.java \
        src/main/java/com/pdg/adventure/server/parser/GenericCommand.java \
        src/main/java/com/pdg/adventure/server/mapper/CommandMapper.java \
        src/test/java/com/pdg/adventure/server/parser/GenericCommandExecuteTest.java \
        src/test/java/com/pdg/adventure/server/mapper/CommandMapperTest.java
git commit -m "refactor: remove privileged action from CommandData and Command runtime"
```

---

## Phase 2 — Fan-out (parallel, disjoint files)

### Task 4 (Slice A): Action list editor + command editor

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/command/action/ActionRow.java`
- Create: `src/main/java/com/pdg/adventure/view/command/action/ActionListEditor.java`
- Modify: `src/main/java/com/pdg/adventure/view/command/PreconditionActionEditor.java`
- Modify: `src/main/java/com/pdg/adventure/view/command/CommandEditorView.java`
- Modify: `src/test/java/com/pdg/adventure/view/command/PreconditionActionEditorTest.java`

- [ ] **Step 1: Create `ActionRow`** (mirrors `ConditionRow`; no negate; adds reorder):

```java
package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import com.pdg.adventure.model.action.ActionData;

public class ActionRow extends Details {
    private final ActionEditorComponent editor;
    @Setter
    private Runnable onRemove;
    @Setter
    private Runnable onMoveUp;
    @Setter
    private Runnable onMoveDown;

    public ActionRow(ActionEditorComponent anEditor) {
        editor = anEditor;
        setSummaryText(anEditor.getActionTypeName().replace("ActionData", ""));

        Button upButton = new Button("Up");
        upButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        upButton.addClickListener(_ -> { if (onMoveUp != null) onMoveUp.run(); });

        Button downButton = new Button("Down");
        downButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        downButton.addClickListener(_ -> { if (onMoveDown != null) onMoveDown.run(); });

        Button removeButton = new Button("Remove");
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(_ -> { if (onRemove != null) onRemove.run(); });

        HorizontalLayout controls = new HorizontalLayout(upButton, downButton, removeButton);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);

        add(controls, anEditor);
        setWidthFull();
    }

    public ActionData toActionData() {
        return editor.getActionData();
    }

    public boolean validate() {
        return editor.validate();
    }
}
```

- [ ] **Step 2: Create `ActionListEditor`** (mirrors `ConditionListEditor`; composes the existing `ActionSelector`/`ActionEditorFactory`):

```java
package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;

public class ActionListEditor extends VerticalLayout {
    private final AdventureData adventureData;
    private final VerticalLayout rowsLayout;
    @Setter
    private Runnable onChange;   // fired on any user-driven edit (add/remove/reorder/leaf-field)

    public ActionListEditor(AdventureData anAdventureData) {
        adventureData = anAdventureData;

        rowsLayout = new VerticalLayout();
        rowsLayout.setPadding(false);
        rowsLayout.setSpacing(true);

        ActionSelector selector = new ActionSelector(adventureData);
        selector.setEditorSelectedListener(editor -> { addRow(editor); notifyChange(); });

        setPadding(false);
        add(rowsLayout, selector);
    }

    public void setActions(List<ActionData> actions) {
        rowsLayout.removeAll();
        if (actions == null) return;
        for (ActionData data : actions) {
            addRow(ActionEditorFactory.createEditor(data, adventureData));  // programmatic load: no notifyChange
        }
    }

    public List<ActionData> getActions() {
        return rowsLayout.getChildren()
                .filter(ActionRow.class::isInstance)
                .map(ActionRow.class::cast)
                .map(ActionRow::toActionData)
                .collect(Collectors.toList());
    }

    public boolean validate() {
        return rowsLayout.getChildren()
                .filter(ActionRow.class::isInstance)
                .map(ActionRow.class::cast)
                .allMatch(ActionRow::validate);
    }

    private void addRow(ActionEditorComponent editor) {
        ActionRow row = new ActionRow(editor);
        row.setOnRemove(() -> { rowsLayout.remove(row); notifyChange(); });
        row.setOnMoveUp(() -> moveRow(row, -1));
        row.setOnMoveDown(() -> moveRow(row, 1));
        // Fire change on client-side leaf-field edits (ports the old CommandEditorView.attachActionEditorListeners).
        editor.getChildren().forEach(child -> {
            if (child instanceof HasValue<?, ?> hasValue) {
                hasValue.addValueChangeListener(e -> { if (e.isFromClient()) notifyChange(); });
            }
        });
        rowsLayout.add(row);
    }

    private void moveRow(ActionRow row, int delta) {
        List<Component> children = rowsLayout.getChildren().collect(Collectors.toList());
        int newIndex = children.indexOf(row) + delta;
        if (newIndex < 0 || newIndex >= children.size()) return;
        rowsLayout.remove(row);
        rowsLayout.addComponentAtIndex(newIndex, row);
        notifyChange();
    }

    private void notifyChange() {
        if (onChange != null) onChange.run();
    }
}
```

- [ ] **Step 3: Replace `PreconditionActionEditor`** (drop the stubbed grid; wire the action list):

```java
package com.pdg.adventure.view.command;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.view.command.action.ActionListEditor;
import com.pdg.adventure.view.command.condition.ConditionListEditor;

public class PreconditionActionEditor extends VerticalLayout {
    private final ConditionListEditor conditionListEditor;
    private final ActionListEditor actionListEditor;

    public PreconditionActionEditor(AdventureData adventureData) {
        conditionListEditor = new ConditionListEditor(adventureData);
        actionListEditor = new ActionListEditor(adventureData);

        Details preconditionsSection = new Details("Preconditions", conditionListEditor);
        Details actionsSection = new Details("Actions", actionListEditor);

        setPadding(false);
        add(preconditionsSection, actionsSection);
    }

    public void setCommand(CommandData commandData) {
        conditionListEditor.setConditions(commandData.getPreConditions());
        actionListEditor.setActions(commandData.getActions());
    }

    public void saveToCommand(CommandData commandData) {
        commandData.setPreConditions(conditionListEditor.getConditions());
        commandData.setActions(actionListEditor.getActions());
    }

    public boolean validate() {
        return actionListEditor.validate();
    }

    /** Wire a single change callback to both editors so the host view can track dirtiness. */
    public void setOnChange(Runnable onChange) {
        conditionListEditor.setOnChange(onChange);
        actionListEditor.setOnChange(onChange);
    }
}
```

- [ ] **Step 3b: Augment `ConditionListEditor` with the same `onChange` hook** (so editing preconditions also enables Save — parity with actions). Add `@Setter private Runnable onChange;` (import `lombok.Setter`), and:
  - selector: `selector.setConditionSelectedListener(data -> { addRow(data, false); notifyChange(); });`
  - remove: `row.setOnRemove(() -> { rowsLayout.remove(row); notifyChange(); });`
  - leaf edits in `addRow`: after `rowsLayout.add(row);`, walk `editor.getChildren()` and add `HasValue` value-change listeners that fire `notifyChange()` when `e.isFromClient()` (mirror `ActionListEditor`; import `com.vaadin.flow.component.HasValue`).
  - add `private void notifyChange() { if (onChange != null) onChange.run(); }`.
  - Known minor gap (acceptable, not in scope): toggling the `Negate` checkbox inside a `ConditionRow` is not caught (it lives in the row, not the editor); any other edit still enables Save.

- [ ] **Step 4: Refactor `CommandEditorView`** (transformation spec — read the file and apply):
  - **Remove** these fields and all their uses: `actionEditorContainer`, `actionEditor`, `originalActionData`, `actionEditorHasChanges`. Keep `commandData`, `currentCommandChain`, `selectedCommandIndex`, `commandChainGrid`.
  - **Remove** these methods entirely: `setupActionEditor`, `showActionEditorForCommand`, `resetActionEditor`, `createActionEditor`, `showActionSelector`, `attachActionEditorListeners`.
  - **Add** a field `private final PreconditionActionEditor preconditionActionEditor;` constructed as `new PreconditionActionEditor(adventureData)`. Replace the line that currently does `add(commandLayout, details, resetBackSaveView, new PreconditionActionEditor(adventureData));` with `add(commandLayout, details, resetBackSaveView, preconditionActionEditor);` and remove the now-empty "Selected Command Action" container plumbing (the `actionEditorContainer` block in the constructor and in `vl1`).
  - **Grid (`createCommandChainGrid`)**: replace the "Primary Action" column body
    ```java
    if (cmd.getAction() != null) { return cmd.getAction().getActionName(); }
    return "none";
    ```
    with
    ```java
    return cmd.getActions().isEmpty() ? "none" : cmd.getActions().getFirst().getActionName();
    ```
    and rename its header to `"First Action"`. **Delete** the separate "First Followup Action" column (lines building it via `getFollowUpActions().getFirst()`).
  - **Selection listener** (was `showActionEditorForCommand(commandData.getAction())`): replace with `preconditionActionEditor.setCommand(commandData);`. Same for the `deleteCommandFromChain` tail and `setupActionEditor` callers — anywhere that showed an action editor for a command now calls `preconditionActionEditor.setCommand(selectedCommand)`.
  - **Save path** (`swivelTheSaveButton`): replace the `if (actionEditor != null) { ActionData action = actionEditor.getActionData(); command.setAction(action); }` block with `preconditionActionEditor.saveToCommand(command);`.
  - **Dirty-tracking & save-button (preserve old behavior — do NOT simplify away):** the old view enabled Save via `(binderHasChanges || actionEditorHasChanges)` and warned-on-leave the same way; that drove whether an author could persist action edits. Re-create it for the list editor:
    - Keep a field `private boolean editorHasChanges = false;` (replaces `actionEditorHasChanges`).
    - In the constructor, after building `preconditionActionEditor`, wire: `preconditionActionEditor.setOnChange(() -> { editorHasChanges = true; updateSaveButtonState(); resetButton.setEnabled(true); });`
    - `updateSaveButtonState`: `saveButton.setEnabled((binder.hasChanges() || editorHasChanges) && binder.isValid() && preconditionActionEditor.validate());`
    - `beforeLeave`: `...checkIfUserWantsToLeavePage(event, binder.hasChanges() || editorHasChanges);`
    - On successful save and on reset: `editorHasChanges = false;` and on reset reload via `preconditionActionEditor.setCommand(commandData);`.
    - Delete the old `attachActionEditorListeners`/`actionEditorHasChanges` machinery (its role now lives in `ActionListEditor.onChange`).
    - **Alternative the user may pick instead (simpler, deliberate behavior change):** drop dirty-tracking and set `saveButton.setEnabled(binder.isValid() && preconditionActionEditor.validate());` — Save then enables whenever the form is valid even with no edits, and leave-warnings rely on `binder.hasChanges()` only. Use ONLY if the user chooses it; otherwise implement the faithful version above.
  - Remove now-unused imports (`ActionEditorComponent`, `ActionEditorFactory`, `ActionSelector`, `ActionData`, `Div`) flagged by the compiler.

- [ ] **Step 5: Update `PreconditionActionEditorTest`** (transformation spec): keep precondition coverage; update/extend for actions. Where it asserted only preconditions in `saveToCommand`, add a parallel action assertion:
  ```java
  // build a CommandData, call editor.setCommand(cmd) then editor.saveToCommand(cmd)
  // assert cmd.getActions() reflects what was loaded (round-trip of an empty list stays empty)
  ```
  If the existing test referenced the old `actionGrid`/`addAction()` stub, delete those assertions. Ensure it compiles against the new `PreconditionActionEditor` API (`setCommand`, `saveToCommand`, `validate`).

- [ ] **Step 6:** Hand back to lead for the central build (Phase 3). Do not run the full suite from the subagent.

---

### Task 5 (Slice B): Usage trackers

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/item/ItemUsageTracker.java`
- Modify: `src/main/java/com/pdg/adventure/view/location/LocationUsageTracker.java`
- Modify: `src/main/java/com/pdg/adventure/view/message/MessageUsageTracker.java`
- Modify: `src/test/java/com/pdg/adventure/view/location/LocationUsageTrackerTest.java`
- Modify: `src/test/java/com/pdg/adventure/view/message/MessageUsageTrackerTest.java`

- [ ] **Step 1:** In each of the three trackers, replace the dual "primary action + follow-up actions" block with a single loop over `getActions()`. The three are identical except the check-helper name (`checkItemAction` / `checkMoveAction` / `checkAction`) and arg list. Pattern:

  Replace:
  ```java
  // Check primary action
  if (command.getAction() != null) {
      checkX(command.getAction(), ..., "Primary Action", ...);
  }
  // Check follow-up actions
  if (command.getFollowUpActions() != null) {
      int followUpIndex = 1;
      for (ActionData followUpAction : command.getFollowUpActions()) {
          checkX(followUpAction, ..., "Follow-up Action #" + followUpIndex, ...);
          followUpIndex++;
      }
  }
  ```
  With:
  ```java
  int actionIndex = 1;
  for (ActionData action : command.getActions()) {
      checkX(action, ..., "Action #" + actionIndex, ...);
      actionIndex++;
  }
  ```
  (Keep each tracker's specific `checkX` name and surrounding args verbatim.)

- [ ] **Step 2:** Update `LocationUsageTrackerTest` and `MessageUsageTrackerTest`:
  - `command.setAction(x)` → `command.addAction(x)`.
  - `command.setFollowUpActions(list)` → add each element via `command.addAction(elem)` (after the primary), or `command.setActions([...])`.
  - Any assertion on the context label `"Primary Action"` / `"Follow-up Action #1"` → `"Action #1"` / `"Action #2"` (indices shift: former primary is now `Action #1`, former follow-up #1 is `Action #2`).

- [ ] **Step 3:** Hand back to lead for the central build.

---

### Task 6 (Slice C): Direction & item editor views

**Files:**
- Modify: `src/main/java/com/pdg/adventure/view/direction/DirectionEditorView.java`
- Modify: `src/main/java/com/pdg/adventure/view/item/ItemEditorView.java`
- Modify: `src/test/java/com/pdg/adventure/view/direction/DirectionEditorViewDataIntegrityTest.java`

- [ ] **Step 1: `DirectionEditorView`** (~line 247): the command's action is being *set* (replace). Replace
  ```java
  directionData.getCommandData().setAction(movePlayerActionData);
  ```
  with
  ```java
  directionData.getCommandData().setActions(new java.util.ArrayList<>(java.util.List.of(movePlayerActionData)));
  ```
  (or add the imports and use the short names). **Replace semantics matter** — use `setActions(...)`, not `addAction`.

- [ ] **Step 2: `ItemEditorView`**:
  - `createTakeCommandData` (~318): `takeCommandData.setAction(takeActionData);` → `takeCommandData.setActions(new ArrayList<>(List.of(takeActionData)));`
  - `createDropCommandData` (~326): `dropCommandData.setAction(dropActionData);` → `dropCommandData.setActions(new ArrayList<>(List.of(dropActionData)));`
  - `createPickupCommands` (~296): `takeCommandFailed_allreadyCarried.setAction(messageData_alreadyCarried);` → `setActions(new ArrayList<>(List.of(messageData_alreadyCarried)));` (**replaces** the Take action set by `createTakeCommandData` — replace semantics required).
  - `createPickupCommands` (~307): `dropCommandFailed_notCarried.setAction(messageData_notCarried);` → `setActions(new ArrayList<>(List.of(messageData_notCarried)));`
  - Removal guard (~253): `if (command.getAction() == null) { return false; }` → `if (command.getActions().isEmpty()) { return false; }`
  - Add imports `java.util.ArrayList`, `java.util.List` if absent.

- [ ] **Step 3: `DirectionEditorViewDataIntegrityTest`** (~135–148):
  - `commandData.setAction(action);` → `commandData.addAction(action);`
  - `assertThat(commandData.getAction()).isNotNull();` → `assertThat(commandData.getActions()).isNotEmpty();`
  - `((MovePlayerActionData) commandData.getAction()).getLocationId()` → `((MovePlayerActionData) commandData.getActions().getFirst()).getLocationId()`
  - The `assertThatThrownBy(() -> commandData.setAction(null))...` null-guard test → `assertThatThrownBy(() -> commandData.addAction(null)).isInstanceOf(IllegalArgumentException.class)` (the guard now lives on `addAction`).

- [ ] **Step 4:** Hand back to lead for the central build.

---

### Task 7 (Slice D): Hand-built adventures (BO rename) + construction-site sweep

**Files:**
- Modify: `src/main/java/com/pdg/adventure/CommandFactory.java`
- Modify: `src/main/java/com/pdg/adventure/MiniAdventureContent.java`

- [ ] **Step 1:** Rename BO `addFollowUpAction` → `addAction` at:
  - `CommandFactory.java:84` `dropAndRemoveCommand.addFollowUpAction(new RemoveAction(...))` → `.addAction(...)`
  - `MiniAdventureContent.java:282–284` three `cutSuccessfully.addFollowUpAction(...)` → `.addAction(...)`

- [ ] **Step 2:** Verify no other callers of the renamed BO methods remain:
  ```bash
  grep -rn "\.addFollowUpAction(\|\.getFollowUpActions()\|\.getAction()" src --include=*.java
  ```
  Expected: only matches on `CommandData`/model already handled in other slices, none on the BO `Command`. Fix any stragglers (e.g. a BO `getFollowUpActions()` call → `getActions()`). The retained `GenericCommand(desc, action)` constructor means `new GenericCommand(...)` sites (incl. all tests: `CommandExecuterTest`, `GenericCommandProviderTest`, `DirectionTest`, `LocationTest`, `ThingTest`, `CommandHandlerExamineFallbackTest`) need **no change**.

- [ ] **Step 3:** Hand back to lead for the central build.

---

## Phase 3 — Integrate & verify

### Task 8: Full build + green suite

- [ ] **Step 1: Compile**
  ```bash
  cd server && mvn -q -DskipTests compile test-compile
  ```
  Fix any compile errors surfaced (most likely: unused imports in `CommandEditorView`, a missed BO `getFollowUpActions()` caller, or a `List<? extends ActionData>` assignment). Use the `grep` from Task 7 Step 2 to confirm zero stale accessor calls.

- [ ] **Step 2: Run the full suite**
  ```bash
  mvn -q test
  ```
  Expected: all tests pass (baseline ~647). Investigate any failure with the systematic-debugging skill — pay special attention to any test asserting command result messages (the §5 semantics) or usage-tracker labels (Task 5 Step 2).

- [ ] **Step 3: Final review & commit**
  ```bash
  git add -A
  git commit -m "refactor: unify command actions into a single ordered list and finish action-list editor"
  ```
  Then use superpowers:requesting-code-review and superpowers:finishing-a-development-branch to integrate.

---

## Self-review (against the spec)

**Spec coverage:**
- §4.1 model → Task 1. §4.2 runtime + §5 semantics → Task 2 (+ execute test). §4.3 mapper → Task 3. §4.4 editor → Task 4. §4.5 trackers → Task 5. §4.6 direction/item + construction → Tasks 6, 7. Testing strategy → Tasks 2/3/4/5/6 + Task 8. ✓
- Rename `followUpActions`→`actions` (model getActions/setActions, BO getActions/addAction) → Tasks 1, 2, 7. ✓
- Former primary → first list element: enforced by mapToBO order (Task 3) and replace-semantics edits (Task 6). ✓
- `mapToDO` compile-only → Task 3. ✓
- Exclude `LoginView.setAction("login")` (Vaadin) — not in any task's file set. ✓

**Type/name consistency:** `getActions()`/`setActions(List<ActionData>)` (model), `getActions()`/`addAction(Action)` (BO), `ActionListEditor.setActions/getActions/validate`, `PreconditionActionEditor.setCommand/saveToCommand/validate`, `ActionRow.toActionData/validate`. Constructor `GenericCommand(CommandDescription)` and `GenericCommand(CommandDescription, Action)` both defined in Task 2 and relied on in Task 3. ✓

**Placeholder scan:** Foundation + net-new components are complete code; existing-file edits are exact old→new transforms with anchors. No "TBD"/"handle edge cases". ✓
