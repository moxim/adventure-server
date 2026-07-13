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
        ├── AdventuresMainLayout — used by adventure / admin / about / dashboards
        ├── LocationsMainLayout  — used by location / map editors
        ├── ItemsMainLayout      — used by item editors
        ├── DirectionsMainLayout — used by direction editors
        ├── CommandMainLayout    — alternative for commands menu (see route table)
        ├── MessagesMainLayout   — used by message editors
        └── VocabularyMainLayout — used by vocabulary editors
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
| `author/adventures/:adventureId/locations/:locationId/commands/:commandId/edit` | `CommandEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/locations/:locationId/commands/new` | `CommandEditorView` | `LocationsMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/messages` | `MessagesMenuView` | `MessagesMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/messages/:messageId/edit` | `MessageEditorView` | `MessagesMainLayout` | `ROLE_AUTHOR` |
| ↳ alias `author/adventures/:adventureId/messages/new` | `MessageEditorView` | `MessagesMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/vocabulary` | `VocabularyMenuView` | `VocabularyMainLayout` | `ROLE_AUTHOR` |
| `author/adventures/:adventureId/vocabulary/special` | `SpecialWordsView` | `VocabularyMainLayout` | `ROLE_AUTHOR` |
| `player/library` | `PlayerLibraryView` | `AdventuresMainLayout` | `ROLE_PLAYER` |

The role hierarchy (`ROLE_ADMIN > ROLE_AUTHOR > ROLE_PLAYER`) means an admin
can reach every route, an author every author + player route, and a player
only player routes.

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

### Grid inline editing (`GridUnbufferedInlineEditor`)

Used by `CommandsMenuView` to edit a command-description row in place
without opening a dialog. Provides a small UX win for high-volume tasks like
editing many commands at once.

### Action editor factory

`view/command/action/` contains:

| Class | Role |
|-------|------|
| `ActionEditorComponent` | Abstract base for all per-action sub-editors. |
| `AbstractSingleItemActionEditor<T extends ActionData>` | Generic abstract mid-layer for the 8 editors that need one `ItemData` selector (title, description, label, placeholder, error text customised per subclass). |
| `ActionSelector` | A combo-box of supported `Action` kinds. Picking one swaps in the matching editor. |
| `ActionEditorFactory` | Java `switch` pattern-match over `ActionData`; covers all 15 authorable action types. |
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

### Condition editor factory

`view/command/condition/` contains:

| Class | Role |
|-------|------|
| `ConditionEditorComponent` | Abstract base for all per-condition sub-editors. |
| `AbstractSingleItemConditionEditor` | Abstract mid-layer for the 3 item-presence conditions (Carried / Here / Worn) that share one `ItemData` selector. |
| `AbstractNumericComparisonConditionEditor` | Abstract mid-layer for the 2 numeric-comparison conditions (GreaterThan / LowerThan) that share a variable-name field and a numeric value field. |
| `ConditionSelector` | A combo-box of supported `PreCondition` kinds. |
| `ConditionEditorFactory` | Java `switch` pattern-match over `PreConditionData`; covers 9 of the 10 condition types (see Known gaps). |
| `CarriedConditionEditor` | Item selector (via `AbstractSingleItemConditionEditor`). |
| `HereConditionEditor` | Item selector (via `AbstractSingleItemConditionEditor`). |
| `WornConditionEditor` | Item selector (via `AbstractSingleItemConditionEditor`). |
| `EqualsConditionEditor` | Variable name + value text fields. |
| `GreaterThanConditionEditor` | Variable name + numeric threshold (via `AbstractNumericComparisonConditionEditor`). |
| `LowerThanConditionEditor` | Variable name + numeric threshold (via `AbstractNumericComparisonConditionEditor`). |
| `SameConditionEditor` | Two variable name text fields. |
| `PlayerAtConditionEditor` | Location selector. |
| `ItemAtConditionEditor` | Item selector + location selector. |

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
- `src/main/java/com/pdg/adventure/view/adventure/{AdventuresMainLayout,AdventuresMenuView,AdventureEditorView}.java`
- `src/main/java/com/pdg/adventure/view/location/{LocationsMainLayout,LocationsMenuView,LocationEditorView,LocationMapView,LocationViewModel,LocationDescriptionAdapter,LocationProvider,LocationUsageTracker}.java`
- `src/main/java/com/pdg/adventure/view/item/*.java`
- `src/main/java/com/pdg/adventure/view/direction/*.java`
- `src/main/java/com/pdg/adventure/view/command/*.java` (and `command/action/`)
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
- **Player play surface missing.** `PlayerLibraryView` is a stub heading;
  there is no `*PlayView` driving `GameLoop`. See
  [`02-functional-requirements.md` § C2](02-functional-requirements.md#c2-play-an-adventure-target-state).
- **`NotConditionData` has no condition editor.** `ConditionEditorFactory`
  covers 9 of the 10 condition types; `NotConditionData` is not yet surfaced
  in the authoring UI. A rebuild should add `NotConditionEditor` and wire it
  into `ConditionEditorFactory`.
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
