# Deep-Linkable Menu & Editor Views (UX Audit P1.2) — Design

## Problem

From the UX audit (`docs/ux-audit-2026-07-06.md`, project root, P1 finding #2): menu views for an in-progress adventure (Locations, Exits/Directions, Items, All Items, Commands, Vocabulary, Special Words, Messages) render empty — no grid, no counter — when opened by URL. The app itself sometimes navigates to ID-less alias URLs and otherwise relies on the adventure staying resident in an already-loaded sibling view, so refresh, bookmark, and shared links all break.

The audit's own text claims editor views (`…/edit`) "deep-link fine." Investigation for this design found that's only true for the top-level `AdventureEditorView`. Its five child editors — `LocationEditorView`, `DirectionEditorView`, `ItemEditorView`, `CommandEditorView`, `MessageEditorView` — have the identical bug: their canonical URLs already carry the needed IDs, but `beforeEnter` never uses them to fetch. This design fixes all 13 affected views (confirmed with the project owner, given the shared root cause and shared fix shape make a partial fix pointless):

**Menu views (8):** `LocationsMenuView`, `DirectionsMenuView`, `ItemsMenuView`, `AllItemsMenuView`, `CommandsMenuView`, `VocabularyMenuView`, `SpecialWordsView`, `MessagesMenuView`
**Child editors (5):** `LocationEditorView`, `DirectionEditorView`, `ItemEditorView`, `CommandEditorView`, `MessageEditorView`
**Not routed, needs no direct fix:** `WordEditorDialogue` (a plain `Dialog` reachable only through `VocabularyMenuView`; fixing that view fixes this dialog's reachability transitively)
**Reference implementation, already correct:** `AdventureEditorView` (`/author/adventures/:adventureId/edit`)

## Root Cause

Every affected view's data comes exclusively from a `setData(...)`/`setAdventureData(...)` method, called only as the `.ifPresent(...)` continuation of a live, same-tab `UI.getCurrent().navigate(...)` call made by the *previous* view — a direct Java object handoff between two component instances during one in-app navigation. Vaadin constructs a brand-new instance of the target view for every navigation (in-app or fresh), and `navigate()` completes the entire lifecycle — including `beforeEnter` — *before* returning the `Optional<T>` that the caller's `.ifPresent(...)` acts on. So `setData()` always fires *after* `beforeEnter`, never before or during it. A fresh HTTP load (refresh, bookmark, shared link, new tab) constructs a view with no prior live instance to hand data off from, so nothing ever calls `setData()`, and the view stays empty.

`LocationsMenuView` has a second, compounding bug: it's the only view reached by a literally parameter-free `navigate(LocationsMenuView.class)` call (two call sites, no `RouteParameters`), which Vaadin resolves to its parameter-free `@RouteAlias("author/adventures/locations")` — producing the ID-less URL directly observed in the audit.

`AdventureEditorView` alone avoids this: its `beforeEnter` reads `:adventureId` from `event.getRouteParameters()` and calls `AdventureAccessService.findAdventureById(id, user)` on every navigation, live or fresh — the pattern this design replicates everywhere else.

No session-scoped or UI-scoped holder for "the current adventure" exists anywhere in the codebase today (confirmed by direct search) — the `setData()` mechanism is the only thing that ever populated these views.

## Fix Architecture

### 1. `beforeEnter` becomes the sole population path — no caching

Every affected view's `beforeEnter` resolves everything it needs from route parameters, on every navigation, and populates itself directly — matching `AdventureEditorView` exactly. `setData()`/`setAdventureData()` are deleted, and their callers simplify to plain `navigate(TargetView.class, new RouteParameters(...))` with no `.ifPresent(...)`.

This was a deliberate choice over caching/reusing the in-memory object a caller already holds. The refetch is a MongoDB `findById` on a ULID primary key — an indexed point lookup, negligible at this app's current scale. Always-refetch is correct by construction (fresh load, bookmark, and in-app navigation all behave identically; no staleness is possible), needs no new infrastructure, and is exactly what the audit itself asked for ("restore context from URL params; make ID-full URLs canonical"). **Deferred cost, not a nonexistent one:** `AdventureData`'s location/item/direction/command graph is loaded via eager `@DBRef(lazy = false)`, so if an adventure ever grows large (many locations, each with items/commands), each navigation's refetch cost grows with it. If that's ever measured to matter, a caching layer is a follow-up project, not a prerequisite — YAGNI applies today.

### 2. `AdventureRouteResolver` — new shared resolver

A new class, `com.pdg.adventure.view.support.AdventureRouteResolver`, sitting alongside the existing `RouteIds` enum (not folded into `ViewSupporter`, which is already a large, general-purpose grab-bag — this is a distinct, single responsibility: route parameters → access-checked domain objects). All nested lookups (location/item/direction/command) are in-memory `Map.get`/stream-filter over the already-eagerly-loaded object graph — the same logic today's `setData()` methods already contain, just relocated earlier in the lifecycle and centralized instead of duplicated 13 times.

Each method returns an `Optional`, showing a "not found or access denied" notification (same wording/style as `AdventureEditorView.loadAdventure`'s existing `Notification.show(..., NotificationVariant.LUMO_ERROR)`) on failure. Consistent with the existing `loadAdventure`/`setUpLoading` split, the resolver does **not** itself forward navigation — each view's `beforeEnter` decides where to forward, since the correct target depends on which ID in the chain failed:

```java
Optional<AdventureData> resolveAdventure(BeforeEnterEvent event, AdventureAccessService accessService)
Optional<LocationData>  resolveLocation(AdventureData adventure, BeforeEnterEvent event)
Optional<ItemData>      resolveItem(LocationData location, BeforeEnterEvent event)
Optional<DirectionData> resolveDirection(LocationData location, BeforeEnterEvent event)
Optional<MessageData>   resolveMessage(AdventureData adventure, BeforeEnterEvent event)
Optional<CommandChainData> resolveCommandChain(ThingData thing, BeforeEnterEvent event)
```

`resolveAdventure` uses the access-controlled `AdventureAccessService.findAdventureById(id, user)` (not the unauthenticated `AdventureService.findAdventureById`), matching `AdventureEditorView`'s existing behavior.

### 3. Per-view resolution depth and forward-on-failure target

Each view forwards to the **nearest still-valid parent view** on failure, not unconditionally back to the top-level adventures list — e.g. a bad `:locationId` inside a valid adventure forwards to that adventure's `LocationsMenuView`, not out to `AdventuresMenuView`.

| View | Resolves | Forwards to, on failure |
|---|---|---|
| `LocationsMenuView`, `AllItemsMenuView`, `VocabularyMenuView`, `SpecialWordsView`, `MessagesMenuView` | adventure only | `AdventuresMenuView` |
| `DirectionsMenuView`, `ItemsMenuView`, `LocationEditorView` | adventure + location | `AdventuresMenuView` (bad adventure) or `LocationsMenuView` (bad location) |
| `ItemEditorView`, `DirectionEditorView` | adventure + location + item/direction | cascades: adventure → `AdventuresMenuView`; location → `LocationsMenuView`; item/direction → `ItemsMenuView`/`DirectionsMenuView` |
| `CommandsMenuView`, `CommandEditorView` | adventure + location + optional item + command | same cascade, plus command → `CommandsMenuView` |
| `MessageEditorView` | adventure + message | `AdventuresMenuView` (bad adventure) or `MessagesMenuView` (bad message) |

`:commandId` is a command-specification string (e.g. `"go|north|"`), not a ULID — resolution is a map lookup (`commandProviderData.getAvailableCommands().get(commandId)`) via `resolveCommandChain`, already how `CommandEditorView.populate` and `CommandsMenuView` work today; no model changes needed.

### 4. Cleanup that falls out of this naturally

- Delete `setData()`/`setAdventureData()` from all 13 views (no longer called by anyone) and the `.ifPresent(editor -> editor.setXxx(...))` half of every caller's navigation call site, across `AdventureEditorView` and within the menu/editor views' own row-selection and Back-button navigation.
- Remove `LocationsMenuView`'s parameter-free `@RouteAlias("author/adventures/locations")`; fix its two `navigate(LocationsMenuView.class)` call sites to pass `RouteParameters` like every other "Manage X" button already does.
- Remove genuinely dead code found during investigation: `LocationsMenuView`'s commented-out `beforeEnter` body and unused `loadAdventure(String)` method; `VocabularyMenuView`'s commented-out `@RouteAlias`.

## File Structure

| File | Change |
|---|---|
| `src/main/java/com/pdg/adventure/view/support/AdventureRouteResolver.java` | Create: the six resolver methods described above |
| `src/main/java/com/pdg/adventure/view/location/LocationsMenuView.java` | Modify: real `beforeEnter`, delete `setAdventureData`/dead code, remove ID-less `@RouteAlias` |
| `src/main/java/com/pdg/adventure/view/direction/DirectionsMenuView.java` | Modify: `beforeEnter` resolves adventure + location, delete `setData` |
| `src/main/java/com/pdg/adventure/view/item/ItemsMenuView.java` | Modify: `beforeEnter` resolves adventure + location, delete `setData` |
| `src/main/java/com/pdg/adventure/view/item/AllItemsMenuView.java` | Modify: `beforeEnter` resolves adventure, delete `setData` |
| `src/main/java/com/pdg/adventure/view/command/CommandsMenuView.java` | Modify: `beforeEnter` resolves adventure + location + optional item, delete `setData` |
| `src/main/java/com/pdg/adventure/view/vocabulary/VocabularyMenuView.java` | Modify: add `beforeEnter` (none exists today), delete `setAdventureData` |
| `src/main/java/com/pdg/adventure/view/vocabulary/SpecialWordsView.java` | Modify: add `beforeEnter` (none exists today), delete `setAdventureData` |
| `src/main/java/com/pdg/adventure/view/message/MessagesMenuView.java` | Modify: `beforeEnter` resolves adventure and actually uses it (currently only reads it for a page title), delete `setData` |
| `src/main/java/com/pdg/adventure/view/location/LocationEditorView.java` | Modify: `beforeEnter` resolves adventure + location, delete `setData`, fix Back-button call site |
| `src/main/java/com/pdg/adventure/view/direction/DirectionEditorView.java` | Modify: `beforeEnter` resolves adventure + location + direction, delete `setData` |
| `src/main/java/com/pdg/adventure/view/item/ItemEditorView.java` | Modify: `beforeEnter` resolves adventure + location + item, delete `setData` |
| `src/main/java/com/pdg/adventure/view/command/CommandEditorView.java` | Modify: `beforeEnter` resolves adventure + location + optional item + command, delete `setData` |
| `src/main/java/com/pdg/adventure/view/message/MessageEditorView.java` | Modify: `beforeEnter` resolves adventure + message, delete `setData` |
| `src/main/java/com/pdg/adventure/view/adventure/AdventureEditorView.java` | Modify: simplify "Manage Locations/Vocabulary/Messages/Items" button navigation call sites (drop `.ifPresent(setData)`) |
| `src/test/java/.../**/*MenuViewTest.java`, `*EditorViewTest.java` | Modify existing test classes to cover the new `beforeEnter` resolution + not-found forwarding. Confirmed absent and must be created: `LocationsMenuViewTest`, `DirectionsMenuViewTest`. Test-file existence for the 5 child editors wasn't exhaustively confirmed during investigation — the plan must verify each and create any that are missing. |

The plan must do a codebase-wide sweep (`grep -rn "\.ifPresent.*set(Adventure)?Data"` or equivalent) to find every navigation call site, not just the ones already identified above — several menu views likely navigate to their own child editors on row double-click using the same handoff pattern, and every one of those call sites needs the same simplification.

## Testing Approach

**Browserless unit tests** (one test class per view, extending the project's existing `BrowserlessTest`/`*MenuViewTest.java` convention): mock a `BeforeEnterEvent` with `RouteParameters` set; verify the happy path populates the grid/counter with expected data; verify the not-found path shows the error notification and calls `event.forwardTo(...)` with the correct target per the table above.

**Real browser (Playwright) fresh-load verification — across all 13 views, not a sample.** A mocked `BeforeEnterEvent` proves the wiring is correct in isolation but cannot prove a real browser renders correctly on a cold load — the same reason `curl` couldn't verify the P1.3 route-not-found fix. For each of the 13 views: navigate directly to its canonical URL (not via an in-app click, and not reusing a session that already visited it) and confirm the grid/content renders with real data.

**Regression pass:** re-walk the normal in-app click-through path once (adventure editor hub → Manage Locations → Manage Items → Back, etc.) to confirm removing `setData()` didn't break the already-working live-navigation flow.

**Spot-check cascading forwards:** hand-edit a URL to a bad `:locationId` inside a valid adventure and confirm it lands on that adventure's `LocationsMenuView` with the notification shown (and similarly for at least one deeper case, e.g. a bad `:itemId`) — this is new behavior, not just a bug fix, so it has no prior regression coverage to lean on.

## Non-Goals

- Any caching/performance optimization for the repeated `findAdventureById` calls — explicitly deferred (see Fix Architecture §1); revisit only if profiling ever shows it matters.
- Changes to how `:commandId` is modeled (a specification string, not a ULID) — existing map-lookup semantics are preserved as-is.
- Any other UX audit finding (the play-loop dead end, destructive-action confirmations — already fixed, unknown routes — already fixed, validation feedback, layout overflow, etc.) — out of scope, separate future work.
