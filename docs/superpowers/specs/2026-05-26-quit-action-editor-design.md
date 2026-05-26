# QuitAction Editor Design

**Date:** 2026-05-26
**Status:** Approved

## Goal

Allow adventure authors to attach a `QuitAction` to any verb|adjective|noun command via the existing `CommandEditorView`. When the player issues such a command, the game terminates. No parameters are needed; any farewell message is handled by a preceding `MessageAction`.

## Approach

Follow the established action pipeline pattern exactly (same as `InventoryActionEditor`). The hardcoded global "quit" interceptor in `CommandFactory` is left untouched; this feature adds an independently author-scoped quit trigger.

## Components

### 1. `QuitActionData` — `model/action/`

No fields. Extends `ActionData`. Lombok `@Data` and `@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)`. `getActionName()` is inherited (returns class simple name). `QuitActionData::new` works as `Supplier<ActionData>` directly.

```java
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class QuitActionData extends ActionData {}
```

### 2. `QuitActionMapper` — `server/mapper/action/`

Annotated `@Service` and `@AutoRegisterMapper`. `mapToDO()` constructs `new QuitAction(adventureData.getMessages())`. `mapToData()` returns `new QuitActionData()`.

### 3. `QuitActionEditor` — `view/command/action/`

No `AdventureData` constructor parameter. `buildUI()` renders a title, a description span, and an info note. `validate()` returns `true` unconditionally.

### 4. Wire-up

| Location | Change |
|---|---|
| `ActionEditorFactory` | Add `case QuitActionData q -> new QuitActionEditor(q);` |
| `ActionSelector` | Add `new ActionTypeDescriptor("Quit", "Terminate the game", QuitActionData::new)` |

### 5. Tests

| Class | Tests |
|---|---|
| `QuitActionEditorTest` | constructor sets actionData; `validate()` returns true; `initialize()` builds UI children |
| `QuitActionMapperTest` | `mapToDO()` returns `QuitAction`; `mapToData()` returns `QuitActionData` |
| `ActionEditorFactoryTest` | factory returns `QuitActionEditor` for `QuitActionData` |

## Data Flow

```
Author picks "Quit" in ActionSelector
  → QuitActionData() created
  → QuitActionEditor displayed (no inputs)
  → Author saves command
  → QuitActionMapper.mapToDO() produces QuitAction
  → QuitAction.execute() throws QuitException
  → GameLoop catches QuitException, exits
```

## Out of Scope

- Migrating the hardcoded "quit" interceptor in `CommandFactory` to be author-editable (separate task).
- Custom quit messages on `QuitActionData` (author uses `MessageAction` instead).
