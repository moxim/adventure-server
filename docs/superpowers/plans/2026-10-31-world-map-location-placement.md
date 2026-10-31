# World Map: Placing Locations and Connecting Them Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An author places locations on the world map's 10 x 10 grid and connects them with directions, including special ones such as "enter door", from the map itself.

**Architecture:** `LocationData` gets a nullable grid cell. Pure placement/replace rules live in one static helper (`LocationMapPlacement`) that is unit-tested without UI. A second static helper (`DirectionBuilder`), extracted from `DirectionEditorView.saveData`, builds directions so the map and the editor cannot drift. `LocationMapView` gets a Place/Connect toggle and three small dialogs. No engine, parser, mapper or `api`/`editor` module changes.

**Tech Stack:** Java 25, Spring Boot, Vaadin Flow (Browserless tests: `BrowserlessTest`, `find()`/`test()`), MongoDB via `AdventureService`, JUnit 5, AssertJ, Mockito.

**Spec:** `server/docs/superpowers/specs/2026-10-31-world-map-location-placement-design.md`

## Global Constraints

- All work is in the `server` module; do not touch `api` or `editor` (AGENTS.md).
- Top edge of the map is north; cells are numbered from 0, column left to right, row top to bottom (`PictureGrid.Cell(column, row)`, `PictureGrid.SIZE` = 10).
- No hardcoded or auto-created compass vocabulary: the verb (mandatory), adjective and noun always come from `VocabularyPickerField`s populated from the adventure's vocabulary.
- "Same words" = equal verb, adjective and noun (`Word.equals`, which compares ids; null-safe). "A direction from source to target" = `DirectionData.destinationId` equals the target's id.
- Duplicate words on a location always warn and let the author confirm or cancel (Connect mode source and opposite direction; direction moved onto Y in a replace).
- Replace carries over directions only; start location, items, conditions and other move actions stay on X.
- Replace saves the affected locations one by one through `AdventureService.saveLocationData`; no bulk save, no `AdventureService` change.
- Editing an existing connection opens `DirectionEditorView` (separate page; its Back button not returning to the map is accepted).
- Wherever you hit ambiguity, add `// TODO: Review needed — [reason]` (AGENTS.md).
- Follow `AGENTS-conventions.md`, `AGENTS-views.md`, `AGENTS-testing.md`. Check Vaadin APIs with the Vaadin MCP/docs, not by grepping JARs.
- Do not commit: the user commits themselves. Leave the working tree changed and say so in the hand-off.
- Run Maven from `server/` with `JAVA_HOME=/opt/homebrew/opt/openjdk@25`. If a run shows mass bogus compile errors, a compile hook raced with `mvn test`; just rerun.

## Review Focus

- Stored data where two locations share a cell, or a cell is outside 0..9 (hand-edited or imported): the map must render, not throw.
- No unplaced locations (empty adventure, or all placed): Place/Replace show a notification instead of an empty dialog.
- Connect mode: a cell connected to itself (second click on the same cell) gets the "Dangerous Destination" confirmation; switching mode drops a pending source click.
- Adventure vocabulary without any verb: the connect form cannot be saved and says why.
- A location with no noun/adjective: the hover tooltip must not throw and must show something.

---

## File Structure

| File | Responsibility |
|------|----------------|
| `model/LocationData.java` (modify) | `Integer mapColumn`, `Integer mapRow` |
| `view/direction/DirectionBuilder.java` (create) | Build/finish a `DirectionData` the way the editor does |
| `view/direction/DirectionEditorView.java` (modify `saveData`) | Call `DirectionBuilder.attach` |
| `view/location/LocationMapPlacement.java` (create) | Pure rules: lookup, placing, duplicates, replace |
| `view/support/PictureGrid.java` (modify) | Overlay with a click callback; cell lookup |
| `view/location/LocationMapView.java` (modify) | Mode toggle, occupied cells, tooltips, dialog wiring |
| `view/location/PlaceLocationDialog.java` (create) | Pick an unplaced location for an empty cell |
| `view/location/ReplaceLocationDialog.java` (create) | Replace X by Y, choose directions to move |
| `view/location/ConnectLocationsDialog.java` (create) | New direction(s) or edit existing between two locations |
| `META-INF/resources/styles/world-map.css` (modify) | Occupied and selected cell styles |
| `CHANGELOG.md` (modify) | One "Added" entry |

(Paths are under `server/src/main/java/com/pdg/adventure/` except CSS (`server/src/main/resources/`) and the changelog; tests mirror them under `server/src/test/java/com/pdg/adventure/`.)

---

### Task 1: Map cell on `LocationData`

**Files:**
- Modify: `model/LocationData.java`
- Test: `model/LocationDataTest.java` (create if absent)

**Interfaces:**
- Produces: `Integer getMapColumn()/setMapColumn(Integer)`, `Integer getMapRow()/setMapRow(Integer)` (Lombok `@Data`); both null = unplaced.

- [ ] **Step 1: Write failing test `newLocationIsUnplaced`**: `new LocationData()` has `getMapColumn()` and `getMapRow()` null.
- [ ] **Step 2: Run** `mvn -q test -Dtest=LocationDataTest`. Expected: compile FAIL (no such getters).
- [ ] **Step 3: Add the two nullable `Integer` fields** after `pictureId`. No mapper change (position is data-only).
- [ ] **Step 4: Run the test.** Expected: PASS.

---

### Task 2: `DirectionBuilder` extracted from the editor

**Files:**
- Create: `view/direction/DirectionBuilder.java`
- Modify: `view/direction/DirectionEditorView.java` (`saveData`, currently copies owner adjective/noun into the description, builds the `MovePlayerActionData`, adds to the set)
- Test: `view/direction/DirectionBuilderTest.java`

**Interfaces:**
- Produces:
  - `public static void attach(DirectionData direction, LocationData owner)` — sets `direction`'s description adjective and noun from `owner.getDescriptionData()`, replaces its command actions with a single `MovePlayerActionData` whose `locationId` is `direction.getDestinationId()`, adds it to `owner.getDirectionsData()`. Does not save. Idempotent (safe to call again after the destination changes).
  - `public static DirectionData create(LocationData owner, String destinationId, Word verb, Word adjective, Word noun)` — new `DirectionData` with destination and command description set, then `attach`ed; returns it. Short/long descriptions left empty (derived later, as in the editor).

- [ ] **Step 1: Write failing tests** in `DirectionBuilderTest`:
  - `createSetsWordsDestinationAndMoveAction`: result's `getCommandData().getCommandDescription()` has the given verb/adjective/noun; `getDestinationId()` equals "dest"; actions hold exactly one `MovePlayerActionData` with `getLocationId()` "dest".
  - `createAddsDirectionToOwnerAndCopiesOwnerWordsIntoDescription`: owner set contains it; `getDescriptionData().getNoun()` equals the owner's noun.
  - `attachAgainAfterRepointingUpdatesMoveAction`: change `setDestinationId("other")`, `attach` again → still one action, `locationId` "other", owner set size unchanged.
- [ ] **Step 2: Run** `mvn -q test -Dtest=DirectionBuilderTest`. Expected: compile FAIL.
- [ ] **Step 3: Implement** both methods in a final utility class (private constructor).
- [ ] **Step 4: Replace the body of `DirectionEditorView.saveData`** between `writeBean` and `saveLocationData` with `DirectionBuilder.attach(directionData, locationData)`. Behaviour must not change.
- [ ] **Step 5: Run** `mvn -q test -Dtest='DirectionBuilderTest,DirectionEditorView*Test,DirectionsMenuViewTest'`. Expected: all PASS (existing editor tests prove nothing drifted).

---

### Task 3: `LocationMapPlacement` rules (no UI)

**Files:**
- Create: `view/location/LocationMapPlacement.java`
- Test: `view/location/LocationMapPlacementTest.java`

**Interfaces:**
- Consumes: Task 1 getters/setters; Task 2 `DirectionBuilder.attach`.
- Produces (all `public static` on a final class):
  - `Optional<LocationData> locationAt(AdventureData adventure, PictureGrid.Cell cell)` — first location of `adventure.getLocationData().values()` on that cell.
  - `List<LocationData> unplaced(AdventureData adventure)` — locations with null column or row, sorted by `ViewSupporter.formatDescription`.
  - `void place(LocationData location, PictureGrid.Cell cell)`
  - `List<DirectionData> directionsTo(LocationData source, String targetId)` — source's directions whose `destinationId` equals `targetId`.
  - `Optional<DirectionData> findSameWords(LocationData owner, Word verb, Word adjective, Word noun)`
  - `List<DirectionData> directionsToDrop(LocationData x, LocationData y)` — Y's directions with destination X.
  - `List<DirectionData> unmovable(LocationData x, LocationData y)` — X's directions with destination Y.
  - `List<DirectionData> clashes(LocationData y, Collection<DirectionData> moved)` — Y's directions with the same words as any in `moved`.
  - `List<LocationData> replace(AdventureData adventure, LocationData x, LocationData y, Collection<DirectionData> moved)` — mutates; returns the distinct locations that changed (the caller saves each). Order of rules: (1) compute `directionsToDrop` and `unmovable` first; (2) remove the dropped ones from Y; (3) Y takes X's cell, X's cell becomes null; (4) every direction anywhere with destination X is re-pointed to Y (`setDestinationId`, then `DirectionBuilder.attach(direction, itsOwner)`); (5) each `moved` direction not in `unmovable` is removed from X's set, any same-words direction on Y is removed, then it is added to Y via `attach(direction, y)` (so it gets Y's adjective/noun).

- [ ] **Step 1: Write failing tests** (build `AdventureData` + `LocationData`s by hand, `Word`s via `new Word("go", Word.Type.VERB)`):
  - `locationAtFindsOccupiedCellAndEmptyOtherwise`, `unplacedListsOnlyLocationsWithoutCell`.
  - `findSameWordsComparesVerbAdjectiveNounAndIgnoresDifferentWords` (including null adjective on both sides = same).
  - `replaceMovesCellAndRepointsIncomingDirectionsAndTheirMoveActions`: A→X becomes A→Y with `MovePlayerActionData.locationId` = Y's id; X's cell null, Y's cell = X's old cell.
  - `replaceMovesOnlyTickedOutgoingDirections`: X has two exits; only the ticked one ends up on Y, the other stays on X.
  - `replaceReplacesSameWordedDirectionOnYAndGivesMovedOneYsNoun`.
  - `replaceDropsYsDirectionToX` and `replaceDoesNotMoveXsDirectionToY` (stays on X).
  - `replaceOfSelfLoopOnX`: an X→X direction becomes X→Y; ticked, it moves to Y and is a Y→Y loop. (`// TODO: Review needed — loop produced by replace`.)
  - `replaceReturnsEveryChangedLocationOnce`.
- [ ] **Step 2: Run** `mvn -q test -Dtest=LocationMapPlacementTest`. Expected: compile FAIL.
- [ ] **Step 3: Implement** the class per the signatures above. Use `Objects.equals` on `Word`s.
- [ ] **Step 4: Run the test.** Expected: PASS.

---

### Task 4: `PictureGrid` click callback and cell lookup

**Files:**
- Modify: `view/support/PictureGrid.java`, `META-INF/resources/styles/world-map.css`
- Test: `view/support/PictureGridTest.java` (add)

**Interfaces:**
- Produces:
  - `public static Div createOverlay(Consumer<Cell> onCellClick)` — as `createOverlay()` but a click calls the consumer instead of showing the notification. `createOverlay()` keeps today's behaviour (delegate with the notification consumer), so the picture editor and the run view are untouched.
  - `public static Div cellComponent(Div overlay, Cell cell)` — the overlay's child at index `row * SIZE + column`.
- CSS classes: `world-map-cell-occupied` (darker background, still lighter on hover), `world-map-cell-selected` (outline for the pending Connect source).

- [ ] **Step 1: Write failing tests**: `overlayWithCallbackReportsClickedCell` (click `cellComponent(overlay, new Cell(3, 7))` via `test()`/`ComponentUtil.fireEvent`-style as the existing tests do → consumer receives `Cell(3, 7)`, no notification); `cellComponentFindsChildByRowAndColumn`; existing `createOverlay()` test still passes.
- [ ] **Step 2: Run** `mvn -q test -Dtest=PictureGridTest`. Expected: FAIL.
- [ ] **Step 3: Implement** the two methods and the two CSS rules.
- [ ] **Step 4: Run** `mvn -q test -Dtest='PictureGridTest,PictureEditorViewBrowserlessTest'`. Expected: PASS.

---

### Task 5: Map view — Place mode, occupied cells, tooltips

**Files:**
- Modify: `view/location/LocationMapView.java` (constructor becomes `LocationMapView(AdventureAccessService, AdventureService)`; update `LocationMapViewTest` and `AdventureEditorViewWorldMapTest` if it constructs it)
- Create: `view/location/PlaceLocationDialog.java`
- Test: `view/location/LocationMapViewTest.java` (add)

**Interfaces:**
- Consumes: Tasks 1, 3, 4.
- Produces:
  - In `LocationMapView`: nested `enum Mode { PLACE, CONNECT }`, a `RadioButtonGroup<Mode>` above the map (default PLACE), `private void refresh()` re-rendering the overlay's occupied classes and tooltips from the current `AdventureData`, and a package-private hook `void onCellClicked(PictureGrid.Cell cell)` that the overlay callback calls (Tasks 6 and 7 extend it).
  - `new PlaceLocationDialog(AdventureService service, List<LocationData> unplaced, PictureGrid.Cell cell, Runnable onDone)`: `Dialog` with a single-select `Grid<LocationData>` (column `ViewSupporter::formatDescription`) and Place/Cancel buttons; Place calls `LocationMapPlacement.place`, `service.saveLocationData`, closes, runs `onDone`.
  - Tooltip on an occupied cell: `new DescriptionProvider(location.getDescriptionData()).getEnrichedBasicDescription()`; if that is blank, fall back to `ViewSupporter.formatDescription(location)`. Verify the Vaadin tooltip API with the Vaadin MCP; a `title` attribute is an acceptable fallback.

- [ ] **Step 1: Write failing tests** (existing test class conventions, `enterAdventure` helper extended with locations):
  - `occupiedCellHasOccupiedClassAndTooltip`: a location at (2, 3) → `cells()` index 3 * 10 + 2 has class `world-map-cell-occupied` and the tooltip text.
  - `clickOnEmptyCellInPlaceModeOpensDialogListingOnlyUnplacedLocations`.
  - `placingStoresCellSavesLocationAndMarksCell`: verify `adventureService.saveLocationData(location)` and the cell now occupied.
  - Review Focus: `twoLocationsOnSameCellAndOutOfRangeCellDoNotBreakTheMap`; `noUnplacedLocationsShowsNotificationInsteadOfDialog`; `locationWithoutNounStillGetsATooltip`.
- [ ] **Step 2: Run** `mvn -q test -Dtest=LocationMapViewTest`. Expected: FAIL.
- [ ] **Step 3: Implement** the view changes and the dialog. `createMap` builds the overlay with `PictureGrid.createOverlay(this::onCellClicked)`; in PLACE mode an empty cell opens the dialog (or `Notification.show("No unplaced locations.")`); an occupied cell does nothing yet (Task 6). Out-of-range cells are ignored; the first location in iteration order wins a shared cell.
- [ ] **Step 4: Run** `mvn -q test -Dtest='LocationMapViewTest,AdventureEditorViewWorldMapTest'`. Expected: PASS.

---

### Task 6: Replace a location (Place mode, occupied cell)

**Files:**
- Create: `view/location/ReplaceLocationDialog.java`
- Modify: `view/location/LocationMapView.java` (`onCellClicked`)
- Test: `view/location/ReplaceLocationDialogTest.java`; `LocationMapViewTest` (add one routing test)

**Interfaces:**
- Consumes: Task 3 rules, Task 5 hook.
- Produces: `new ReplaceLocationDialog(AdventureData adventure, AdventureService service, LocationData x, Runnable onDone)`: `Dialog` asking whether X should be replaced; a `Grid<LocationData>` of unplaced locations (Y); a multi-select `Grid<DirectionData>` of X's outgoing directions (excluding `unmovable`) labelled with `ViewSupporter.formatDescription(direction's CommandDescriptionData)`; a note listing directions that will be dropped (`directionsToDrop`) or cannot move (`unmovable`); Replace/Cancel. On Replace: if `clashes(y, moved)` is non-empty a `ConfirmDialog` names them and proceeds only on confirm; then `LocationMapPlacement.replace`, `service.saveLocationData` for each returned location, close, `onDone`. With no unplaced locations: `Notification` and no dialog.

- [ ] **Step 1: Write failing tests**:
  - `replaceWithoutClashSavesEveryChangedLocation` (verify each saved once, incoming direction now points to Y).
  - `clashAsksForConfirmationAndSavesNothingUntilConfirmed`; `cancelOnClashLeavesDataUntouched`.
  - `dialogListsDropsAndUnmovableDirectionsBeforeConfirming`.
  - routing: clicking an occupied cell in Place mode opens `ReplaceLocationDialog`.
- [ ] **Step 2: Run** `mvn -q test -Dtest='ReplaceLocationDialogTest,LocationMapViewTest'`. Expected: FAIL.
- [ ] **Step 3: Implement** the dialog and route occupied-cell clicks in PLACE mode to it.
- [ ] **Step 4: Run** the same command. Expected: PASS.

---

### Task 7: Connect mode

**Files:**
- Create: `view/location/ConnectLocationsDialog.java`
- Modify: `view/location/LocationMapView.java`
- Test: `view/location/ConnectLocationsDialogTest.java`; `LocationMapViewTest` (add)

**Interfaces:**
- Consumes: Tasks 2, 3, 4, 5. Existing: `VocabularyPickerField(String label, String tooltip)` + `populate(vocabulary.getWords(Word.Type.X))`, `RouteIds.ADVENTURE_ID/LOCATION_ID/DIRECTION_ID`, route `DirectionEditorView` with `RouteParameters` of those three.
- Produces: `new ConnectLocationsDialog(AdventureData adventure, AdventureService service, LocationData source, LocationData target, Runnable onDone)`:
  - If `directionsTo(source, target.getId())` is non-empty: lists them (label via `ViewSupporter.formatDescription`), each with an **Edit** button navigating to `DirectionEditorView` (adventure id, source id, direction id), plus **Add another direction** which reveals the form below.
  - Form (shown directly when none exist): verb (required), adjective, noun pickers from the adventure's vocabulary; checkbox **"Create the opposite direction?"**; when ticked, a second set of pickers (verb required) for target→source. Save is disabled until all required verbs are set. With no verbs in the vocabulary, a hint says a verb must be added to the vocabulary first and Save stays disabled.
  - Save: collect warnings — `findSameWords(source, …)` (and `findSameWords(target, …)` for the opposite) → `ConfirmDialog` per spec; `source == target` → "Dangerous Destination" `ConfirmDialog` with the same wording as `DirectionEditorView`. Only after confirmation: `DirectionBuilder.create(...)` for each, `service.saveLocationData` for each owner (once per owner), close, `onDone`.
- In `LocationMapView`: in CONNECT mode the first click on an occupied cell stores `pendingSource` (cell gets `world-map-cell-selected`, `Notification` "Now click the target location"); the second click on an occupied cell opens the dialog and clears the pending state; a click on an empty cell shows "Click a cell that holds a location." and keeps the pending source; changing mode clears pending state. A second click on the same cell is a legal target (loop).

- [ ] **Step 1: Write failing tests**:
  - `savingNewDirectionCreatesItOnSourceWithGivenWords` (verify `saveLocationData(source)`, direction destination = target).
  - `oppositeCheckboxRevealsSecondPickersAndCreatesBothDirections`; `saveDisabledUntilVerbPicked` (also for the opposite verb when ticked).
  - `existingDirectionsAreListedWithEditAndAddAnother`; `editButtonNavigatesToDirectionEditorView`.
  - `sameWordsOnSourceAsksForConfirmationAndSavesNothingUntilConfirmed`; same for the opposite direction on target.
  - Review Focus: `connectingLocationToItselfAsksDangerousDestinationConfirmation`; `noVerbsInVocabularyShowsHintAndKeepsSaveDisabled`.
  - View: `connectModeNeedsTwoClicksOnOccupiedCells`; `switchingModeDropsPendingSource`; `clickOnEmptyCellInConnectModeShowsNotificationAndKeepsSource`.
- [ ] **Step 2: Run** `mvn -q test -Dtest='ConnectLocationsDialogTest,LocationMapViewTest'`. Expected: FAIL.
- [ ] **Step 3: Implement** the dialog and the CONNECT branch of `onCellClicked`.
- [ ] **Step 4: Run** the same command. Expected: PASS.

---

### Task 8: Changelog and full verification

**Files:**
- Modify: `server/CHANGELOG.md` (`## [Unreleased]` → `### Added`, newest entry first, per `AGENTS-update-changelog.md`)

- [ ] **Step 1: Add one entry**: authors place locations on the world map's grid and connect them with directions (Place/Connect toggle, replace with carry-over of directions, optional opposite direction, warning on duplicate words, edit of existing connections in the Direction editor).
- [ ] **Step 2: Run the full suite** with the `verify-tests` skill. Expected: all tests pass; report the pass count.
- [ ] **Step 3: Start the app and check by hand** (`run` skill; log in with a reset author password per the project notes): place two locations, connect them with "go"/"north" and the opposite, add "enter"/"door", replace one location, hover a cell. Report what was seen; do not claim success without it.

---

## Self-Review (spec coverage)

| Spec item | Task |
|-----------|------|
| `mapColumn`/`mapRow` | 1 |
| Shared direction builder | 2 |
| Place, unplaced-only picker, occupied darker, tooltip | 4, 5 |
| Replace + carry-over + drop/skip notice + clash confirm | 3, 6 |
| Connect, selectors, opposite checkbox, edit existing, add another | 7 |
| Duplicate-words warning, loop confirmation | 3, 6, 7 |
| Changelog, TODO rule, no engine changes | 8, Global Constraints |
