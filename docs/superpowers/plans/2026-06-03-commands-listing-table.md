# Commands Listing Table Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `CommandsMenuView` list one row per `CommandData` with columns **ID, verb, adjective, noun, preconditions, actions**, where preconditions/actions render as compact text (e.g. `NOT_HERE dragon`, `SETVAR b_fill 1`).

**Architecture:** A new pure `PreconditionActionFormatter` renders `PreConditionData`/`ActionData` to single-line strings. `CommandsMenuView` switches from a one-row-per-spec `Grid<CommandDescriptionAdapter>` to a one-row-per-`CommandData` `Grid<CommandData>`, flattening all command chains and using `ComponentRenderer` for the multi-line precondition/action cells. `CommandEditorView` is decoupled from the menu's data view (see Decision 3). The now-unused `CommandDescriptionAdapter` is deleted.

**Tech Stack:** Java 21, Spring Boot, Vaadin Flow (Grid, ComponentRenderer), Lombok, JUnit 5 + AssertJ + Mockito, Maven.

---

## Architecture Decisions (read before implementing)

**Decision 1 — One row per `CommandData` (flatten chains).** `CommandProviderData.availableCommands` is `Map<String, CommandChainData>`; each chain holds `List<CommandData>`. The mockup shows two rows (`123`, `xyz`) that share the spec `open cage` but differ in id/preconditions/actions — so a row is a single `CommandData`, gathered across all chains.

**Decision 2 — New pure `PreconditionActionFormatter` (the genuinely new piece).** Actions have no summary today (only a type name); the existing condition `getConditionSummary()` reads built editor UI state, not data, so it is not reusable. The formatter switches on the concrete subtype, recurses for `Not`, and uses `AdventureData` to resolve `thingId`/`locationId` → display names. Notation is the mockup's uppercase DSL (`HERE`, `NOT_`, `SETVAR`, `CREATE_ITEM`, `QUIT`, …) — invented here because those tokens exist nowhere in the codebase. `AndConditionData`/`OrConditionData` are obsolete and intentionally **not** handled (a stray instance falls through to the safe default-name branch).

**Decision 3 — Decouple `CommandEditorView` from the menu's data view (delete, don't migrate).** Today `CommandsMenuView` threads its `GridListDataView<CommandDescriptionAdapter>` into `CommandEditorView.swivelTheSaveButton`, which incrementally adds/removes adapter rows. That code assumes **one row per spec** — `getItems().filter(... .equals(commandId)).findFirst()...removeItem` would delete one of N rows once a spec maps to several `CommandData`. It is also redundant: `validateSave()` ends with `navigateBack()`, which re-runs `CommandsMenuView.setData()` (a full reload from data). So the correct resolution is to **remove** the third `setData` parameter, the field, and the grid-sync calls — not to migrate them. Menu freshness after save is guaranteed by the reload.

**Known limitation (intended, document for the user):** Editing is keyed by the command spec; `CommandEditorView` is chain-aware and opens at chain index 0. Double-clicking the 2nd row of a shared spec (e.g. `xyz` in `open cage`) opens the editor on the chain's first entry (`123`). Acceptable — the editor's own chain sub-grid lets the author switch.

**ID column:** `BasicData.id` is an auto-generated ULID and is each `CommandData`'s identity (good for grid item identity). The ID column shows `ViewSupporter.formatId(...)` (truncated to 26 chars); real ids are ULIDs, not the mockup's `abc`/`123`.

**Testing note:** `CommandsMenuView` exposes no grid-row count; do **not** add a seam just to assert flattening headlessly. Keep `CommandsMenuViewTest` at the data-state level (as it is). Real coverage lives in `PreconditionActionFormatterTest`.

---

## File Structure

- **Create** `server/src/main/java/com/pdg/adventure/view/command/PreconditionActionFormatter.java` — pure data→text renderer.
- **Create** `server/src/test/java/com/pdg/adventure/view/command/PreconditionActionFormatterConditionsTest.java`
- **Create** `server/src/test/java/com/pdg/adventure/view/command/PreconditionActionFormatterActionsTest.java`
- **Modify** `server/src/main/java/com/pdg/adventure/view/command/CommandEditorView.java` — drop 3rd `setData` param + grid-sync (Decision 3).
- **Modify** `server/src/test/java/com/pdg/adventure/view/command/CommandEditorViewTest.java` — 2-arg `setData`, drop `gridDataView` helper.
- **Modify** `server/src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java` — `Grid<CommandData>`, flatten, 6 columns, edit-by-spec, delete-per-command.
- **Delete** `server/src/main/java/com/pdg/adventure/view/command/CommandDescriptionAdapter.java` and `server/src/test/java/com/pdg/adventure/view/command/CommandDescriptionAdapterTest.java`.

**Commands** (run from anywhere; `-f` points at the server pom):
- Single test class: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='<ClassName>'`
- Full suite (final verification): `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml test` (or the `verify-tests` skill for the pass count).
- All `git` commands run against the repo at `/Users/mafw/workroom/projects/adventurebuilder/server`.

---

## Task 1: PreconditionActionFormatter — conditions

> **Scope update (user):** `AndConditionData`/`OrConditionData` are obsolete and intentionally omitted — the actual files created below do **not** include their two `if` branches in `formatCondition`, the `andJoinsWithAnd`/`orJoinsWithOr` tests, or the `AndConditionData`/`OrConditionData` imports.

**Files:**
- Create: `server/src/main/java/com/pdg/adventure/view/command/PreconditionActionFormatter.java`
- Test: `server/src/test/java/com/pdg/adventure/view/command/PreconditionActionFormatterConditionsTest.java`

- [ ] **Step 1: Write the failing test**

Create `PreconditionActionFormatterConditionsTest.java`:

```java
package com.pdg.adventure.view.command;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.AndConditionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.OrConditionData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.model.condition.WornConditionData;

class PreconditionActionFormatterConditionsTest {

    private final AdventureData adventureData = new AdventureData();
    private final PreconditionActionFormatter formatter = new PreconditionActionFormatter(adventureData);

    private static HereConditionData here(String id) {
        HereConditionData h = new HereConditionData();
        h.setThingId(id);
        return h;
    }

    @Test
    void here() {
        assertThat(formatter.formatCondition(here("bucket"))).isEqualTo("HERE bucket");
    }

    @Test
    void carried() {
        CarriedConditionData c = new CarriedConditionData();
        c.setItemId("key");
        assertThat(formatter.formatCondition(c)).isEqualTo("CARRIED key");
    }

    @Test
    void worn() {
        WornConditionData c = new WornConditionData();
        c.setThingId("cloak");
        assertThat(formatter.formatCondition(c)).isEqualTo("WORN cloak");
    }

    @Test
    void playerAt() {
        PlayerAtConditionData c = new PlayerAtConditionData();
        c.setLocationId("cave");
        assertThat(formatter.formatCondition(c)).isEqualTo("PLAYER_AT cave");
    }

    @Test
    void itemAt() {
        ItemAtConditionData c = new ItemAtConditionData();
        c.setThingId("gem");
        c.setLocationId("vault");
        assertThat(formatter.formatCondition(c)).isEqualTo("ITEM_AT gem vault");
    }

    @Test
    void equals_() {
        EqualsConditionData c = new EqualsConditionData();
        c.setVariableName("score");
        c.setValue("5");
        assertThat(formatter.formatCondition(c)).isEqualTo("EQ score 5");
    }

    @Test
    void greaterThan() {
        GreaterThanConditionData c = new GreaterThanConditionData();
        c.setVariableName("score");
        c.setValue(3);
        assertThat(formatter.formatCondition(c)).isEqualTo("GT score 3");
    }

    @Test
    void lowerThan() {
        LowerThanConditionData c = new LowerThanConditionData();
        c.setVariableName("score");
        c.setValue(10);
        assertThat(formatter.formatCondition(c)).isEqualTo("LT score 10");
    }

    @Test
    void same() {
        SameConditionData c = new SameConditionData();
        c.setVariableNameOne("a");
        c.setVariableNameTwo("b");
        assertThat(formatter.formatCondition(c)).isEqualTo("SAME a b");
    }

    @Test
    void notWrapsLeafWithPrefix() {
        NotConditionData not = new NotConditionData();
        not.setPreCondition(here("dragon"));
        assertThat(formatter.formatCondition(not)).isEqualTo("NOT_HERE dragon");
    }

    @Test
    void andJoinsWithAnd() {
        AndConditionData and = new AndConditionData();
        and.setPreCondition(here("a"));
        CarriedConditionData car = new CarriedConditionData();
        car.setItemId("b");
        and.setAnotherPreCondition(car);
        assertThat(formatter.formatCondition(and)).isEqualTo("HERE a AND CARRIED b");
    }

    @Test
    void orJoinsWithOr() {
        OrConditionData or = new OrConditionData();
        or.setPreCondition(here("a"));
        WornConditionData worn = new WornConditionData();
        worn.setThingId("b");
        or.setAnotherPreCondition(worn);
        assertThat(formatter.formatCondition(or)).isEqualTo("HERE a OR WORN b");
    }

    @Test
    void nullConditionIsRenderedSafely() {
        assertThat(formatter.formatCondition(null)).isEqualTo("?");
    }

    @Test
    void blankThingIdIsRenderedSafely() {
        assertThat(formatter.formatCondition(here(null))).isEqualTo("HERE ?");
    }

    @Test
    void formatConditionsReturnsOneLinePerEntry() {
        NotConditionData n1 = new NotConditionData();
        n1.setPreCondition(here("dragon"));
        NotConditionData n2 = new NotConditionData();
        n2.setPreCondition(here("chest"));
        assertThat(formatter.formatConditions(List.of(n1, n2)))
                .containsExactly("NOT_HERE dragon", "NOT_HERE chest");
    }

    @Test
    void formatConditionsHandlesNullList() {
        assertThat(formatter.formatConditions(null)).isEmpty();
    }

    @Test
    void resolvesThingIdToItemDescription() {
        ItemData bucket = new ItemData();
        bucket.setId("item-1");
        bucket.setDescriptionData(new DescriptionData("bucket", "a metal bucket"));
        adventureData.getPlayerPocket().getItems().add(bucket);
        // Formatter indexes items at construction time, so build a fresh one after adding the item.
        PreconditionActionFormatter f = new PreconditionActionFormatter(adventureData);
        assertThat(f.formatCondition(here("item-1"))).isEqualTo("HERE bucket");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='PreconditionActionFormatterConditionsTest'`
Expected: FAIL — compilation error, `PreconditionActionFormatter` does not exist.

- [ ] **Step 3: Write minimal implementation**

Create `PreconditionActionFormatter.java` (the action methods are added in Task 2; this compiles and passes the condition tests now):

```java
package com.pdg.adventure.view.command;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.condition.AndConditionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.OrConditionData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Renders preconditions and actions to compact, single-line text for the commands listing grid
 * (e.g. "NOT_HERE dragon", "SETVAR b_fill 1"). Pure: depends only on the supplied AdventureData
 * for id -> display-name resolution. No Vaadin UI, so it is unit-testable in isolation.
 */
public class PreconditionActionFormatter {
    private final Map<String, ItemData> itemsById;
    private final Map<String, LocationData> locationsById;

    public PreconditionActionFormatter(AdventureData adventureData) {
        itemsById = indexItems(adventureData);
        locationsById = adventureData.getLocationData() == null ? Map.of() : adventureData.getLocationData();
    }

    public List<String> formatConditions(List<PreConditionData> conditions) {
        if (conditions == null) {
            return List.of();
        }
        return conditions.stream().map(this::formatCondition).collect(Collectors.toList());
    }

    public String formatCondition(PreConditionData c) {
        if (c == null) {
            return "?";
        }
        if (c instanceof NotConditionData not) {
            return "NOT_" + formatCondition(not.getPreCondition());
        }
        if (c instanceof AndConditionData and) {
            return formatCondition(and.getPreCondition()) + " AND " + formatCondition(and.getAnotherPreCondition());
        }
        if (c instanceof OrConditionData or) {
            return formatCondition(or.getPreCondition()) + " OR " + formatCondition(or.getAnotherPreCondition());
        }
        if (c instanceof HereConditionData here) {
            return "HERE " + resolveName(here.getThingId());
        }
        if (c instanceof CarriedConditionData carried) {
            return "CARRIED " + resolveName(carried.getItemId());
        }
        if (c instanceof WornConditionData worn) {
            return "WORN " + resolveName(worn.getThingId());
        }
        if (c instanceof PlayerAtConditionData playerAt) {
            return "PLAYER_AT " + resolveName(playerAt.getLocationId());
        }
        if (c instanceof ItemAtConditionData itemAt) {
            return "ITEM_AT " + resolveName(itemAt.getThingId()) + " " + resolveName(itemAt.getLocationId());
        }
        if (c instanceof EqualsConditionData eq) {
            return "EQ " + txt(eq.getVariableName()) + " " + txt(eq.getValue());
        }
        if (c instanceof GreaterThanConditionData gt) {
            return "GT " + txt(gt.getVariableName()) + " " + num(gt.getValue());
        }
        if (c instanceof LowerThanConditionData lt) {
            return "LT " + txt(lt.getVariableName()) + " " + num(lt.getValue());
        }
        if (c instanceof SameConditionData same) {
            return "SAME " + txt(same.getVariableNameOne()) + " " + txt(same.getVariableNameTwo());
        }
        return c.getPreconditionName().replace("ConditionData", "").toUpperCase(Locale.ROOT);
    }

    private String resolveName(String id) {
        if (id == null || id.isBlank()) {
            return "?";
        }
        ItemData item = itemsById.get(id);
        if (item != null) {
            return ViewSupporter.formatDescription(item);
        }
        LocationData location = locationsById.get(id);
        if (location != null) {
            return ViewSupporter.getLocationsShortedDescription(location);
        }
        return id;
    }

    private static String txt(String s) {
        return (s == null || s.isBlank()) ? "?" : s;
    }

    private static String num(Number n) {
        return n == null ? "?" : String.valueOf(n);
    }

    private static Map<String, ItemData> indexItems(AdventureData data) {
        Map<String, ItemData> map = new HashMap<>();
        if (data.getLocationData() != null) {
            for (LocationData loc : data.getLocationData().values()) {
                ItemContainerData container = loc.getItemContainerData();
                if (container != null && container.getItems() != null) {
                    container.getItems().forEach(i -> map.put(i.getId(), i));
                }
            }
        }
        if (data.getPlayerPocket() != null && data.getPlayerPocket().getItems() != null) {
            data.getPlayerPocket().getItems().forEach(i -> map.put(i.getId(), i));
        }
        return map;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='PreconditionActionFormatterConditionsTest'`
Expected: PASS (all condition tests green).

- [ ] **Step 5: Commit**

```bash
git -C /Users/mafw/workroom/projects/adventurebuilder/server add server/src/main/java/com/pdg/adventure/view/command/PreconditionActionFormatter.java server/src/test/java/com/pdg/adventure/view/command/PreconditionActionFormatterConditionsTest.java
git -C /Users/mafw/workroom/projects/adventurebuilder/server commit -m "feat: add PreconditionActionFormatter condition rendering"
```

---

## Task 2: PreconditionActionFormatter — actions

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/command/PreconditionActionFormatter.java`
- Test: `server/src/test/java/com/pdg/adventure/view/command/PreconditionActionFormatterActionsTest.java`

- [ ] **Step 1: Write the failing test**

Create `PreconditionActionFormatterActionsTest.java`:

```java
package com.pdg.adventure.view.command;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.model.action.InventoryActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.model.action.WearActionData;

class PreconditionActionFormatterActionsTest {

    private final AdventureData adventureData = new AdventureData();
    private final PreconditionActionFormatter formatter = new PreconditionActionFormatter(adventureData);

    @Test
    void setVariable() {
        assertThat(formatter.formatAction(new SetVariableActionData("b_fill", "1"))).isEqualTo("SETVAR b_fill 1");
    }

    @Test
    void incrementVariable() {
        IncrementVariableActionData a = new IncrementVariableActionData();
        a.setName("score");
        a.setValue("2");
        assertThat(formatter.formatAction(a)).isEqualTo("INCVAR score 2");
    }

    @Test
    void decrementVariable() {
        DecrementVariableActionData a = new DecrementVariableActionData();
        a.setName("score");
        a.setValue("1");
        assertThat(formatter.formatAction(a)).isEqualTo("DECVAR score 1");
    }

    @Test
    void message() {
        MessageActionData a = new MessageActionData();
        a.setMessageId("cage_opened");
        assertThat(formatter.formatAction(a)).isEqualTo("MESSAGE cage_opened");
    }

    @Test
    void create() {
        CreateActionData a = new CreateActionData();
        a.setThingId("chest");
        a.setContainerProviderId("room");
        assertThat(formatter.formatAction(a)).isEqualTo("CREATE_ITEM chest");
    }

    @Test
    void destroy() {
        DestroyActionData a = new DestroyActionData();
        a.setThingId("vase");
        assertThat(formatter.formatAction(a)).isEqualTo("DESTROY vase");
    }

    @Test
    void drop() {
        DropActionData a = new DropActionData();
        a.setThingId("key");
        assertThat(formatter.formatAction(a)).isEqualTo("DROP key");
    }

    @Test
    void take() {
        TakeActionData a = new TakeActionData();
        a.setThingId("coin");
        assertThat(formatter.formatAction(a)).isEqualTo("TAKE coin");
    }

    @Test
    void wear() {
        WearActionData a = new WearActionData();
        a.setThingId("cloak");
        assertThat(formatter.formatAction(a)).isEqualTo("WEAR cloak");
    }

    @Test
    void remove() {
        RemoveActionData a = new RemoveActionData();
        a.setThingId("ring");
        assertThat(formatter.formatAction(a)).isEqualTo("REMOVE ring");
    }

    @Test
    void moveItem() {
        MoveItemActionData a = new MoveItemActionData();
        a.setThingId("apple");
        a.setDestinationId("basket");
        assertThat(formatter.formatAction(a)).isEqualTo("MOVE_ITEM apple basket");
    }

    @Test
    void movePlayer() {
        MovePlayerActionData a = new MovePlayerActionData();
        a.setLocationId("cave");
        assertThat(formatter.formatAction(a)).isEqualTo("MOVE_PLAYER cave");
    }

    @Test
    void describe() {
        DescribeActionData a = new DescribeActionData();
        a.setTargetId("sign");
        assertThat(formatter.formatAction(a)).isEqualTo("DESCRIBE sign");
    }

    @Test
    void inventory() {
        assertThat(formatter.formatAction(new InventoryActionData())).isEqualTo("INVENTORY");
    }

    @Test
    void quit() {
        assertThat(formatter.formatAction(new QuitActionData())).isEqualTo("QUIT");
    }

    @Test
    void nullActionIsRenderedSafely() {
        assertThat(formatter.formatAction(null)).isEqualTo("?");
    }

    @Test
    void formatActionsReturnsOneLinePerEntry() {
        MessageActionData m = new MessageActionData();
        m.setMessageId("dragon_fight");
        assertThat(formatter.formatActions(List.of(m, new QuitActionData())))
                .containsExactly("MESSAGE dragon_fight", "QUIT");
    }

    @Test
    void formatActionsHandlesNullList() {
        assertThat(formatter.formatActions(null)).isEmpty();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='PreconditionActionFormatterActionsTest'`
Expected: FAIL — `formatAction`/`formatActions` do not exist (compilation error).

- [ ] **Step 3: Add the action methods**

Add these imports to `PreconditionActionFormatter.java` (with the existing imports):

```java
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.model.action.DescribeActionData;
import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.model.action.InventoryActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.model.action.WearActionData;
```

Add these two methods to the class (e.g. directly after `formatCondition`):

```java
    public List<String> formatActions(List<ActionData> actions) {
        if (actions == null) {
            return List.of();
        }
        return actions.stream().map(this::formatAction).collect(Collectors.toList());
    }

    public String formatAction(ActionData a) {
        if (a == null) {
            return "?";
        }
        if (a instanceof SetVariableActionData sv) {
            return "SETVAR " + txt(sv.getVariableName()) + " " + txt(sv.getVariableValue());
        }
        if (a instanceof IncrementVariableActionData iv) {
            return "INCVAR " + txt(iv.getName()) + " " + txt(iv.getValue());
        }
        if (a instanceof DecrementVariableActionData dv) {
            return "DECVAR " + txt(dv.getName()) + " " + txt(dv.getValue());
        }
        if (a instanceof MessageActionData m) {
            return "MESSAGE " + txt(m.getMessageId());
        }
        if (a instanceof CreateActionData cr) {
            return "CREATE_ITEM " + resolveName(cr.getThingId());
        }
        if (a instanceof DestroyActionData d) {
            return "DESTROY " + resolveName(d.getThingId());
        }
        if (a instanceof DropActionData d) {
            return "DROP " + resolveName(d.getThingId());
        }
        if (a instanceof TakeActionData t) {
            return "TAKE " + resolveName(t.getThingId());
        }
        if (a instanceof WearActionData w) {
            return "WEAR " + resolveName(w.getThingId());
        }
        if (a instanceof RemoveActionData r) {
            return "REMOVE " + resolveName(r.getThingId());
        }
        if (a instanceof MoveItemActionData mi) {
            return "MOVE_ITEM " + resolveName(mi.getThingId()) + " " + resolveName(mi.getDestinationId());
        }
        if (a instanceof MovePlayerActionData mp) {
            return "MOVE_PLAYER " + resolveName(mp.getLocationId());
        }
        if (a instanceof DescribeActionData de) {
            return "DESCRIBE " + resolveName(de.getTargetId());
        }
        if (a instanceof InventoryActionData) {
            return "INVENTORY";
        }
        if (a instanceof QuitActionData) {
            return "QUIT";
        }
        return a.getActionName().replace("ActionData", "").toUpperCase(Locale.ROOT);
    }
```

- [ ] **Step 4: Run both formatter test classes to verify they pass**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='PreconditionActionFormatterConditionsTest,PreconditionActionFormatterActionsTest'`
Expected: PASS (both classes green).

- [ ] **Step 5: Commit**

```bash
git -C /Users/mafw/workroom/projects/adventurebuilder/server add server/src/main/java/com/pdg/adventure/view/command/PreconditionActionFormatter.java server/src/test/java/com/pdg/adventure/view/command/PreconditionActionFormatterActionsTest.java
git -C /Users/mafw/workroom/projects/adventurebuilder/server commit -m "feat: add PreconditionActionFormatter action rendering"
```

---

## Task 3: Decouple CommandEditorView from the menu data view

Implements Decision 3. After this task the editor no longer receives a `GridListDataView`; `CommandsMenuView` still compiles using its existing adapter grid (its navigation calls just drop the 3rd argument).

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/command/CommandEditorView.java`
- Modify: `server/src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java` (drop 3rd arg at the two call sites only)
- Test: `server/src/test/java/com/pdg/adventure/view/command/CommandEditorViewTest.java`

- [ ] **Step 1: Update the editor test to the new 2-arg `setData`**

In `CommandEditorViewTest.java`:

Replace the call on line 98:
```java
        view.setData(adventureData, locationData, null);
```
with:
```java
        view.setData(adventureData, locationData);
```

Replace the assertion block on lines 146-147:
```java
        assertThatCode(() -> view.setData(adventureData, locationData, gridDataView(spec)))
                .doesNotThrowAnyException();
```
with:
```java
        assertThatCode(() -> view.setData(adventureData, locationData))
                .doesNotThrowAnyException();
```

Delete the now-unused helper (lines 150-153):
```java
    private GridListDataView<CommandDescriptionAdapter> gridDataView(String spec) {
        Grid<CommandDescriptionAdapter> grid = new Grid<>();
        return grid.setItems(List.of(new CommandDescriptionAdapter(spec)));
    }
```

Delete the now-unused imports at the top of the test:
```java
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
```
and
```java
import java.util.List;
```

- [ ] **Step 2: Run the editor test to verify it fails to compile**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='CommandEditorViewTest'`
Expected: FAIL — `setData(AdventureData, LocationData)` does not exist yet (the production signature is still 3-arg).

- [ ] **Step 3: Edit `CommandEditorView` — drop the field, the 3rd param, and the grid-sync**

In `CommandEditorView.java`:

(a) Delete the field (line 57):
```java
    private GridListDataView<CommandDescriptionAdapter> gridListDataView;
```

(b) Delete the unused import:
```java
import com.vaadin.flow.component.grid.dataview.GridListDataView;
```

(c) Change `setData`'s signature and drop the assignment. Replace:
```java
    public void setData(AdventureData anAdventureData, LocationData aLocationData,
                        GridListDataView<CommandDescriptionAdapter> aGridListDataView) {
        adventureData = anAdventureData;
        locationData = aLocationData;
        gridListDataView = aGridListDataView;

        commandProviderData = locationData.getCommandProviderData();
```
with:
```java
    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        commandProviderData = locationData.getCommandProviderData();
```

(d) Update the `validateSave` call. Replace:
```java
                commandData = swivelTheSaveButton(gridListDataView);
```
with:
```java
                commandData = swivelTheSaveButton();
```

(e) Replace the entire `swivelTheSaveButton` method (drop the param and the three `aGridListDataView.*` grid-sync calls; keep all data mutations). Replace:
```java
    private CommandData swivelTheSaveButton(GridListDataView<CommandDescriptionAdapter> aGridListDataView) {
        // Use the commandDescriptionData that was updated via the binder
        final CommandDescriptionData updatedCommandDescription = cvm.getData();
        final String newSpecification = updatedCommandDescription.getCommandSpecification();

        final Map<String, CommandChainData> availableCommandsHelper = commandProviderData.getAvailableCommands();

        // If editing an existing command and the specification has changed, remove the old entry
        if (commandId != null && !commandId.isEmpty() && !commandId.equals(newSpecification)) {
            availableCommandsHelper.remove(commandId);
            // Remove old item from grid
            aGridListDataView.getItems().filter(item -> item.getShortDescription().equals(commandId)).findFirst()
                             .ifPresent(aGridListDataView::removeItem);
        }

        // Determine if we're editing an existing command or creating a new one
        boolean isEditingExistingCommand = commandId != null && !commandId.isEmpty() &&
                                           commandId.equals(newSpecification);

        final CommandData command = getEditingCommandData(isEditingExistingCommand, updatedCommandDescription);

        // Persist the preconditions and actions from the editor
        preconditionActionEditor.saveToCommand(command);

        final CommandChainData commandChainData = availableCommandsHelper.get(newSpecification);
        if (commandChainData == null) {
            // New command - create new chain
            final CommandChainData chainData = new CommandChainData();
            chainData.getCommands().add(command);
            availableCommandsHelper.put(newSpecification, chainData);
            // Add to grid only if it's truly new
            aGridListDataView.addItem(new CommandDescriptionAdapter(newSpecification));
        } else if (!isEditingExistingCommand) {
            // Command specification already exists and we're adding a new variant (not editing existing)
            // Commands with the same description are chained together
            // The chain will execute commands until one with met preconditions succeeds
            commandChainData.getCommands().add(command);
            // Refresh grid to show updated data
            aGridListDataView.refreshAll();
        }
        // If isEditingExistingCommand is true, the command is already in the chain and has been updated in place

        return command;
    }
```
with:
```java
    private CommandData swivelTheSaveButton() {
        // Use the commandDescriptionData that was updated via the binder
        final CommandDescriptionData updatedCommandDescription = cvm.getData();
        final String newSpecification = updatedCommandDescription.getCommandSpecification();

        final Map<String, CommandChainData> availableCommandsHelper = commandProviderData.getAvailableCommands();

        // If editing an existing command and the specification has changed, remove the old entry
        if (commandId != null && !commandId.isEmpty() && !commandId.equals(newSpecification)) {
            availableCommandsHelper.remove(commandId);
        }

        // Determine if we're editing an existing command or creating a new one
        boolean isEditingExistingCommand = commandId != null && !commandId.isEmpty() &&
                                           commandId.equals(newSpecification);

        final CommandData command = getEditingCommandData(isEditingExistingCommand, updatedCommandDescription);

        // Persist the preconditions and actions from the editor
        preconditionActionEditor.saveToCommand(command);

        final CommandChainData commandChainData = availableCommandsHelper.get(newSpecification);
        if (commandChainData == null) {
            // New command - create new chain
            final CommandChainData chainData = new CommandChainData();
            chainData.getCommands().add(command);
            availableCommandsHelper.put(newSpecification, chainData);
        } else if (!isEditingExistingCommand) {
            // Command specification already exists and we're adding a new variant (not editing existing).
            // Commands with the same description are chained together; the chain executes until one
            // with met preconditions succeeds.
            commandChainData.getCommands().add(command);
        }
        // If isEditingExistingCommand is true, the command is already in the chain and updated in place.
        // The menu grid reflects all of the above on return: navigateBack() re-runs CommandsMenuView.setData().

        return command;
    }
```

- [ ] **Step 4: Drop the 3rd argument at the two `CommandsMenuView` call sites**

In `CommandsMenuView.java`, replace (inside `createButton`, lines ~63-66):
```java
            UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData, gridListDataView));
```
with:
```java
            UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData));
```

And replace (inside `navigateToCommandEditor`, lines ~162-166):
```java
        UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                  new RouteParam(RouteIds.COMMAND_ID.getValue(), aCommandId),
                  new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                  new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
          .ifPresent(editor -> editor.setData(adventureData, locationData, gridListDataView));
```
with:
```java
        UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                  new RouteParam(RouteIds.COMMAND_ID.getValue(), aCommandId),
                  new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                  new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
          .ifPresent(editor -> editor.setData(adventureData, locationData));
```

- [ ] **Step 5: Run editor + menu tests to verify they pass**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='CommandEditorViewTest,CommandsMenuViewTest'`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git -C /Users/mafw/workroom/projects/adventurebuilder/server add -A
git -C /Users/mafw/workroom/projects/adventurebuilder/server commit -m "refactor: decouple CommandEditorView from the menu grid data view"
```

---

## Task 4: CommandsMenuView — one row per CommandData with 6 columns

Implements Decisions 1 and 2. Replaces the adapter grid with `Grid<CommandData>`.

**Files:**
- Modify: `server/src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java`
- Test: `server/src/test/java/com/pdg/adventure/view/command/CommandsMenuViewTest.java`

- [ ] **Step 1: Extend the menu test with a chain that has multiple commands**

The existing two tests stay (data-state level). Add a regression test that a single spec mapping to several `CommandData` does not collapse rows and that `setData` builds the grid without throwing. Add to `CommandsMenuViewTest.java`:

```java
    @Test
    void setData_withChainOfMultipleCommands_buildsWithoutThrowing() {
        // given: one spec ("open||cage") mapped to a chain of two distinct CommandData (cf. the
        // mockup's "123"/"xyz" rows). Each row is a CommandData, so the chain must not collapse.
        view = new CommandsMenuView(adventureService);

        CommandDescriptionData openCage = new CommandDescriptionData("open||cage");

        CommandData first = new CommandData();
        first.setCommandDescription(openCage);
        MessageActionData firstMsg = new MessageActionData();
        firstMsg.setMessageId("cage_opened");
        first.addAction(firstMsg);

        CommandData second = new CommandData();
        second.setCommandDescription(openCage);
        second.addAction(new QuitActionData());

        CommandChainData chain = new CommandChainData();
        chain.getCommands().add(first);
        chain.getCommands().add(second);
        commandProviderData.getAvailableCommands().put("open||cage", chain);

        // when / then
        org.assertj.core.api.Assertions.assertThatCode(() -> view.setData(adventureData, locationData))
                .doesNotThrowAnyException();
        assertThat(chain.getCommands()).hasSize(2);
    }
```

Add the imports needed by this test to `CommandsMenuViewTest.java`:
```java
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
```
(`CommandData`, `CommandChainData`, `CommandProviderData` are already covered by the existing `import com.pdg.adventure.model.*;`.)

- [ ] **Step 2: Run the menu test to verify it fails**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='CommandsMenuViewTest'`
Expected: FAIL — compilation error: the current grid is `Grid<CommandDescriptionAdapter>` and `getSimpleGrid()` references the old adapter columns; the new test compiles but the grid wiring is replaced in the next step. (If it happens to compile and pass, that is fine — Step 3 still applies the production change and Step 5 re-verifies.)

- [ ] **Step 3: Rewrite the grid wiring in `CommandsMenuView`**

(a) Replace the imports block. Replace:
```java
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
```
with:
```java
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
```
Then replace:
```java
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.location.LocationEditorView;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;
```
with:
```java
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.location.LocationEditorView;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;
```
(Removes the now-unused `GridProvider` and `Map` imports.)

(b) Replace the grid/data-view field declarations. Replace:
```java
    private Grid<CommandDescriptionAdapter> grid;
    private String pageTitle;
    private LocationData locationData;
    private AdventureData adventureData;
    private CommandProviderData commandProviderData;
    private GridListDataView<CommandDescriptionAdapter> gridListDataView;
```
with:
```java
    private Grid<CommandData> grid;
    private String pageTitle;
    private LocationData locationData;
    private AdventureData adventureData;
    private CommandProviderData commandProviderData;
    private GridListDataView<CommandData> gridListDataView;
    private transient PreconditionActionFormatter formatter;
```

(c) Replace `getSimpleGrid()` with a 6-column builder. Replace:
```java
    private Grid<CommandDescriptionAdapter> getSimpleGrid() {
        GridProvider<CommandDescriptionAdapter> gridProvider = new GridProvider<>(CommandDescriptionAdapter.class);
        gridProvider.getGrid().getColumns().get(1).setHeader("Command");
        gridProvider.addColumn(CommandDescriptionAdapter::getVerb, "Verb");
        gridProvider.addColumn(CommandDescriptionAdapter::getAdjective, "Adjective");
        gridProvider.addColumn(CommandDescriptionAdapter::getNoun, "Noun");
        ViewSupporter.setSize(gridProvider.getGrid());
        return gridProvider.getGrid();
    }
```
with:
```java
    private Grid<CommandData> buildGrid() {
        Grid<CommandData> aGrid = new Grid<>(CommandData.class, false);
        aGrid.addColumn(ViewSupporter::formatId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getVerb()))
             .setHeader("Verb").setAutoWidth(true);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getAdjective()))
             .setHeader("Adjective").setAutoWidth(true);
        aGrid.addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getNoun()))
             .setHeader("Noun").setAutoWidth(true);
        aGrid.addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatConditions(cmd.getPreConditions()))))
             .setHeader("Preconditions").setAutoWidth(true);
        aGrid.addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatActions(cmd.getActions()))))
             .setHeader("Actions").setAutoWidth(true);
        aGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        ViewSupporter.setSize(aGrid);
        return aGrid;
    }

    /** Stack each rendered line in its own Span so multi-entry precondition/action cells wrap vertically. */
    private static Component stack(List<String> lines) {
        Div box = new Div();
        box.getStyle().set("display", "flex").set("flex-direction", "column");
        lines.forEach(line -> box.add(new Span(line)));
        return box;
    }
```

(d) Replace `fillGrid` to flatten chains into one row per `CommandData`. Replace:
```java
    private GridListDataView<CommandDescriptionAdapter> fillGrid(CommandProviderData commandProviderData) {
        final Map<String, CommandChainData> availableCommands = commandProviderData.getAvailableCommands();
        final List<CommandDescriptionAdapter> commandDescriptionAdapters = new ArrayList<>(availableCommands.size());
        for (String command : availableCommands.keySet()) {
            commandDescriptionAdapters.add(new CommandDescriptionAdapter(command));
        }
        return grid.setItems(commandDescriptionAdapters);
    }
```
with:
```java
    private GridListDataView<CommandData> fillGrid(CommandProviderData aCommandProviderData) {
        final List<CommandData> rows = new ArrayList<>();
        for (CommandChainData chain : aCommandProviderData.getAvailableCommands().values()) {
            rows.addAll(chain.getCommands());
        }
        return grid.setItems(rows);
    }
```

(e) Update `setData` to build the formatter + grid and navigate by spec on double-click. Replace:
```java
        grid = getSimpleGrid();
//        grid = new GridUnbufferedInlineEditor(availableCommands, vocabularyData, saveButton);
        grid.setEmptyStateText("Create some commands.");

        // Add double-click listener to edit commands
        grid.addItemDoubleClickListener(e -> {
            String commandSpec = e.getItem().getShortDescription(); // This returns the command specification
            navigateToCommandEditor(commandSpec);
        });

        gridListDataView = fillGrid(locationData.getCommandProviderData());
```
with:
```java
        formatter = new PreconditionActionFormatter(adventureData);
        grid = buildGrid();
        grid.setEmptyStateText("Create some commands.");

        // Double-click edits the command. Editing is keyed by the command spec; CommandEditorView is
        // chain-aware and opens the chain (at index 0) for that spec.
        grid.addItemDoubleClickListener(e ->
                navigateToCommandEditor(e.getItem().getCommandDescription().getCommandSpecification()));

        gridListDataView = fillGrid(locationData.getCommandProviderData());
```

(f) Replace the context menu to operate on `CommandData` and delete a single command from its chain. Replace:
```java
    private class CommandContextMenu extends GridContextMenu<CommandDescriptionAdapter> {
        public CommandContextMenu(Grid<CommandDescriptionAdapter> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(command -> {
                String commandSpec = command.getShortDescription();
                navigateToCommandEditor(commandSpec);
            }));

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(command -> {
                String commandSpec = command.getShortDescription();
                // Remove from the data view
                gridListDataView.removeItem(command);
                // Remove from the command provider data
                commandProviderData.getAvailableCommands().remove(commandSpec);
                // Save changes
                adventureService.saveLocationData(locationData);
                // Refresh grid
                gridListDataView.refreshAll();
            }));
        }
    }
```
with:
```java
    private class CommandContextMenu extends GridContextMenu<CommandData> {
        public CommandContextMenu(Grid<CommandData> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(command ->
                    navigateToCommandEditor(command.getCommandDescription().getCommandSpecification())));

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(command -> {
                String commandSpec = command.getCommandDescription().getCommandSpecification();
                CommandChainData chain = commandProviderData.getAvailableCommands().get(commandSpec);
                if (chain != null) {
                    chain.getCommands().remove(command);
                    // Drop the whole spec entry once its chain is empty.
                    if (chain.getCommands().isEmpty()) {
                        commandProviderData.getAvailableCommands().remove(commandSpec);
                    }
                }
                gridListDataView.removeItem(command);
                adventureService.saveLocationData(locationData);
                gridListDataView.refreshAll();
            }));
        }
    }
```

- [ ] **Step 4: Verify there are no remaining `CommandDescriptionAdapter` references in `CommandsMenuView`**

Run: `grep -n "CommandDescriptionAdapter" /Users/mafw/workroom/projects/adventurebuilder/server/src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java`
Expected: no output.

- [ ] **Step 5: Run menu + editor tests to verify they pass**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -q -Dvaadin.skip.frontend.build=true test -Dtest='CommandsMenuViewTest,CommandEditorViewTest'`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git -C /Users/mafw/workroom/projects/adventurebuilder/server add -A
git -C /Users/mafw/workroom/projects/adventurebuilder/server commit -m "feat: list commands per CommandData with preconditions/actions columns"
```

---

## Task 5: Delete the obsolete CommandDescriptionAdapter

**Files:**
- Delete: `server/src/main/java/com/pdg/adventure/view/command/CommandDescriptionAdapter.java`
- Delete: `server/src/test/java/com/pdg/adventure/view/command/CommandDescriptionAdapterTest.java`

- [ ] **Step 1: Confirm there are no remaining references**

Run: `grep -rn "CommandDescriptionAdapter" /Users/mafw/workroom/projects/adventurebuilder/server/src`
Expected: only matches inside `CommandDescriptionAdapter.java` and `CommandDescriptionAdapterTest.java` (the files to delete). If any other file matches, stop and fix it before deleting.

- [ ] **Step 2: Delete the files**

```bash
git -C /Users/mafw/workroom/projects/adventurebuilder/server rm \
  server/src/main/java/com/pdg/adventure/view/command/CommandDescriptionAdapter.java \
  server/src/test/java/com/pdg/adventure/view/command/CommandDescriptionAdapterTest.java
```

- [ ] **Step 3: Run the full test suite**

Run: `mvn -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml test`
Expected: BUILD SUCCESS, 0 failures/errors. (Or use the `verify-tests` skill and report the pass count.)

- [ ] **Step 4: Commit**

```bash
git -C /Users/mafw/workroom/projects/adventurebuilder/server commit -m "refactor: remove obsolete CommandDescriptionAdapter"
```

---

## Done-when

- The commands listing shows one row per `CommandData` with columns ID, verb, adjective, noun, preconditions, actions.
- Preconditions/actions render compactly (e.g. `NOT_HERE dragon`, `SETVAR b_fill 1`), one line per list entry, matching the mockup.
- Editing (double-click / context-menu) and per-command delete work; deleting the last command of a spec removes the spec.
- `CommandDescriptionAdapter` is gone; the full Maven suite passes.
