# Per-session engine isolation

## Problem

The play engine's state lives in process-wide singletons. `AdventureRunSessionFactory.start` drives the shared `GameContext` and `AdventureConfig` beans (see `AdventureRunSessionFactory` Javadoc: "no per-session engine isolation, so at most one run session is meaningfully active at a time"). With more than one player in the process this breaks in five ways:

1. **A second `start` wipes the first player's world.** `start` runs `LoadAdventureAction.loadAdventure`, which clears the shared `allLocations`, `allItems`, `allContainers` and `allMessages` registries and replaces the shared `GameContext`'s current location and pocket. Player A's next command then runs against player B's adventure.
2. **Two players on the same adventure share one game** (same pocket, location, variables).
3. **Output goes to the wrong browser.** Per-turn state (`currentNoun`, `currentAdjective`, `outputSink`, ...) is stored on the shared `GameContext`. Two simultaneous turns overwrite each other's sink, so one player's text can land in the other's browser.
4. **System-message overrides leak across adventures.** `SystemMessageKey.overridesByKeyId` is one `static` slot (`SystemMessageKey.java:127`). It is installed by `LoadAdventureAction` on every adventure load, so the last adventure loaded wins for everyone. The editor views show those overrides too.
5. **A save/load feature (next spec) would be unsafe.** A save would snapshot whatever is in the shared state, possibly another player's game; a restore would corrupt everyone else's.

This spec fixes isolation only. Save/load is a separate spec that builds on this one.

## Goals

- Several players can play at once, each with their own game state, whether on the same or different adventures.
- One game per browser (Vaadin) session; a second start in the same session is blocked.
- No change to the roughly 50 classes that reference `GameContext` / `AdventureConfig`, nor to the mappers.

## Non-goals

- Clustering, or serialising a running game across server restarts.
- Several concurrent games in one browser session.
- Save/load.
- Any change to the author editor flows.

## Design

### 1. What becomes per Vaadin session, what stays shared

**Per Vaadin session** (Vaadin's session scope with scoped proxies):

- `GameContext`.
- The six registries `AdventureConfig` exposes: `allLocations`, `allItems`, `allContainers`, `allWords`, `allMessages`, `allVariables`.
- Everything the factory builds per run (`Workflow`, `Parser`, `GameLoop`) hangs off those and so becomes per-session without further changes.

**Shared singletons, unchanged:** the mappers, `MapperSupporter`, `AdventureService`, `AdventureAccessService`, `AdventureRunSessionFactory`. They keep injecting the same types.

Why this works for the singletons: `MapperSupporter` copies its references from `AdventureConfig` once, in its constructor (`MapperSupporter.java:45-50`). Once the `allX()` beans are scoped proxies, those copies are the proxies, and every call resolves to the calling browser session's instance. The action mappers that call `adventureConfig.allItems()` etc. behave the same way.

**Lifecycle:** created lazily on a session's first run; destroyed with the Vaadin session. A run can only be started or driven on a thread bound to a Vaadin session, which holds for UI event handlers. Plain unit tests that build objects by hand are unaffected.

**To verify during implementation:**

- The proxied classes must be non-final with no-arg constructors (`GameContext`, `Vocabulary`, `MessagesHolder`, `VariableProvider`).
- If proxying the `Map<String, X>` return types misbehaves, wrap each registry in a small named class (e.g. `ItemRegistry`).
- Tests that use real Spring wiring (`AdventureRunSessionFactoryTest`, the browserless view tests) may need a test registration of the session scope.

### 2. One active run per browser session

A session-scoped bean, `ActiveRun`, holds the current `AdventureRunSession` and its owner. The owner is seen by the engine only as a `BooleanSupplier ownerAlive`, so the engine layer stays free of Vaadin types. A run counts as **active** only while `ownerAlive` is true (the owning `AdventureRunView` is attached) and the session is not game over.

**Starting.** `AdventureRunSessionFactory.start(adventureData, ownerAlive)` checks the guard **first**. If a run is active it throws `RunAlreadyActiveException` before touching any state, so the blocked tab cannot disturb the running game. `AdventureRunView` catches it and shows "You already have a game running in another tab." (This check-before-mutate order is the key behaviour change; today `start` mutates the shared engine immediately.)

**Releasing.** A run releases itself:

- when it ends (`quit` / game over);
- when its view detaches (Back, in-app navigation: the UI stays alive but the view goes away);
- when the Vaadin session is destroyed (expiry), which destroys the scoped bean.

Releasing also clears the session's registries so a finished game does not pin its adventure graph in memory until the browser session expires.

**Refresh / crashed tabs.** An F5 refresh creates a new view while the old one may not have detached yet. Because "active" means "owner still attached", the dead view reads as inactive and the new start takes over. A player cannot be locked out by their own refresh.

**Callers.** Only `AdventureRunView` calls `start`. Author Test and player Run share the path and get the same rule.

### 3. System-message overrides

System-message overrides belong to the **adventure definition** (`AdventureData.getSystemMessages()`): they are identical for every player and every saved game of that adventure, and need not be stored in a save. The static is a problem only when *different* adventures run at the same time, because one static slot holds one adventure's overrides (last writer wins).

`SystemMessageKey.defaultText()` is an enum method with about 67 call sites in 30 files; the enum cannot see "which adventure am I in". So the static map becomes a thread-local:

- `defaultText()` reads the thread-local and falls back to the built-in text when nothing is bound.
- `installOverrides(map)` is replaced by `bindOverrides(map)`, returning an `AutoCloseable`. Closing restores whatever was bound before, so nested binds are safe.
- The factory passes the adventure's override map into `AdventureRunSession` at creation. `LoadAdventureAction` no longer installs overrides globally.
- `AdventureRunSession.submit()` binds them with try-with-resources around the whole turn, including the opening `look`, `Parser`'s `SM51` terminator check and the `SM2` output filter, which all run inside a turn.
- Outside a run (the editor views) `defaultText()` returns the true built-in text. This also fixes the existing leak into the editors.
- `LoadAdventureActionTest` currently expects the global install and will be updated.

## Error handling

- A turn that throws still unbinds the overrides (try-with-resources).
- A blocked start, or a start that fails to load, leaves the guard and the registries as they were.

## Testing

- Two simulated Vaadin sessions run different adventures at once with independent location, pocket, variables and output.
- A second start in the same session is blocked and leaves the first game untouched.
- A start succeeds after the owning view detaches (takeover), including the refresh case.
- Overrides from adventure A do not appear in a turn of adventure B; the editor-style lookup outside a run sees built-in text.
- A turn that throws leaves no overrides bound on the thread.
- Existing engine/mapper/view tests pass unchanged apart from `LoadAdventureActionTest` and any test that needs the session scope registered.

## Out of scope: follow-up spec

Save/load (up to 10 slots per player and adventure, best-effort restore, builder-version stamp on `AdventureData`) is designed separately and depends on this isolation.
