# Save and load a game

## Problem

A player cannot keep their progress: a run session is one continuous sitting, `VocabularyData.saveWord` / `loadWord` are unused slots, and `Variable` state, item placement and visit counts live only in memory (see the known gap in `03-domain-model.md` and `04-runtime-engine.md`). Authors need a way to offer `SAVE` and `LOAD` commands in their adventures; players need up to ten saved games per adventure.

This builds on the per-session engine isolation (`2026-10-07-session-isolation-design.md`): the engine state a save captures and restores is per browser session.

## Player-facing behaviour

The author binds the commands in the Workflow, like the Auto-item actions: a Response for the verb `save` (and one for `load`) on the wildcard noun `~`, with the new **Save Game** / **Load Game** action. The wildcard matches "no noun" as well, so one Response serves `save` and `save 3`.

| Input | Result |
|-------|--------|
| `save` | Saves into the lowest free slot of this player and adventure. If all 10 slots are taken: refuses and asks for a slot number. |
| `save N` (1-10) | Saves into slot N, overwriting it silently (the player chose the slot). |
| `load` | Lists this player's saved games for this adventure, used slots only, as `N. <adventure title> - yyyy-MM-dd HH:mm:ss` (server time zone). "You have no saved games." when there are none. |
| `load N` | Restores slot N, then describes the restored location. A slot without a save is reported. |
| `save`/`load` followed by a noun that is not a slot number (e.g. `save lamp`) | "There are only slots 1 to 10." |

Numbers outside 1-10 are not vocabulary words, and the parser silently drops unknown words, so `save 11` behaves like a bare `save` and `load 11` like a bare `load` (known limitation, harmless: nothing is lost).

After `load` alone the player may simply type the number: the parser infers the missing verb from the previous sub-command, so `3` means `load 3`.

## Goals / non-goals

Goals: ten slots per player and adventure; best-effort restore after the author changed the adventure; a version stamp so `load` can warn about a mismatch; all texts translatable through the System Message catalog.

Non-goals (YAGNI): deleting saves, named slots, auto-save, saving across adventures, rebuilding the world from the authored data on `load` (decision: in-place restore), saving the author's workflow/commands (those are authored, not runtime state), nested containers (the mappers register only locations' item containers and the pocket).

## Design

### 1. Snapshot and in-place restore

`GameSnapshotData` (embedded, `model` package, ids and values only):
`currentLocationId`, `currentPictureId`, `knownItemIds` (every item id registered at save time), `containers` (container id -> ordered item ids), `wornItemIds`, `lumen` (item id -> lumen, non-zero only), `visits` (location id -> `timesVisited`), `variables` (name -> value).

`GameStateSnapshotter` (engine package) reads `GameContext` and the `AdventureConfig` registries through the session-scoped proxies:

- `capture()` walks the **registered containers**, not `getParentContainer()`: `DestroyAction` removes an item from its container but leaves that pointer stale. Item ids come from the container contents (`Item`s); an item in no container is "nowhere" and is covered by `knownItemIds`.
- `restore(snapshot)` is best-effort. It first checks that the saved location still exists and returns `false` without touching anything if it does not (never a partial restore). Otherwise: (1) remove every known item from the container it is in, (2) for each saved container that still exists set its saved contents in the saved order and point each item's parent at it (ids that no longer exist are skipped; capacity checks are bypassed because the saved state was valid), (3) reapply worn flags and lumen to every known item and visit counts and variables from the save, (4) move the player to the saved location and restore the picture id. Things the save does not mention are left alone. In the common case (a fresh game, then `load`) the world is the authored one, so the restore is exact. The known limitation of the in-place approach: an item the author added after the save, if the player has since moved it, stays where it is now.
- Variables not in the save keep their current value (author-added variables, and `VISITED`, which is reassigned on every move anyway).

### 2. Storage

`SavedGameData` is a Mongo document (collection `savedgames`): `userId`, `adventureId`, `slot`, `savedAt` (`Instant`), `builderVersion`, `snapshot`. Its `_id` is deterministic, `"<userId>:<adventureId>:<slot>"`, so Mongo itself guarantees one save per slot and `save N` is a plain upsert (no extra index needed). `SAVED_GAME_SLOTS = 10` is a constant on `SavedGameData`.

`SavedGameRepository extends MongoRepository<SavedGameData, String>` (`findByUserIdAndAdventureId`). `SavedGameService` implements the slot rules: `freeSlot` (lowest unused slot), `save` (upsert, slot range checked), `list` (by slot), `find`, and the display `label(title, savedAt)`. A save is never shared between users (the user id is part of every key).

### 3. Run identity

`RunOwner` gains the player id. `AdventureRunSessionFactory.start` puts a `GameContext.RunIdentity(playerId, adventureId, adventureTitle, builderVersion)` on the (session-scoped) `GameContext`; `release` clears it. The actions read it, so they need no user lookup of their own. Author test sessions save under the author's own id like any player.

### 4. Actions and author binding

`SaveGameAction` and `LoadGameAction` (server/action) take `GameContext`, `GameStateSnapshotter` and `SavedGameService`; the slot comes from `gameContext.getCurrentNoun()` (like `AutoTakeAction`). Failures return `FAILURE` so a command chain stops and `GameLoop` prints the message. Both are wired like `AutoTakeAction`: `SaveGameActionData` / `LoadGameActionData`, mappers (`@AutoRegisterMapper`), editor components (`@AutoRegisterActionEditor`), entries in `ActionSelector` and `PreconditionActionFormatter`.

`load` describes the restored location with its look text and the restored picture; it does not run arrival processes or change visit counts.

### 5. Slot numbers as words

The parser silently drops unknown words, so `save 3` would be a bare `save`. `registerBaseVerbs` therefore adds the nouns `1`-`10` to the run vocabulary (skipping any the author already defined).

### 6. Messages

Ten new descriptive keys in `SystemMessageKey` (`SAVE_DONE`, `SAVE_FULL`, `SLOT_INVALID`, `SAVELOAD_UNAVAILABLE`, `LOAD_LIST_HEADER`, `LOAD_NONE`, `LOAD_DONE`, `LOAD_EMPTY_SLOT`, `LOAD_CANNOT`, `LOAD_VERSION_NOTE`). Only `%s` placeholders are used (`PlaceholderSpec` supports `%s` / `%n$s` only).

### 7. Version stamp

`AdventureData.builderVersion` (string) holds the builder version that last wrote the adventure. It is assigned in `AdventureService.saveAdventureData`, the one choke point every editor goes through (the location, item, message and picture editors call it directly, not via `AdventureAccessService`). The version comes from Spring Boot's `BuildProperties` (the `build-info` goal is added to the Maven build); when it is unavailable the field is left as it is. A save stores the version it saw; `load` appends a warning when both versions are known and differ. `null` means unknown and never warns.

### 8. Error handling

- No run identity (an action used outside a run session): `SAVELOAD_UNAVAILABLE`, `FAILURE`.
- Saved location gone: `LOAD_CANNOT`, nothing changed.
- Unknown item/container/location ids in a save are skipped.
- Version mismatch: a warning line only.

## Testing

Unit tests for the snapshotter (real `Item`s, containers, locations, including a destroyed item and an item that is "nowhere"), the slot service (mocked repository), the actions (through a real `GameLoop` like `AutoTakeDropGameLoopTest`), the mappers (real mapper dispatch), the selector/formatter, the build-version stamp, the digit nouns and the new message keys; the whole suite stays green.

## Documentation

Specs `02`, `03`, `04`, `07` (known gaps, action list, selector) and the author handbook (action reference, how to bind `SAVE`/`LOAD`).
