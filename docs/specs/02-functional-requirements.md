# 02 — Functional Requirements

## Purpose

This chapter is the source of truth for *what users can do*. It enumerates user
stories grouped by role and codifies the cross-cutting editor navigation contract
that every authoring view must implement.

The acceptance criteria here are deliberately concrete: when a chapter says
"navigates back", a future test or screen review will check that BACK is wired,
not just that the user "returns somehow".

## Cross-cutting requirements

### Authentication

- **Login.** Anonymous users hitting any non-public URL are redirected to
  `/login`. The login form has username and password fields; failed attempts
  log the username at INFO and redirect to `/login?error`.
- **Single session per user.** Logging in from a second browser does not
  invalidate the first session, but only one session is recognised at a time.
- **Remember-me.** A signed cookie keeps the user logged in across browser
  restarts when they tick the "remember me" box.
- **Logout.** `/logout` ends the session and returns the user to `/`.
- **Post-login routing.** After authentication, `RootView` redirects:
  - ADMIN → `/admin/dashboard`
  - AUTHOR → `/author/dashboard`
  - PLAYER → `/player/library`

### Public pages

- `/about` is anonymously accessible and shows the application name, version,
  and credits.
- `/login` and `/logout` are anonymously accessible.
- Static assets under `/VAADIN/**`, `/icons/**`, `/images/**`, `/styles/**`,
  `/frontend/**`, `/favicon.ico`, `/robots.txt`, `/manifest.webmanifest`,
  `/sw.js`, `/offline.html` are anonymously accessible.

### Editor navigation contract

Every `*EditorView` MUST expose the same four-button bar (`ResetBackSaveView`):

| Button | Behaviour |
|--------|-----------|
| **BACK** | Navigate to the parent `*MenuView` (e.g. from `LocationEditorView` back to `LocationsMenuView`). If the form has unsaved changes, prompt with a `ConfirmDialog` before leaving. |
| **SAVE** | Validate via the `Binder`; if invalid, show inline field errors and do nothing else. If valid, persist the entity and stay on the editor. Show a success `Notification`. |
| **CANCEL** | Composite of *Reset* + *BACK*: discard unsaved changes and navigate back. |
| **RESET** | Reload the bound bean from its last saved state, restoring all fields. Stay on the editor. |

Unsaved-change handling is implemented in `BaseEditorView.beforeLeave(...)` and
`AdventuresMainLayout.checkIfUserWantsToLeavePage(...)`.

### Validation feedback

| Severity | Mechanism |
|----------|-----------|
| Field-level (e.g. required field empty, command must have a verb) | Inline error via `Binder` |
| Operation failure (save error, conflict) | `Notification` (toast) |
| Destructive or blocking action (delete word in use, delete location in use) | `Dialog` |

### Confirmation dialogs

- Deleting an `Adventure`, `Location`, `Item`, `Word`, `Command`, `Direction`,
  or `Message` MUST require explicit confirmation.
- Deleting a `Word` that is referenced by any `Command`, `Item`, or `Location`
  MUST be refused with a list of usages (see `WordUsageTracker`,
  `LocationUsageTracker`, `ItemUsageTracker`, `MessageUsageTracker`).

### Page titles

Every routed view sets a title via `@PageTitle` or implements
`HasDynamicTitle`. The active title is rendered into the application header by
`AdventureAppLayout.afterNavigation(...)`.

---

## Role: ADMIN

`@RolesAllowed("ROLE_ADMIN")` (or any view inheriting that, via the role
hierarchy).

### A1. View admin dashboard

- **As** an ADMIN
- **I want** a landing page with quick links to user management, adventure
  assignments, and overall game management
- **So that** I can perform any administrative task in two clicks
- **Acceptance:** `/admin/dashboard` renders the admin dashboard with buttons to
  *User Management*, *Adventure Assignments*, *Game Management*.

### A2. Manage users

- **As** an ADMIN
- **I want** to list, create, edit, enable/disable, and delete users
- **So that** I can grant or revoke access to the platform
- **Acceptance:**
  - `/admin/users` shows a grid of `UserData`: id, username, roles, enabled.
  - "Add New User" opens a form with username, password, role multi-select, enabled.
  - Editing a user opens the same form pre-filled.
  - Saving a user enforces username uniqueness; on conflict, a `Notification` reports it.
  - Disabling a user immediately prevents future logins (Spring Security `isEnabled()`).
  - Deleting a user requires confirmation. Deleting a user that is referenced as
    `AdventureAuthor` or `AdventurePlayer` MUST be refused or cascade per
    [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md#mysql-relationships).

### A3. Manage adventure assignments

- **As** an ADMIN
- **I want** to assign authors and players to specific adventures
- **So that** the right people can edit and play each game
- **Acceptance:**
  - `/admin/adventures/assignments` shows the master list of adventures.
  - For each adventure, the ADMIN can view the current author and player list.
  - Assigning an author replaces the existing author (one-author-per-adventure
    invariant on `AdventureAuthor.adventureId` PK).
  - Assigning a player adds a row to `AdventurePlayer` with composite key
    `(adventureId, userId)`; assigning the same player twice is idempotent.
  - Removing an assignment requires confirmation.

### A4. Inherit author and player capabilities

ADMIN inherits all AUTHOR and PLAYER user stories below, by virtue of the
`ROLE_ADMIN > ROLE_AUTHOR > ROLE_PLAYER` hierarchy.

---

## Role: AUTHOR

`@RolesAllowed("ROLE_AUTHOR")` (also accessible by ADMIN via the hierarchy).

### B1. View author dashboard

- **As** an AUTHOR
- **I want** a landing page with a link to my adventures
- **So that** I can start managing my games
- **Acceptance:** `/author/dashboard` renders with a button leading to
  `/author/adventures`.

### B2. List my adventures

- **As** an AUTHOR
- **I want** to see only adventures I author
- **So that** I do not accidentally edit someone else's content
- **Acceptance:**
  - `/author/adventures` shows a grid of adventures returned by
    `AdventureAccessService.getAdventuresForUser(currentUser)`. ADMINs see
    everything; AUTHORs see what they own; PLAYERs see what they have been
    assigned to.
  - Double-clicking a row navigates to the `AdventureEditorView`; a
    right-click context menu offers **Edit** (same target) and **Delete**
    (see B3).
  - A "Run Adventure" button is enabled only when a row is selected; it
    launches `AdventureRunView` (see [C2](#c2-play-an-adventure)) with this
    adventure loaded.

### B3. Create, edit, delete an adventure

- **Acceptance:**
  - "Create Adventure" navigates to `/author/adventures/new`. Saving creates an
    `AdventureData` in MongoDB AND an `AdventureAuthor` row in MySQL pointing the
    current user at the new adventure id (handled by
    `AdventureAccessService.createAdventure`, `@Transactional` over the JPA write).
  - Editing navigates to `/author/adventures/:adventureId/edit`. Title, notes,
    starting-location reference, and other top-level metadata can be modified.
    This editor's button bar is Back/Run Adventure/Save (not the four-button
    Cancel/Reset/Back/Save contract used elsewhere — see
    [§ Editor navigation contract](#editor-navigation-contract)); **Run Adventure**
    launches `AdventureRunView` in place and is gated on the adventure
    being saved, unchanged since save, and having at least one location.
  - Deleting an adventure (right-click a row → **Delete** on
    `/author/adventures`) removes the `AdventureData` and all owned
    documents (locations, items, vocabulary, messages) via the
    cascade-delete machinery, then removes the `AdventureAuthor` row and
    any `AdventurePlayer` rows. **This happens immediately with no
    confirmation dialog** — unlike location/item/word/message deletion
    elsewhere in the app, which all confirm first.
  - Authors MUST NOT see or edit adventures they do not own
    (`AdventureAccessService.canRead/canWrite`).

### B4. Manage locations

- **Acceptance:**
  - `/author/adventures/:adventureId/locations` lists the adventure's locations.
  - `/author/adventures/:adventureId/locations/:locationId/edit` opens the
    location editor: noun & adjective (vocabulary pickers), short description,
    long description, lumen (light level, integer), commands, directions, items.
  - The map view at `/author/map` visualises the adventure's locations.
  - A location MUST be referenced by the adventure's starting-location id or by
    at least one direction; orphan locations may exist during editing but are
    flagged.
  - Deleting a location is refused if any direction or command targets it.

### B5. Manage items

- **Acceptance:**
  - Two scopes are exposed:
    - `/author/adventures/:adventureId/items` shows every item across all
      locations (`AllItemsMenuView`).
    - `/author/adventures/:adventureId/locations/:locationId/items` shows the
      items in one location (`ItemsMenuView`).
  - The item editor at `…/items/:itemId/edit` (or `…/items/new`) lets the author
    set noun & adjective, short and long description, `isContainable`,
    `isWearable`, parent container.
  - Items can be moved between containers via `MoveItemAction` at runtime; in
    the editor, the parent container is set directly.

### B6. Manage directions

- **Acceptance:**
  - `/author/adventures/:adventureId/locations/:locationId/directions` lists
    exits from a location.
  - The editor at `…/direction/:directionId/edit` (or `…/direction/new`)
    captures the destination location, the verb command (e.g. *north*), and a
    flag for whether the destination must be mentioned in the command.

### B7. Manage commands

- **Acceptance:**
  - `/author/adventures/:adventureId/locations/:locationId/commands` lists the
    location's commands (read-only summary grid); double-click a row, or
    right-click it for **Edit**, to open the full `CommandEditorView`.
  - The editor lets the author build a command from:
    - A `CommandDescription` (verb + optional adjective + optional noun, all
      drawn from the adventure's vocabulary — no free-text entry).
    - An ordered list of PreConditions, combined with AND. There is no
      separate "Not" entry to pick; each precondition row carries a
      **Negate** checkbox that wraps it in a `NotConditionData` when
      checked. There is no And/Or composite in the current data model —
      "either of these" logic is expressed as separate Command Chain
      variants instead (below).
    - An ordered list of Actions, all run in sequence when the command
      fires (no primary/follow-up split at the data level — see
      [`03-domain-model.md` § Commands and chains](03-domain-model.md#commands-and-chains)).
  - **Command Chain.** Multiple commands may share the same
    `CommandDescription`; they form a `CommandChainData` and the engine
    (and the `CommandEditorView`'s "Command Chain" grid) tries them **in
    order**, running the first whose PreConditions all pass. To add a new
    variant to an existing chain, create a new command with the same
    verb/adjective/noun and save; to remove one variant, right-click its
    row in the Command Chain grid.
  - Action sub-editors are pluggable via an annotation-driven registry
    (`@AutoRegisterActionEditor`, discovered by `ActionEditorRegistry`) /
    `ActionSelector`; all 15 authorable action types have editors including
    Message, Describe, Take, Drop, Wear, Remove, MovePlayer, Inventory, Quit,
    LoadAdventure, SetVariable, IncrementVariable, DecrementVariable, Create,
    Destroy, and Break (see
    [`07-ui-and-navigation.md` § Action editor factory](07-ui-and-navigation.md#action-editor-factory)).
  - Condition sub-editors are pluggable the same way
    (`@AutoRegisterConditionEditor` / `ConditionEditorRegistry`) /
    `ConditionSelector`; all 10 selectable condition types have editors,
    including `ChanceCondition` (a random-roll gate). `NotConditionData`
    is applied structurally via the Negate checkbox above rather than
    being one of the 10 selectable kinds.

### B8. Manage messages

- **Acceptance:**
  - `/author/adventures/:adventureId/messages` lists message snippets.
  - The editor lets the author set the message id and body. Messages are
    referenced by id from `MessageAction`.

### B9. Manage vocabulary

- **Acceptance:**
  - `/author/adventures/:adventureId/vocabulary` shows the word list with
    filtering (`WordFilter`).
  - "Create Word" opens `WordEditorDialogue` with text and `Word.Type` (NOUN /
    ADJECTIVE / VERB).
  - A word can be made a synonym of another word; synonyms resolve to the
    canonical form during parsing.
  - When a word is saved with a new synonym, `WordEditorDialogue` detects
    any other words that still point to the old synonym and offers a
    confirmation dialog ("Update All" / "Skip"). The dialog warns when
    synonym adoption would mutate `Word.Type` for the affected words.
  - `/author/adventures/:adventureId/vocabulary/special` is the dedicated editor
    for special-word slots (take, drop, look, examine, inventory, go, help,
    quit, save, load).
  - Deleting a word is refused if any command, item, location, or direction
    references it; the dialog enumerates usages.

### B10. Manage workflow commands

- **As** an AUTHOR
- **I want** to define commands that run automatically every turn,
  regardless of the player's location
- **So that** I can build global mechanics — ambient events, hazards, a
  win/lose check — without repeating a command on every location
- **Acceptance:**
  - `/author/adventures/:adventureId/workflow` (`WorkflowEditorView`)
    lists the adventure's workflow commands and lets the author create,
    edit, and delete them. Reached via **Manage Workflow** on
    `AdventureEditorView`.
  - Each workflow command is built the same way as a location command:
    `CommandDescription` + ordered PreConditions + ordered Actions, using
    the same sub-editor components.
  - **A workflow command with an unmet precondition is not silent.** If a
    precondition (or the command) is set up to report a message on
    failure, that message is shown **every turn** the precondition is
    unmet — it does not skip quietly. The editor's own help text warns of
    this explicitly.
  - There is no Command Chain concept for workflow commands; each one
    stands alone (unlike location commands, which may share a
    `CommandDescription` across chained variants).

### B11. Inherit player capabilities

AUTHORs can also browse and play adventures they have been granted player
access to (or, by hierarchy, all adventures); the player flow is described
below.

---

## Role: PLAYER

`@RolesAllowed("ROLE_PLAYER")` (also accessible by AUTHOR and ADMIN via the
hierarchy).

### C1. Browse the library

- **As** a PLAYER
- **I want** a list of adventures I have access to
- **So that** I can choose what to play
- **Acceptance:** `/player/library` (`PlayerLibraryView`) lists adventures
  returned by `AdventureAccessService.getAdventuresForUser(currentUser)`
  filtered for player access (`AdventurePlayer` rows). A "Run Adventure"
  button is enabled once a row is selected; double-clicking a row does the
  same thing directly.

### C2. Play an adventure

The in-browser play surface is implemented: `AdventureRunView`, backed by
`AdventureRunSession` / `AdventureRunSessionFactory`
(`server/engine/`), drives the same `GameLoop` / `GameContext` the CLI
runner (`AdventureClient` / `MiniAdventure`) uses. It is reached three
ways — a player's "Run Adventure" here, an author's "Run Adventure" from
`/author/adventures`, or an author's "Run Adventure" from `AdventureEditorView` —
all landing on the identical view; only the **Back** destination and the
page title ("Playing: …" vs "Running: …") differ by origin.

- **As** a PLAYER
- **I want** to type natural verb-noun commands and see the game respond
- **So that** I can experience the adventure
- **Acceptance:**
  - Entering the view auto-submits `look`, so the current location's long
    description renders immediately, spoken by "Narrator" in a
    `MessageList` transcript. A `MessageInput` below accepts further
    commands, each echoed under the player's own username before the
    response renders.
  - Input runs through `Parser` → `GameLoop.processCommand` →
    `CommandExecutor` (workflow interceptors first, then pocket/location
    dispatch), and the resulting messages are appended to the transcript.
  - The core verbs `look` (+ `l`/`desc`/`examine`/`x`), `inventory` (+
    `i`), `help`, and `quit` (+ `exit`/`bye`) are always available,
    registered directly by `AdventureRunSessionFactory` — independent of
    the adventure's own vocabulary/special-word setup. `take`/`get`,
    `drop`, `wear`, `remove` work only for items the author has actually
    made containable/wearable.
  - Re-renders the current location whenever the player moves
    (`MovePlayerAction`).
  - On `quit`, the session ends (input disabled, a farewell line shown);
    the player then clicks **Back** to return to the library (or, for an
    author, to wherever they launched from).
  - `save`/`load` are **not** wired in a run session — it is scoped to one
    adventure, played in one sitting. (The CLI's `load <adventureId>`,
    which raises `ReloadAdventureException` to restart the loop with a
    different adventure, remains a console-only capability — see
    [Known gaps](#known-gaps).)

> **Constraint inherited from the engine, not new to this view:**
> `AdventureRunSessionFactory` reuses the same process-wide
> `GameContext`/`AdventureConfig` singleton beans the console runner uses
> (no per-session engine isolation), so at most one run session is
> meaningfully active at a time across the whole server — the same
> constraint `MiniAdventure` already has on the console. Concurrent
> Test/Run sessions (two authors testing at once, or two browser tabs)
> will interfere with each other. See
> [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps).

---

## Functional invariants

These are *system-level* invariants that emerge from the user stories and
should be enforced by the implementation, not just by the UI:

1. **Ownership.** Every adventure has exactly one row in `AdventureAuthor`
   (PK on `adventureId` enforces this). A user cannot be an author of an
   adventure they do not own.
2. **Read access.** A user MAY read an adventure iff they are ADMIN, **or**
   they author it (`AdventureAuthor`), **or** they have a row in
   `AdventurePlayer` for it. The check lives in
   `AdventureAccessService.canRead`.
3. **Write access.** A user MAY write an adventure iff they are ADMIN **or**
   they author it. Players never write. Check in
   `AdventureAccessService.canWrite`.
4. **Vocabulary uniqueness.** Within an adventure, two words MUST NOT have the
   same `(text, type)` pair. The `WordEditorDialogue` and
   `SpecialWordsView.checkIfValueAlreadyExists` enforce this.
5. **Synonym chains terminate.** A word's synonym chain MUST resolve to a
   non-synonym; cycles are rejected at save time.
6. **Container capacity.** Adding to a container at capacity raises
   `ContainerFullException`. Items that are not `Containable` raise
   `NotContainableException`.
7. **Worn items are carried.** Dropping a worn item triggers a follow-up
   `RemoveAction` (built by `CommandFactory.setUpDropCommand`).
8. **Special words exist.** Each adventure's `VocabularyData` MUST have a
   non-null reference for every special-word slot before play begins.

## Source pointers

- `src/main/java/com/pdg/adventure/view/RootView.java`
- `src/main/java/com/pdg/adventure/view/login/LoginView.java`,
  `src/main/java/com/pdg/adventure/view/login/LogoutView.java`
- `src/main/java/com/pdg/adventure/view/component/BaseEditorView.java`,
  `src/main/java/com/pdg/adventure/view/component/ResetBackSaveView.java`
- `src/main/java/com/pdg/adventure/view/admin/{AdminDashboardView,UserManagementView,AdventureAssignmentView}.java`
- `src/main/java/com/pdg/adventure/view/author/AuthorDashboardView.java`
- `src/main/java/com/pdg/adventure/view/adventure/{AdventuresMenuView,AdventureEditorView}.java`
- `src/main/java/com/pdg/adventure/view/location/{LocationsMenuView,LocationEditorView,LocationMapView}.java`
- `src/main/java/com/pdg/adventure/view/item/*`
- `src/main/java/com/pdg/adventure/view/direction/*`
- `src/main/java/com/pdg/adventure/view/command/*`
- `src/main/java/com/pdg/adventure/view/message/*`
- `src/main/java/com/pdg/adventure/view/vocabulary/*`
- `src/main/java/com/pdg/adventure/view/player/PlayerLibraryView.java`
- `src/main/java/com/pdg/adventure/view/adventure/AdventureRunView.java`
- `src/main/java/com/pdg/adventure/view/workflow/{WorkflowMainLayout,WorkflowEditorView}.java`
- `src/main/java/com/pdg/adventure/server/engine/{AdventureRunSession,AdventureRunSessionFactory}.java`
- `src/main/java/com/pdg/adventure/server/security/service/AdventureAccessService.java`

## Known gaps

- **Single active run session, server-wide.** `AdventureRunSessionFactory`
  reuses the process-wide `GameContext`/`AdventureConfig` singletons, so
  only one Test/Run session is meaningfully active at a time across the
  whole deployment — the same constraint the CLI runner already had, now
  more visible because multiple browser users can trigger it
  concurrently. A rebuild SHOULD give each session its own engine state
  (request- or session-scoped `GameContext`) if concurrent play is a
  requirement.
- **No save/load within a run session.** `save`/`load` special-word slots
  exist on `VocabularyData` but are not wired into `AdventureRunView` —
  a session runs start-to-finish in one sitting. Cross-adventure
  `load <id>` (via `ReloadAdventureException`) remains console-only.
- **AI-augmented descriptions.** Authors cannot yet ask the system to enrich a
  description; `DescribeAction` has the integration code commented out.
- **Self-service signup.** No public registration view; ADMIN must create users
  manually.
- **Audit log.** No record of who changed what when; only Spring Data's
  `@CreatedDate` / `@LastModifiedDate` on `DatedData`.
