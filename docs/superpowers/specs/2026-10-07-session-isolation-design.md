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

**Mechanics (checked while planning):**

- `@VaadinSessionScope` is just `@Scope("vaadin-session")` with **no proxy mode**, so it cannot be used as is. A small composed annotation, `@PerBrowserSession` (`@Scope(value = "vaadin-session", proxyMode = TARGET_CLASS)`), is used instead; it targets the same Vaadin-registered scope.
- The `Map<String, X>` registry beans get an interface (JDK) proxy from Spring; no wrapper classes are needed.
- No `final` methods exist on `GameContext`, `Vocabulary`, `BasicData`, `MessagesHolder` or `VariableProvider`, so CGLIB proxies intercept every call.
- No startup, async or scheduled code dereferences these beans (`DataInitializer`, `AutoMapperRegistrationProcessor` and the `ai` package do not touch them), so no code path runs outside a bound Vaadin session. A call on a proxy outside a bound session throws; that is accepted.
- Tests that build a plain Spring context need the scope registered: a test-only `FakeSessionScopeConfig` provides a switchable fake "vaadin-session" scope. It is also the vehicle for the two-sessions isolation tests.
- Vaadin's docs say scoped beans must be serializable only when sessions are persisted (e.g. Kubernetes Kit). Session persistence is a non-goal (see above).

### 2. One active run per browser session

A session-scoped bean, `ActiveRun`, holds the current `AdventureRunSession` and its owner. The owner is a small Vaadin-free handle, `RunOwner`, that the view creates and flips to "gone" when it detaches (a flag set in `onDetach`; `isAttached()` cannot be used because a view is not attached yet while `beforeEnter` runs). A run counts as **active** only while its `RunOwner` is not gone and the session is not game over.

**Starting.** `AdventureRunSessionFactory.start(adventureData, owner)` checks the guard **first**. If a run is active it throws `RunAlreadyActiveException` before touching any state, so the blocked tab cannot disturb the running game. (This check-before-mutate order is the key behaviour change; today `start` mutates the shared engine immediately.)

**Takeover (decided with the user).** Vaadin detects dead UIs only through missed heartbeats (5-minute interval, a UI expires after 3 missed ones), so a refreshed or crashed tab can still look alive for roughly 15 minutes. A pure block would lock a player out of their own game after F5. Therefore the view, on `RunAlreadyActiveException`, opens a `ConfirmDialog`: "You already have a game running in another tab." with **"End the other game and start here"** and **"Back"**. Confirming calls `startReplacingActive(adventureData, owner)`, which marks the old session as superseded (its next `submit` returns "This game was ended in another tab." with `gameOver = true`, which disables that tab's input) and then starts normally. Nothing is ever lost silently. If the takeover's load itself fails the old game stays ended; that is the price of an explicit takeover.

**Releasing.** `factory.release(owner)` clears the guard and the session's registries **only if `owner` still owns the active run**. A late detach of an old view after a newer run started is therefore a no-op and cannot wipe the new game. It is called:

- when the run ends (`quit` / game over);
- when the owning view detaches (Back, in-app navigation: the UI stays alive but the view goes away);
- and the scoped bean is destroyed with the Vaadin session (expiry).

Clearing the registries (locations, items, containers, messages, variables, vocabulary) stops a finished game pinning its adventure graph until the browser session expires.

**Callers.** Only `AdventureRunView` calls `start`. Author Test and player Run share the path and get the same rule.

### 3. System-message overrides

System-message overrides belong to the **adventure definition** (`AdventureData.getSystemMessages()`): they are identical for every player and every saved game of that adventure, and need not be stored in a save. The static is a problem only when *different* adventures run at the same time, because one static slot holds one adventure's overrides (last writer wins).

`SystemMessageKey.defaultText()` is an enum method with about 67 call sites in 30 files; the enum cannot see "which adventure am I in". So the static map becomes a thread-local:

- `defaultText()` reads the thread-local and falls back to the built-in text when nothing is bound.
- `installOverrides(map)` is replaced by `bindOverrides(map)`, returning an `AutoCloseable`. Closing restores whatever was bound before, so nested binds are safe.
- The factory passes the adventure's override map into `AdventureRunSession` at creation. `LoadAdventureAction` no longer installs overrides globally.
- `AdventureRunSession.submit()` binds them with try-with-resources around the whole turn, including `Parser`'s `SM51` terminator check and the `SM2` output filter, which both run inside a turn.
- **The opening room does not go through `submit()`.** `AdventureRunView` executes a `MovePlayerAction` directly. So the session also exposes `runBound(Supplier)`, which binds the same overrides, and the view renders the opening room through it.
- Mapping/loading does not need the overrides: a search of the mappers, `CommandFactory` and `Adventure` found no override text captured at map time, so `start()` itself does not bind them.
- Outside a run (the editor views) `defaultText()` returns the true built-in text. This also fixes the existing leak into the editors.
- No existing test references `installOverrides`, so removing it needs no test migration.

## Error handling

- A turn that throws still unbinds the overrides (try-with-resources).
- A blocked start, or a start that fails to load, leaves the guard and the registries as they were.

## Testing

- Two fake sessions (switchable scope) hold independent `GameContext` fields and independent registries.
- A second start in the same session is blocked and leaves the first game untouched (the mapper is not called again).
- A start succeeds after the owning view is marked gone.
- `startReplacingActive` supersedes the old session; its next `submit` reports the game as ended elsewhere.
- Release by a stale owner after a newer run started does not clear the newer run's registries.
- Overrides from adventure A do not appear in a turn of adventure B; a lookup outside a run sees built-in text; the opening room (`runBound`) sees the overrides.
- A turn that throws leaves no overrides bound on the thread.
- The view shows the takeover dialog on conflict, takes over on confirm, and releases on detach and on game over.
- Existing tests keep passing; the plain-Spring-context tests register the fake session scope, and `AdventureRunViewTest` / `AdventureRunSessionFactoryTest` are updated for the new `start` signature.

## Out of scope: follow-up spec

Save/load (up to 10 slots per player and adventure, best-effort restore, builder-version stamp on `AdventureData`) is designed separately and depends on this isolation.
