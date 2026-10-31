# World map: placing locations and connecting them — design

## Intent

An author already sees the adventure's world map (`LocationMapView`): a picture with a clickable 10 x 10 grid on top
(`PictureGrid.createOverlay()`), where a click only shows a notification. This feature makes the map useful: the
author **places locations on grid cells** and **connects them with directions** directly on the map, instead of
building every exit by hand in the location's Directions menu.

The top edge of the map is north, the bottom south, the right east, the left west. The compass is only a guide for
the author; the words a direction uses are always chosen by the author (see below).

## Goals

- Place an unplaced location on a grid cell; see at a glance which cells hold a location.
- Replace the location in a cell by another one, carrying the connections over.
- Connect two placed locations with a direction, optionally creating the opposite direction in the same step.
- Edit an existing connection between two locations, including special directions such as "enter door".
- Change as little of the existing code as possible: reuse `DirectionData`, `DirectionEditorView`,
  `VocabularyPickerField`, `PictureGrid` and `AdventureService`.

## Non-goals

- No change to the engine, the parser, the `api` or `editor` modules, or the run view.
- No hardcoded compass vocabulary and no automatic creation of vocabulary words.
- No automatic compass-word suggestion from the relative cell positions.
- No removing a location from the map (not asked for; the author can replace it).
- No map-position awareness in the engine (players never see the grid).

## Anti-goals

- A map that silently changes connections the author did not ask to change.
- A second, parallel way of building a direction that drifts from what `DirectionEditorView` produces.
- A direction created without a verb.

## Constraints

- All work happens in the `server` module (AGENTS.md). Project conventions in `AGENTS-*.md` apply.
- Locations live in MongoDB (`LocationData`), reached through `AdventureService`.
- There is no vocabulary of compass words. Words come from the adventure's own `VocabularyData`.
- Browserless UI tests (`BrowserlessTest`) are the test style for views.

## The design

### Data

`LocationData` gets two nullable fields, `Integer mapColumn` and `Integer mapRow` (0-based, matching
`PictureGrid.Cell`). Both null means the location is unplaced. No migration: existing documents read as unplaced.
Two locations never share a cell (enforced by the map view, not by the database).

### Map view (`LocationMapView`)

- A toggle above the map switches between **Place** and **Connect** mode (default Place).
- A cell holding a location is drawn darker. Hovering it shows that location's
  `DescriptionProvider.getEnrichedBasicDescription()` as a tooltip.
- `PictureGrid.createOverlay()` currently adds a cell click listener that shows a notification. It needs a way for
  the map to receive the clicked cell and to mark cells as occupied; the run view and the picture editor, which also
  use the overlay, keep today's behaviour.

### Place mode

- Click an **empty** cell: a dialog lists only **unplaced** locations of the adventure. Choosing one stores its cell
  and saves the location.
- Click an **occupied** cell (location X): a dialog asks whether X should be replaced and, if so, offers the unplaced
  locations (Y) to replace it with. On confirmation:
  - Y takes X's cell; X becomes unplaced.
  - Every direction in the adventure whose destination is X is re-pointed to Y (its move action's destination too).
  - The dialog also shows X's outgoing directions as a list of checkboxes. The ticked ones are moved from X to Y; a
    moved direction replaces any direction on Y with the same verb, adjective and noun (after the author confirms,
    see "Duplicate words"). Unticked ones stay on X.
  - My calls on edge cases: a direction of Y that pointed to X would become a loop on Y and is dropped; a direction
    of X that points to Y is not moved (it would become a loop on Y).

### Replace carries over directions only (decided by the user)

Replace never touches other references to X: the start location, items at X, PLAYER AT / ITEM AT conditions and move
actions inside ordinary commands all keep pointing at X, which stays in the adventure as an unplaced location.

### Duplicate words warn and confirm (decided by the user)

Whenever a direction is about to be added to a location that already has a direction with the same verb, adjective
and noun, a confirm dialog names the existing direction and lets the author proceed or cancel (like the loop
warning in `DirectionEditorView`). This applies to Connect mode (the new direction on the source, and the opposite
direction on the target), and to a direction moved onto Y in a replace. In a replace the moved direction replaces
Y's same-worded one only after the author confirms.

### Connect mode

Click a source cell, then a target cell (both must hold a location; otherwise a notification says so).

- **No direction from source to target yet:** a dialog shows the verb, adjective and noun selectors
  (`VocabularyPickerField`, verb mandatory, populated from the adventure's vocabulary, same as in
  `DirectionEditorView`) and a checkbox **"Create the opposite direction?"**. If ticked, the same three selectors
  appear again for the target-to-source direction (verb mandatory). Saving creates the direction(s) exactly as
  `DirectionEditorView.saveData` does: command description from the selectors, a `MovePlayerActionData` pointing at
  the destination, and the description's adjective and noun taken from the owning location.
- **One or more directions from source to target already exist:** the dialog lists them, each with an **Edit**
  button that navigates to `DirectionEditorView` (a separate page; its Back button returns to the location's
  Directions menu, not to the map — accepted), plus an **Add another direction** button that shows the form above.
  Several directions between the same two locations are legal (for example "go north" and "enter door").
- Special directions such as "enter door" need nothing extra: the author picks the verb "enter" and the noun "door"
  in the same selectors.

### Shared code

Building a `DirectionData` from selected words is currently inside `DirectionEditorView.saveData`. It should be
extracted into a small shared helper that both the editor view and the map dialog call, so the two cannot drift.
The extraction must not change the editor view's behaviour. Both views keep their existing behaviour.

## Left to the builder

- Class and package names for the new dialogs and the helper (follow `AGENTS-conventions.md`).
- How `PictureGrid` exposes cell clicks and occupied cells without disturbing its other users.
- CSS for the darker cell (extend `world-map.css`).
- Test breakdown; the replace-with-carry-over logic should be unit-tested apart from the UI.
- A `// TODO: Review needed` comment wherever the builder meets ambiguity (AGENTS.md rule), and a CHANGELOG entry per
  `AGENTS-update-changelog.md`.

## Decisions made by the author of this document (not by the user)

- Default mode is Place.
- Edge-case handling in replace (loops dropped or skipped), described above.
- Locations cannot be removed from the map in this feature.
- No visual line or arrow is drawn between connected cells.
- "Same words" means equal verb, adjective and noun (`Word` equality, as the existing model defines it).
  "A direction from source to target" means `DirectionData.destinationId` equals the target's id.
- A direction moved from X to Y gets Y's adjective and noun in its `descriptionData`, as `saveData` does for a new
  direction. Re-pointed incoming directions stay with their original owners and need no description change.
- Connect mode with source cell equal to target cell is allowed, with the same "Dangerous Destination" confirmation
  `DirectionEditorView` shows. The "Create the opposite direction?" checkbox is always offered, even when the target
  already has a direction back; it just adds another.
- When a replace would drop a direction of Y that pointed to X, or skip a direction of X that points to Y, the
  replace dialog says so before the author confirms.
- A replace saves the affected locations one by one through the existing `AdventureService.saveLocationData`. There is
  no bulk save and no transaction; a failure part-way leaves the map half-updated and is reported as an error.
  Acceptable here because there is no production environment (AGENTS.md).
