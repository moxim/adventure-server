# 07 — UI and Navigation

## Purpose

This chapter documents the **Vaadin view tree**: every routed page, the
layout each one lives inside, the role required to reach it, the reusable
components that authoring views compose, and the conventions for
view-models, validation, and navigation. The product capabilities exposed
by the UI are described in
[`02-functional-requirements.md`](02-functional-requirements.md); the
authentication wiring is in
[`06-security-and-access-control.md`](06-security-and-access-control.md).

## Top-level shape

The application is a **single-page Vaadin Flow app** rendered by Spring Boot.
There is one top-level `AppShellConfigurator` (`AdventureBuilderServer`),
one root entry point (`RootView`), and a small family of `AppLayout`
subclasses that frame each functional area with a header and drawer.

```
AppShell: AdventureBuilderServer (@PWA Adventure Builder)
   ├── RootView ("/", anonymous) — dispatcher
   ├── LoginView ("/login", anonymous) — login form
   ├── LogoutView ("/logout", any authenticated)
   ├── AboutView ("/about", anonymous)
   │
   └── AdventureAppLayout (base @PermitAll)
        ├── AdventuresMainLayout — used by adventure / admin / about / dashboards,
        │                          the Player library, AdventureRunView (Test/Run),
        │                          and SystemMessagesView
        ├── LocationsMainLayout  — used by location / map editors
        ├── ItemsMainLayout      — used by item editors
        ├── DirectionsMainLayout — used by direction editors
        ├── CommandMainLayout    — alternative for commands menu (see route table)
        ├── MessagesMainLayout   — used by message editors
        ├── VocabularyMainLayout — used by vocabulary editors
        └── WorkflowMainLayout   — used by WorkflowEditorView and ResponsesEditorView
```

The drawer always carries: **About** (all users), **Dashboard** (ADMIN /
AUTHOR — links to `AdminDashboardView` or `AuthorDashboardView`), **Logout**
(all). Sub-layouts add a section icon and image to brand the area.

The layout JS shim in `AdventureAppLayout.createDrawer` highlights the active
drawer entry by matching `window.location.pathname` against every
`vaadin-side-nav a` element on each `vaadin-router-location-changed` event;
this is a workaround for the lack of an out-of-the-box `aria-current` mark in
Vaadin's `SideNav` at the time of writing.

## Route × role matrix

This is the canonical list of routed views with their `@Route` value, layout,
and role gate. Routes containing `:adventureId`, `:locationId`, etc. use
Vaadin's `@RouteParameter` matching; the parameter names are centralised in
`view/support/RouteIds`. `@RouteAlias` entries are listed underneath their
primary route.

| Route | View | Layout | Role |
|-------|------|--------|------|
| `""` | `RootView` | none | anonymous (dispatcher) |
| `login` | `LoginView` | `autoLayout=false` | anonymous |
| `logout` | `LogoutView` | none | `@PermitAll` |
| `about` | `AboutView` | `AdventuresMainLayout` | anonymous |
| `admin/dashboard` | `AdminDashboardView` | `AdventuresMainLayout` | `ROLE_ADMIN` |
| `admin/users` | `UserManagementView` | `AdventuresMainLayout` | `ROLE_ADMIN` |
| `admin/adventures/assignments` | `AdventureAssignmentView` | `AdventuresMainLayout` | `ROLE_ADMIN` |
| `author/dashboard` | `AuthorDashboardView` | `AdventuresMainLayout` | `ROLE_AUTHOR` |
| `author/adventures` | `AdventuresMenuView` | `AdventuresMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/edit` | `AdventureEditorView` | `AdventuresMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/new` | `AdventureEditorView` | `AdventuresMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/test` | `AdventureRunView` | `AdventuresMainLayout` | `ROLE_AUTHOR`, `ROLE_PLAYER` |
| ↳ alias `player/library/:adventureId/run` | `AdventureRunView` | `AdventuresMainLayout` | `ROLE_AUTHOR`, `ROLE_PLAYER` |
| `author/adventures/:adventureId/workflow` | `WorkflowEditorView` (Processes) | `WorkflowMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/responses` | `ResponsesEditorView` | `WorkflowMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/system-messages` | `SystemMessagesView` | `AdventuresMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations` | `LocationsMenuView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/locations` | `LocationsMenuView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations/:locationId/edit` | `LocationEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/new` | `LocationEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| `author/map` | `LocationMapView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/items` | `AllItemsMenuView` | `ItemsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations/:locationId/items` | `ItemsMenuView` | `ItemsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations/:locationId/items/:itemId/edit` | `ItemEditorView` | `ItemsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/:locationId/items/new` | `ItemEditorView` | `ItemsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations/:locationId/directions` | `DirectionsMenuView` | `DirectionsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations/:locationId/direction/:directionId/edit` | `DirectionEditorView` | `DirectionsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/:locationId/direction/new` | `DirectionEditorView` | `DirectionsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations/:locationId/commands` | `CommandsMenuView` | `AdventuresMainLayout` (note: not `CommandMainLayout` — see Known gaps) | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/:locationId/items/:itemId/commands` | `CommandsMenuView` | `AdventuresMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/locations/:locationId/commands/:commandId/edit` | `CommandEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/:locationId/commands/new` | `CommandEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/:locationId/items/:itemId/commands/:commandId/edit` | `CommandEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/:locationId/items/:itemId/commands/new` | `CommandEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/messages` | `MessagesMenuView` | `MessagesMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/messages/:messageId/edit` | `MessageEditorView` | `MessagesMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/messages/new` | `MessageEditorView` | `MessagesMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/vocabulary` | `VocabularyMenuView` | `VocabularyMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/vocabulary/special` | `SpecialWordsView` | `VocabularyMainLayout` | `ROLE_AUTHOR` |
| `player/library` | `PlayerLibraryView` | `AdventuresMainLayout` | `ROLE_PLAYER` |

The role hierarchy (`ROLE_ADMIN > ROLE_AUTHOR > ROLE_PLAYER`) means an admin
can reach every route, an author every author + player route, and a player
only player routes.

**`AdventureRunView`'s three logical origins, two physical routes.** The
table above shows its `@Route` (`…/test`) and `@RouteAlias` (`player/library/:id/run`),
but a third origin — `AdventuresMenuView`'s "Run Adventure" button — reuses
the *first* route template with a `?from=menu` query parameter instead of a
third mapping,
because Vaadin's outbound URL generation for class-based `navigate(...)`
calls can't disambiguate between two templates with an identically-shaped
`:adventureId` parameter (it always resolves to the primary `@Route`).
Callers navigate via literal path strings from the static
`AdventureRunView.editorTestPath(id)` / `.menuRunPath(id)` /
`.libraryRunPath(id)` helpers instead of `navigate(AdventureRunView.class, …)`
— incoming route *matching* does correctly consider aliases, so this only
affects outbound link generation. The view's own `beforeEnter` resolves
which of the three origins sent it here (from the path prefix and the
query parameter) and uses that to decide both its `HasDynamicTitle` value
(`"Test: …"` for the two author origins, `"Playing: …"` for the library
origin) and where its **Back** button returns to.

## Layouts

All authoring layouts extend `AdventureAppLayout` (`view/component/`), which
is the base `@PermitAll` AppLayout. Common behaviour:

- `createHeader(title)` — adds a `DrawerToggle`, the brand image
  (`images/adventure.png`, 30 px), and an `H2` title.
- `createDrawer(name)` — adds an `H1` brand and a `SideNav` with the standard
  *About / Dashboard / Logout* entries.
- `extendDrawer(...)` — hook for sub-layouts to add their own `SideNavItem`s.
- `afterNavigation(...)` — picks up the routed view's title (either via
  `@PageTitle` or `HasDynamicTitle.getPageTitle()`) and writes it into the
  header.

Each functional area has a thin sub-layout that brands the drawer with a
themed image:

| Sub-layout | Brand image | Extra nav |
|------------|-------------|-----------|
| `AdventuresMainLayout` | `images/adventure.png` | (Dashboard / About / Logout only) |
| `LocationsMainLayout` | `icons/maps.gif` | "The World" → `LocationMapView` |
| `ItemsMainLayout` | `icons/treasure.gif` | (none) |
| `DirectionsMainLayout` | `icons/path.gif` | (none) |
| `CommandMainLayout` | `icons/to-do-list.gif` | (none) — note: `CommandsMenuView` currently uses `AdventuresMainLayout` instead |
| `MessagesMainLayout` | `icons/scroll-with-quill.gif` | (none) |
| `VocabularyMainLayout` | `icons/grammar.gif` | (none) |
| `WorkflowMainLayout` | `icons/to-do-list.gif` | (none) — used by `WorkflowEditorView` (Processes) and `ResponsesEditorView` |

`AdventuresMainLayout.checkIfUserWantsToLeavePage(event, hasChanges)` is the
shared unsaved-change guard called by every editor's `beforeLeave(...)`.

## Editor template: `BaseEditorView<T>`

Every authoring editor extends `BaseEditorView<T>` (in `view/component/`):

```java
public abstract class BaseEditorView<T> extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver, BeforeLeaveObserver {

    protected final transient AdventureService adventureService;
    protected final Binder<T> binder;
    protected final ResetBackSaveView navigationButtons;
    protected String pageTitle;
    protected AdventureData adventureData;
    protected String adventureId;
    protected String locationId;

    protected abstract void navigateBack();
    protected abstract void save();
    protected abstract String getDefaultPageTitle();
}
```

Behaviour the template enforces:

1. **`BeanValidationBinder<T>`** — every editor binds to its `*ViewModel` or
   `*Data` with bean-validation enabled. Field constraints come from
   `jakarta.validation` annotations on the view model.
2. **Button wiring** — `BACK` calls `navigateBack()`, `SAVE` calls `save()`,
   `RESET` calls `binder.readBean(binder.getBean())`, `CANCEL` is bound to
   `Key.ESCAPE` and triggers reset+back via `ResetBackSaveView`.
3. **Save/reset enabled state** — listens to `binder.addStatusChangeListener`
   so SAVE is enabled only when there are valid changes, and RESET only when
   there are changes.
4. **Route parameters** — `beforeEnter` reads `RouteIds.ADVENTURE_ID`,
   `RouteIds.LOCATION_ID` (defaulting to `"new"` when absent).
5. **Unsaved-change guard** — `beforeLeave` calls
   `AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges())`,
   which posts a `ConfirmDialog` if there are pending changes.

`ResetBackSaveView` (`view/component/`) is a `Composite<HorizontalLayout>`
containing the four buttons in fixed order: **Cancel, Reset, Back, Save**.
The `Cancel` button's listener invokes `reset.clickInClient()` followed by
`back.clickInClient()` so the side effects run on the client side and the
unsaved-change dialog only fires once.

## Reusable components

`view/component/`:

| Class | Role |
|-------|------|
| `AdventureAppLayout` | Base layout. Adds the standard header + drawer; auto-updates the title on navigation. |
| `BaseEditorView<T>` | Editor template (above). |
| `ResetBackSaveView` | Standard Cancel / Reset / Back / Save button bar. |
| `VocabularyPicker` | `ComboBox<Word>` extension. Configurable populate from a vocabulary; renders `Word.text`. |
| `VocabularyPickerField` | Convenience subclass with `(label)`, `(label, tooltip)`, and `(label, tooltip, type, vocabulary)` constructors that populate non-synonym words for a given `Word.Type`. |
| `AdventureGrid` / `GridFactory` | Shared grid set-up so every menu view has consistent column widths and selection behaviour. |
| `NavigationHelper` | Static utilities for assembling typed `RouteParameters` and routing back from an editor. |

`view/support/`:

| Class | Role |
|-------|------|
| `ViewSupporter` | Cross-cutting helpers: current user lookup (`getCurrentUser` — throws if no security context), id formatter (truncates ULIDs to 26 chars), location/description/word formatters used by grids, two-way `Binder` wiring helpers for vocabulary pickers, the standard `getConfirmDialog()`, and `setSize(grid)` defaults (`1024 px` max width, `640 px` max height). Aggregate collection helpers: `collectAllItems(AdventureData)`, `collectAllContainers(AdventureData)`, `collectAllLocations(AdventureData)` — gather items / containers / locations across all locations for multi-location pickers. Constants: `MAX_TEXT_IN_GRID = 32`, `MAX_ID_LENGTH = 26`. |
| `RouteIds` | Enum mapping logical route parameter names → string keys: `ADVENTURE_ID`, `LOCATION_ID`, `COMMAND_ID`, `DIRECTION_ID`, `MESSAGE_ID`, `ITEM_ID`. |
| `GridProvider` | Lazy-loading data-provider helpers for grids. |
| `TrackedUsage` | Interface for usage trackers (see below). |

## Usage trackers

Authoring requires showing where a vocabulary word, location, item, or
message is referenced — both for context and to refuse deletion when in use.
The pattern is uniform: a `*UsageTracker` aggregates references across the
adventure and surfaces them via `ViewSupporter.showUsages(...)`.

| Tracker | Tracks |
|---------|--------|
| `WordUsageTracker` (`view/vocabulary/`) | Commands, item descriptions, location descriptions, direction descriptions referencing a word. |
| `LocationUsageTracker` (`view/location/`) | Directions targeting a location; commands using `MovePlayerAction` to it. |
| `ItemUsageTracker` (`view/item/`) | Commands referencing an item by id. |
| `MessageUsageTracker` (`view/message/`) | `MessageAction`s referencing a message by id. |

A delete attempt that finds a non-empty usage list MUST refuse the delete
and present the usages in a `Dialog`.

## View model pattern

Authoring views bind to a *view model* — a small POJO matching the form's
shape — rather than to the raw `*Data` document. Existing view models:

| ViewModel | View |
|-----------|------|
| `LocationViewModel` | `LocationEditorView` |
| `ItemViewModel` | `ItemEditorView` |
| `DirectionViewModel` | `DirectionEditorView` |
| `CommandViewModel` | `CommandEditorView` |
| `MessageViewModel` | `MessageEditorView` |

Each view model exposes only the fields the form binds to. The editor's
`save()` translates `viewModel → *Data` (or directly mutates the bound
`*Data`) and calls the appropriate service.

## Adapters

Some Vaadin components (`ComboBox`, `Grid` row renderers) need a flat
"display" object rather than a deeply nested `*Data`. Adapters in each domain
folder bridge the gap:

| Adapter | Purpose |
|---------|---------|
| `LocationDescriptionAdapter` | Flattens `LocationData` for grid rendering. |
| `DirectionDescriptionAdapter` | Same for directions. |
| `CommandDescriptionAdapter` | Flattens `CommandDescriptionData` for inline grid editing. |
| `MessageDescriptionAdapter` | Same for messages. |
| `DescribableWordAdapter` | Bridges `Word` for `Describable`-aware components. |
| `ItemLocationPair` / `ItemLocationPairAdapter` | Used by `AllItemsMenuView` to display "item @ location" rows. |

## Validation feedback

The contract from [`02-functional-requirements.md` § Validation feedback](02-functional-requirements.md#validation-feedback) is:

| Severity | Mechanism | Implementation pointer |
|----------|-----------|-------------------------|
| Field-level constraint (required, format, length) | `Binder` inline error | `BeanValidationBinder<T>` + `jakarta.validation` annotations on the view model |
| Operation success (save, assign, delete) | `Notification` toast | `NotificationVariant.LUMO_SUCCESS`, 2000 ms, `BOTTOM_START` |
| Operation failure (error, conflict) | `Notification` toast | `NotificationVariant.LUMO_ERROR`, 5000 ms, `MIDDLE` |
| Destructive or blocking action | `ConfirmDialog` | `ViewSupporter.getConfirmDialog()` |
| In-use deletion refused | `Dialog` listing usages | `*UsageTracker.show(...)` |

## Special components

### `VocabularyPickerField` and the special-words editor

`SpecialWordsView` (`view/vocabulary/`) edits the special-word slots on
`VocabularyData` (take, drop, examine, look, inventory, go, help, quit,
save, load). Each slot is a `VocabularyPickerField`; when the user picks a
word, the listener:

1. Returns early if `event.isFromClient() == false` — programmatic
   `setValue` MUST NOT trigger the model update path (prevents recursion
   when the form is repopulated after save).
2. Calls `checkIfValueAlreadyExists(oldValue, newValue, type, selector)` —
   refuses a pick that duplicates an existing assignment.
3. Calls the typed setter (`vocabularyData.setExamineWord(word)` etc.).

This pattern is documented because it appears in test code and tripped up
the browserless-test setup; see
[`08-build-test-and-ops.md`](08-build-test-and-ops.md#known-limitations-combobox-in-browserless).

### `WordEditorDialogue` synonym cascade

`WordEditorDialogue` (`view/vocabulary/`) opens as a `Dialog` when creating or
editing a word. When the author picks a new synonym for the word and saves,
the dialogue:

1. Calls `VocabularyData.findWordsBySynonym(oldSynonym)` to find every word
   that still points to the old synonym.
2. If any such words exist, opens a confirmation `Dialog` listing them and
   offering **Update All** (reroute all to the new synonym) or **Skip**
   (leave them pointing to the old one).
3. Emits a type-mutation warning when synonym adoption would change a word's
   `Word.Type` (because a word's type is inherited from its synonym).

This cascade is `VocabularyData.findWordsBySynonym`'s primary call site.

### `CommandsMenuView` grid

`CommandsMenuView` shows a **read-only** summary grid of the scope's commands
(verb / adjective / noun / first precondition / first action). Editing is via
double-click or a right-click **Edit** into `CommandEditorView`; right-click
**Delete** removes one chain variant. (The former in-place
`GridUnbufferedInlineEditor` / `SimpleCommandDescription` spec-string editors
were deleted — commands are now identified by a stable chain id, not a
derived spec string.)

### Workflow editors: `CommandListEditorView`

`view/workflow/CommandListEditorView` is a single concrete, constructor-
parameterized class — **not** a `BaseEditorView` subclass — providing a
grid-of-commands + single-command editor with a **Back / New / Delete /
Save** button set (Delete prompts a `ConfirmDialog`). Two thin subclasses
differ only in fixed data (label, page-title prefix, help text, empty-state
text, `CommandListType`, and which `WorkflowData` list they read/write):

| Subclass | `@Route` | Edits | Verb required? |
|----------|----------|-------|----------------|
| `WorkflowEditorView` | `…/workflow` | `WorkflowData.commands` (Processes) | no |
| `ResponsesEditorView` | `…/responses` | `WorkflowData.interceptorCommands` (Responses) | yes |

Both reuse `PreconditionActionEditor` / `PreconditionActionFormatter` from
`view/command/`.

### System-messages editor: `SystemMessagesView`

`view/systemmessage/SystemMessagesView` (`…/system-messages`,
`AdventuresMainLayout`) renders the fixed `SystemMessageKey` catalog as a
grid (Key / Current Text) synthesized from the enum plus this adventure's
sparse `SystemMessageData` overrides. Double-click opens an edit `Dialog`
(`ModalityMode.STRICT`) showing the English original, the translator
description + source location, and an editable text area. Save validates
placeholder parity via `PlaceholderSpec` and writes an override row on first
edit only. No create / delete / rename. `SystemMessageEntry` is the
grid/dialog read-model record.

### Action editor factory

`view/command/action/` contains:

| Class | Role |
|-------|------|
| `ActionEditorComponent` | Abstract base for all per-action sub-editors. |
| `AbstractSingleItemActionEditor<T extends ActionData>` | Generic abstract mid-layer for the 8 editors that need one `ItemData` selector (title, description, label, placeholder, error text customised per subclass). |
| `ActionSelector` | A combo-box of supported `Action` kinds. Picking one swaps in the matching editor. |
| `ActionEditorFactory` | `createEditor(ActionData, AdventureData)` — the stable entry point every call site uses. Delegates lookup to `ActionEditorRegistry` (package-private); covers all 16 authorable action types. |
| `ActionEditorRegistry` | One-time classpath scan (`ClassPathScanningCandidateComponentProvider`) for `@AutoRegisterActionEditor`-annotated `ActionEditorComponent`s, keyed by the `ActionData` subtype resolved from each editor's generic type argument. Replaced a hand-maintained `switch` statement — adding a new action editor means writing the class and annotating it, not touching the factory. Reflectively picks a `(ActionData)` or `(ActionData, AdventureData)` constructor to instantiate. |
| `MessageActionEditor` | Inline text field for the message body. |
| `MoveItemActionEditor` | Item selector (uses `ViewSupporter.collectAllItems`). |
| `MovePlayerActionEditor` | Location selector (uses `ViewSupporter.collectAllLocations`). |
| `WearActionEditor` | Item selector (wearable items only, via `AbstractSingleItemActionEditor`). |
| `TakeActionEditor` | Item selector (via `AbstractSingleItemActionEditor`). |
| `DropActionEditor` | Item selector (via `AbstractSingleItemActionEditor`). |
| `RemoveActionEditor` | Item selector (via `AbstractSingleItemActionEditor`). |
| `DestroyActionEditor` | Item selector (via `AbstractSingleItemActionEditor`). |
| `CreateActionEditor` | Container selector (via `AbstractSingleItemActionEditor`, uses `collectAllContainers`). |
| `DescribeActionEditor` | No extra input (describe current location). |
| `InventoryActionEditor` | No extra input (list pocket). |
| `QuitActionEditor` | No extra input. |
| `IncrementVariableActionEditor` | Variable name text field. |
| `DecrementVariableActionEditor` | Variable name text field. |
| `SetVariableActionEditor` | Variable name + value text fields. |
| `BreakActionEditor` | No extra input (stops command chain execution immediately). |

### Condition editor factory

`view/command/condition/` contains:

| Class | Role |
|-------|------|
| `ConditionEditorComponent` | Abstract base for all per-condition sub-editors. |
| `AbstractSingleItemConditionEditor` | Abstract mid-layer for the 3 item-presence conditions (Carried / Here / Worn) that share one `ItemData` selector. |
| `AbstractNumericComparisonConditionEditor` | Abstract mid-layer for the 2 numeric-comparison conditions (GreaterThan / LowerThan) that share a variable-name field and a numeric value field. |
| `ConditionSelector` | A combo-box of supported `PreCondition` kinds — 10 entries; `NotCondition` is not among them (see below). |
| `ConditionEditorFactory` | Entry point for condition editors, mirroring `ActionEditorFactory`'s shape. Delegates to `ConditionEditorRegistry`; covers all 10 selectable condition types. |
| `ConditionEditorRegistry` | Classpath scan for `@AutoRegisterConditionEditor`-annotated `ConditionEditorComponent`s, the condition-side twin of `ActionEditorRegistry` above — same replaced-the-switch-statement story. |
| `CarriedConditionEditor` | Item selector (via `AbstractSingleItemConditionEditor`). |
| `HereConditionEditor` | Item selector (via `AbstractSingleItemConditionEditor`). |
| `WornConditionEditor` | Item selector (via `AbstractSingleItemConditionEditor`). |
| `ChanceConditionEditor` | A single numeric field for the 1–100 chance percentage. |
| `EqualsConditionEditor` | Variable name + value text fields. |
| `GreaterThanConditionEditor` | Variable name + numeric threshold (via `AbstractNumericComparisonConditionEditor`). |
| `LowerThanConditionEditor` | Variable name + numeric threshold (via `AbstractNumericComparisonConditionEditor`). |
| `SameConditionEditor` | Two variable name text fields. |
| `PlayerAtConditionEditor` | Location selector. |
| `ItemAtConditionEditor` | Item selector + location selector. |

**There is no `NotConditionEditor`, by design.** `NotConditionData` is
applied structurally rather than picked as a kind: `ConditionRow`
(`view/command/condition/ConditionRow.java`) renders every condition row
with a **Negate** checkbox alongside whichever of the 10 kinds above was
chosen, and `ConditionRow.toConditionData()` wraps the underlying
`PreConditionData` in a `NotConditionData` when it's checked. This is a
structural choice, not a coverage gap — see
[`04-runtime-engine.md` § Composites](04-runtime-engine.md#composites).

## PWA configuration

`AdventureBuilderServer` is annotated `@PWA(name = "Adventure Builder",
shortName = "Adventure", offlineResources = {"./images/adventure.png"},
offlinePath = "offline.html")`. The web manifest, service worker, offline
HTML, and adventure logo are bundled into the production frontend by
`vaadin-maven-plugin`. The login page works offline only insofar as the
manifest resources are cached; live API access requires the server to be
reachable.

## Theme

The application uses Vaadin's **Lumo** theme. `AdventureAppLayout` carries
`@StyleSheet(Lumo.STYLESHEET)` and applies the **dark** lumo variant on the
header. There is no custom CSS theme today; visual customisation is done by
swapping the brand image per layout and using `LumoUtility` classes.

## Source pointers

- `src/main/java/com/pdg/adventure/AdventureBuilderServer.java`
- `src/main/java/com/pdg/adventure/view/RootView.java`
- `src/main/java/com/pdg/adventure/view/login/{LoginView,LogoutView}.java`
- `src/main/java/com/pdg/adventure/view/about/AboutView.java`
- `src/main/java/com/pdg/adventure/view/component/`
  — `AdventureAppLayout`, `BaseEditorView`, `ResetBackSaveView`, `VocabularyPicker`, `VocabularyPickerField`, `AdventureGrid`, `GridFactory`, `NavigationHelper`.
- `src/main/java/com/pdg/adventure/view/support/`
  — `ViewSupporter`, `RouteIds`, `GridProvider`, `TrackedUsage`.
- `src/main/java/com/pdg/adventure/view/admin/{AdminDashboardView,AdventureAssignmentView,UserManagementView}.java`
- `src/main/java/com/pdg/adventure/view/author/AuthorDashboardView.java`
- `src/main/java/com/pdg/adventure/view/player/PlayerLibraryView.java`
- `src/main/java/com/pdg/adventure/view/adventure/{AdventuresMainLayout,AdventuresMenuView,AdventureEditorView,AdventureRunView}.java`
- `src/main/java/com/pdg/adventure/view/workflow/{WorkflowMainLayout,CommandListEditorView,WorkflowEditorView,ResponsesEditorView}.java`
- `src/main/java/com/pdg/adventure/view/systemmessage/{SystemMessagesView,SystemMessageEntry}.java`
- `src/main/java/com/pdg/adventure/view/location/{LocationsMainLayout,LocationsMenuView,LocationEditorView,LocationMapView,LocationViewModel,LocationDescriptionAdapter,LocationProvider,LocationUsageTracker}.java`
- `src/main/java/com/pdg/adventure/view/item/*.java`
- `src/main/java/com/pdg/adventure/view/direction/*.java`
- `src/main/java/com/pdg/adventure/view/command/*.java` (and `command/action/`, `command/condition/`)
- `src/main/java/com/pdg/adventure/view/message/*.java`
- `src/main/java/com/pdg/adventure/view/vocabulary/*.java`
- `src/main/resources/META-INF/resources/{images,icons}/` — assets.
- `src/main/resources/META-INF/resources/offline.html` — PWA fallback.

## Known gaps

- **`CommandsMenuView` uses `AdventuresMainLayout` instead of `CommandMainLayout`.**
  Verified in source (`view/command/CommandsMenuView.java`); a rebuild
  should standardise the layout choice.
- **`VocabularyMenuView` has a commented-out `@RouteAlias`**
  (`adventures/vocabulary`). Decide whether to keep the alias for
  bookmark-friendly URLs and re-enable it, or remove the dead annotation.
- **`LocationMapView` is a non-functional placeholder.** It renders a
  static `islandMap.jpg` via `ImageMap` with a hardcoded 100×100px click
  grid (`for (x = 0; x < 2451; x += 100) for (y = 0; y < 2628; y += 100) …`)
  that just pops a `"Location X : Y"` notification on click — it is not
  bound to `LocationData` at all. A rebuild should either wire a real
  node-graph view of the adventure's actual locations/exits, or drop the
  drawer link until it is.
- **Two deletion paths skip confirmation.** Deleting an adventure
  (`AdventuresMenuView`'s context menu) and deleting an exit
  (`DirectionsMenuView`'s context menu) both remove the row immediately
  with no `ConfirmDialog`, unlike every other delete path in the app
  (locations, items, words, messages, workflow commands all confirm
  first). Confirm this is intentional or bring them in line with
  [§ Validation feedback](#validation-feedback) below.
- **`SpecialWordsView` browserless test workarounds.** Two ComboBox quirks
  (silent `setValue`, wrong scope on `$()` queries) are documented in the
  testing strategy; until the upstream fix lands, browserless tests for
  combo-driven views need the reflection-based event-bus shim
  ([`08-build-test-and-ops.md`](08-build-test-and-ops.md#known-limitations-combobox-in-browserless)).
- **Active-link drawer highlighting is JS-driven.** `AdventureAppLayout`
  injects a small JavaScript snippet to mark the active drawer entry.
  Replace with native Vaadin `aria-current` once available.
- **Vaadin `MainLayout`-style top-level layout.** `AGENTS-conventions.md`
  mentions `MainLayout`/`SidebarLayout` as a possible class name; the
  current code names them `*MainLayout` per area. Prefer the `*MainLayout`
  pattern in a rebuild and retire the older naming.
