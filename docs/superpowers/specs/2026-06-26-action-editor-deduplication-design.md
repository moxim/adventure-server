# Action Editor Deduplication Design

**Date:** 2026-06-26  
**Scope:** `com.pdg.adventure.view.command.action`

---

## Problem

Seven action editors duplicate the same item-collection logic, and five of those additionally duplicate the full single-item-selector pattern (UI wiring, validation, summary).

| Duplicated pattern | Editors affected |
|---|---|
| `collectAllItems(AdventureData)` — identical 12-line method | Wear, Take, Drop, Destroy, Remove, Create, MoveItem (7×) |
| `findItemById(List, String)` — identical stream | Wear, Remove, Create, MoveItem (4×; others inline) |
| `buildUI()` item-selector wiring | Wear, Take, Drop, Destroy, Remove (5× near-identical) |
| `validate()` + `getActionSummary()` single-item pattern | Wear, Take, Drop, Destroy, Remove (5× identical) |

---

## Architecture

```
ActionEditorComponent  (existing root abstract class)
├── AbstractSingleItemActionEditor<T extends ActionData>  [NEW]
│   ├── WearActionEditor
│   ├── TakeActionEditor
│   ├── DropActionEditor
│   ├── DestroyActionEditor
│   └── RemoveActionEditor
├── CreateActionEditor          (uses new ViewSupporter helpers)
├── MoveItemActionEditor        (uses new ViewSupporter helpers)
└── ... (all other editors unchanged)

ViewSupporter  (existing utility — 2 new static methods)
  + collectAllItems(AdventureData): List<ItemData>
  + collectAllContainers(AdventureData): List<ItemContainerData>
```

---

## Component Designs

### 1. `ViewSupporter` — 2 new static methods

Location: `com.pdg.adventure.view.support.ViewSupporter`

```java
public static List<ItemData> collectAllItems(AdventureData adventureData)
```
Collects all `ItemData` from every location's `ItemContainerData` plus the player pocket. Replaces 7 identical private copies.

```java
public static List<ItemContainerData> collectAllContainers(AdventureData adventureData)
```
Collects all `ItemContainerData` (player pocket first, then one container per location). Used by `MoveItemActionEditor`.

Both follow the same null-guard pattern already present in all 7 private copies.

---

### 2. `AbstractSingleItemActionEditor<T extends ActionData>` — new class

Package: `com.pdg.adventure.view.command.action`

**Fields:**
- `protected final T typedAction` — typed reference to the concrete action data (avoids casting in subclasses)
- `protected final AdventureData adventureData`
- `private ComboBox<ItemData> itemSelector`
- 5 private `final String` fields: `title`, `descriptionText`, `itemLabel`, `itemPlaceholder`, `itemErrorMessage`

**Constructor:**
```java
protected AbstractSingleItemActionEditor(
        T actionData, AdventureData adventureData,
        String title, String descriptionText,
        String itemLabel, String itemPlaceholder, String itemErrorMessage)
```
Calls `super(actionData)`, stores all fields. String configuration is passed by subclass constructors — no abstract method per constant.

**Abstract methods (2 only):**
```java
protected abstract String currentThingId();
protected abstract void applyThingId(String id);
```
These delegate to the typed action data, keeping model classes untouched.

**Implemented methods (all `final`):**

`buildUI()`:
1. Adds `H4(title)` and `Span(descriptionText)` with secondary text color
2. Calls `ViewSupporter.collectAllItems(adventureData)` to populate the combo box
3. Creates and configures `itemSelector` (label, items, label generator, placeholder, required)
4. Pre-selects the item if `currentThingId()` is non-null (stream find)
5. Wires value-change listener: calls `applyThingId(id)` or `applyThingId(null)`
6. Calls `add(title, description, itemSelector)`

`validate()`:
- Returns `itemSelector.getValue() != null`
- Sets `errorMessage` + `invalid(true/false)` on `itemSelector` accordingly

`getActionSummary()`:
- Returns `"(none)"` if selector is null or has no value
- Otherwise returns `ViewSupporter.formatDescription(itemSelector.getValue())`

---

### 3. Five concrete editors — refactored

Each of `WearActionEditor`, `TakeActionEditor`, `DropActionEditor`, `DestroyActionEditor`, `RemoveActionEditor` becomes ~12 lines:

```java
public class WearActionEditor
        extends AbstractSingleItemActionEditor<WearActionData> {

    public WearActionEditor(WearActionData actionData, AdventureData adventureData) {
        super(actionData, adventureData,
              "Wear Action",
              "Select the wearable item the player will wear. Shows all items; ensure you select a wearable one.",
              "Item to Wear", "Select item to wear", "Please select an item to wear");
    }

    @Override protected String currentThingId() { return typedAction.getThingId(); }
    @Override protected void applyThingId(String id) { typedAction.setThingId(id); }
}
```

`DestroyActionEditor` will gain a title (`"Destroy Action"`) and description (it currently has neither), making it consistent with all other editors.

---

### 4. `CreateActionEditor` and `MoveItemActionEditor` — partial refactor

These editors keep their own `buildUI()` / `validate()` / `getActionSummary()` because their dual-selector pattern is unique. Only the private helpers are replaced:

- `collectAllItems()` → `ViewSupporter.collectAllItems(adventureData)`
- `findItemById(allItems, id)` → inline `allItems.stream().filter(...).findFirst().orElse(null)` (already the pattern in Take/Drop)
- `collectAllContainers()` (`MoveItemActionEditor` only) → `ViewSupporter.collectAllContainers(adventureData)`
- `findContainerById(containers, id)` → inline stream

`CreateActionEditor`'s `findLocationById()` is replaced with an inline stream on `allLocations`.

---

## Data Flow

No change to data flow. The editors still bind directly to their `ActionData` objects; `AdventureData` is read-only at construction time to populate combo box choices.

---

## Testing

Existing tests for all 5 concrete editors remain valid — they construct the concrete class and call `initialize()`, `validate()`, `getActionSummary()` etc., which now execute through the abstract base. No test changes are required. The abstract base class is covered implicitly by all 5 concrete test suites.

No new test class for `AbstractSingleItemActionEditor` itself (it has no behaviour of its own beyond what the concrete tests already exercise).

`ViewSupporter` additions are covered by the existing concrete editor tests (they exercise `collectAllItems` indirectly). A dedicated unit test for `collectAllItems` / `collectAllContainers` can be added to `ViewSupporterTest` if one exists, but is not required.

---

## Files Changed

| File | Change |
|---|---|
| `ViewSupporter.java` | Add `collectAllItems()`, `collectAllContainers()` |
| `AbstractSingleItemActionEditor.java` | New class |
| `WearActionEditor.java` | Refactor to extend abstract base |
| `TakeActionEditor.java` | Refactor to extend abstract base |
| `DropActionEditor.java` | Refactor to extend abstract base |
| `DestroyActionEditor.java` | Refactor to extend abstract base |
| `RemoveActionEditor.java` | Refactor to extend abstract base |
| `CreateActionEditor.java` | Replace private helpers with `ViewSupporter` calls |
| `MoveItemActionEditor.java` | Replace private helpers with `ViewSupporter` calls |
