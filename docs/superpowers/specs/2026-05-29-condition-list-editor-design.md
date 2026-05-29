# Condition List Editor Design

## Goal

Add a functional precondition editor inside `PreconditionActionEditor` that lets users add, edit, remove, and negate leaf preconditions on a command. The UI uses an inline-expand (accordion) layout — each condition is a collapsible row; clicking a row expands it to reveal its editor form.

## Scope

- 9 leaf condition types: Carried, Here, Worn, PlayerAt, ItemAt, Equals, GreaterThan, LowerThan, Same
- Negate toggle on each condition (wraps/unwraps `NotConditionData`)
- No And/Or composite editing — out of scope for this iteration
- Lives inside the existing `PreconditionActionEditor` component

## Architecture

New package: `src/main/java/com/pdg/adventure/view/command/condition/`

Mirrors the existing `view/command/action/` pattern exactly.

### Components

**`ConditionListEditor`**
Top-level component embedded in `PreconditionActionEditor`. Owns an ordered `VerticalLayout` of `ConditionRow` instances and a `ConditionSelector` at the bottom. Exposes:
- `setConditions(List<PreConditionData>)` — loads from model, unwrapping any `NotConditionData`
- `getConditions()` — serialises back, re-wrapping negated conditions in `NotConditionData`

**`ConditionSelector`**
Mirrors `ActionSelector`. A `ComboBox<ConditionTypeDescriptor>` listing the 9 leaf types + an "Add" button. On click, instantiates a fresh leaf `PreConditionData` via the descriptor factory, then notifies `ConditionListEditor` via a listener callback.

**`ConditionRow`**
Wraps one leaf condition in a Vaadin `Details` component. The summary header shows `[✓/✗] TypeName: brief summary` and contains a negate `Checkbox` and a "Remove" `Button`. The content panel holds the `ConditionEditorComponent` for this condition. Exposes:
- `toConditionData()` — returns `NotConditionData(leafData)` if negate checked, otherwise the raw leaf data

**`ConditionEditorComponent`**
Abstract base (mirrors `ActionEditorComponent`). Constructor receives the leaf `PreConditionData`; UI is built lazily via `initialize()` → `buildUI()`. Abstract methods:
- `buildUI()` — builds the type-specific form
- `validate()` — returns true if all required fields are filled
- `getConditionSummary()` — short string for the row header (e.g. `"sword"`, `"score = 10"`)

**`ConditionEditorFactory`**
Static factory with a switch expression on `PreConditionData` subtype → creates and initializes the correct `ConditionEditorComponent`. Throws `UnsupportedOperationException` for unknown types.

**9 concrete editors**

| Class | Data class | Form fields |
|---|---|---|
| `CarriedConditionEditor` | `CarriedConditionData` | `ComboBox<ItemData>` |
| `HereConditionEditor` | `HereConditionData` | `ComboBox<ItemData>` |
| `WornConditionEditor` | `WornConditionData` | `ComboBox<ItemData>` (wearable items only) |
| `PlayerAtConditionEditor` | `PlayerAtConditionData` | `ComboBox<LocationData>` |
| `ItemAtConditionEditor` | `ItemAtConditionData` | `ComboBox<ItemData>` + `ComboBox<LocationData>` |
| `EqualsConditionEditor` | `EqualsConditionData` | `TextField` (variable) + `TextField` (value) |
| `GreaterThanConditionEditor` | `GreaterThanConditionData` | `TextField` (variable) + `NumberField` (value) |
| `LowerThanConditionEditor` | `LowerThanConditionData` | `TextField` (variable) + `NumberField` (value) |
| `SameConditionEditor` | `SameConditionData` | `TextField` (variable 1) + `TextField` (variable 2) |

Item and location pickers are populated from `AdventureData` passed down from `PreconditionActionEditor`. For `WornConditionEditor`, items are filtered to those that implement or are tagged as wearable — determined by checking `ItemData` for the wearable flag (to confirm during implementation).

## Data Flow

### Loading (CommandData → UI)

`PreconditionActionEditor` receives `CommandData` and calls `conditionListEditor.setConditions(commandData.getPreConditions())`. For each entry:
1. If the entry is `NotConditionData`: unwrap the inner `preCondition`, set `negate = true`
2. Otherwise use the entry directly with `negate = false`
3. Call `ConditionEditorFactory.createEditor(leafData, adventureData)`
4. Wrap the editor in a `ConditionRow(editor, negate)`, append to list

### Saving (UI → CommandData)

`conditionListEditor.getConditions()` iterates `ConditionRow` instances:
- `negate = true` → `new NotConditionData(leafData)`
- `negate = false` → raw `leafData`

Result is assigned back to `commandData.setPreConditions(...)`.

## PreconditionActionEditor Changes

- Add `AdventureData` parameter to constructor (currently takes none)
- Remove: `Grid<PreConditionData>`, "Add Precondition" button, empty `addPrecondition()` method
- Add: `ConditionListEditor conditionListEditor` field, instantiated in constructor
- Split the single `Details("Preconditions & Actions", ...)` into two separate `Details` sections: one for preconditions (`ConditionListEditor`), one for actions (existing grid + button)
- Add `setCommand(CommandData)` method that calls `conditionListEditor.setConditions(commandData.getPreConditions())`

## Testing

All tests are plain unit tests — no Spring context, no Vaadin test server. Mock `AdventureData` with Mockito.

**Each `XxxConditionEditor`:** construct with sample data, call `initialize()`, verify:
- Field values match the input data
- Changing a field updates the data object
- `validate()` returns false when required fields are empty, true when filled
- `getConditionSummary()` returns the expected string

**`ConditionEditorFactory`:** one test per type — verify the correct editor subclass is returned and initialized.

**`ConditionRow`:** verify:
- `toConditionData()` returns the leaf data when negate is unchecked
- `toConditionData()` returns `NotConditionData` wrapping the leaf when negate is checked
- Header label updates when negate is toggled

**`ConditionListEditor`:** verify:
- `setConditions()` with a mix of plain and `NotConditionData` entries creates the right rows with correct negate state
- `getConditions()` round-trips: a `NotConditionData` in → unwrapped on load → rewrapped on save
- Adding a condition via `ConditionSelector` appends a new row
- Removing a row removes it from `getConditions()`
