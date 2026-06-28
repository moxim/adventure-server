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
  - Double-clicking a row navigates to the `AdventureEditorView`.
  - A "Run Adventure" button is enabled only when a row is selected.

### B3. Create, edit, delete an adventure

- **Acceptance:**
  - "Create Adventure" navigates to `/author/adventures/new`. Saving creates an
    `AdventureData` in MongoDB AND an `AdventureAuthor` row in MySQL pointing the
    current user at the new adventure id (handled by
    `AdventureAccessService.createAdventure`, `@Transactional` over the JPA write).
  - Editing navigates to `/author/adventures/:adventureId/edit`. Title, notes,
    starting-location reference, and other top-level metadata can be modified.
  - Deleting an adventure removes the `AdventureData` and all owned documents
    (locations, items, vocabulary, messages) via the cascade-delete machinery,
    then removes the `AdventureAuthor` row and any `AdventurePlayer` rows.
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
    location's commands.
  - The editor lets the author build a command from:
    - A `CommandDescription` (verb + optional adjective + optional noun, all
      drawn from the adventure's vocabulary).
    - A list of PreConditions (`NotCondition` composite available; no And/Or
      composites exist in the current data model).
    - One primary Action.
    - Zero or more follow-up Actions.
  - Action sub-editors are pluggable via `ActionEditorFactory` /
    `ActionSelector`; all 15 authorable action types have editors (see
    [`07-ui-and-navigation.md` § Action editor factory](07-ui-and-navigation.md#action-editor-factory)).
  - Condition sub-editors are pluggable via `ConditionEditorFactory` /
    `ConditionSelector`; 9 of the 10 condition types have editors
    (`NotConditionData` is the remaining gap).

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

### B10. Inherit player capabilities

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
- **Acceptance:** `/player/library` lists adventures returned by
  `AdventureAccessService.getAdventuresForUser(currentUser)` filtered for
  player access (`AdventurePlayer` rows). Each row offers a "Play" button.

### C2. Play an adventure (target state)

The current code ships a CLI runner (`AdventureClient` / `MiniAdventure`) and
the engine is fully functional; the in-browser play surface is partly built.
The product target is:

- **As** a PLAYER
- **I want** to type natural verb-noun commands and see the game respond
- **So that** I can experience the adventure
- **Acceptance:**
  - Selecting "Play" launches an interactive view that:
    - Shows the current location's long description on entry.
    - Provides a single text input for commands.
    - Echoes the command back, runs it through `Parser` →
      `CommandHandler` → `CommandExecutor`, and renders the resulting messages.
    - Re-renders the current location whenever the player moves
      (`MovePlayerAction`).
    - Handles the special verbs: `inventory`, `quit`, `help`, `look`,
      `examine`, `take`, `drop`, `wear`, `remove`, plus `save`/`load` for
      persistence (these slots exist on `VocabularyData`).
    - On `quit`, returns the player to the library.
    - On `load <adventureId>`, the engine raises `ReloadAdventureException` and
      restarts with the chosen adventure.

> See [`Known gaps`](#known-gaps) — the in-browser player surface is the largest
> partly-built piece. The runtime engine itself does not need rebuilding;
> [`04-runtime-engine.md`](04-runtime-engine.md) describes its contract.

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
- `src/main/java/com/pdg/adventure/server/security/service/AdventureAccessService.java`

## Known gaps

- **Player play surface.** No `*PlayView` is wired up yet. The CLI
  `AdventureClient` is the manual-test path. A rebuild MUST add an interactive
  Vaadin view that drives `Parser` and `CommandHandler` and renders the
  resulting messages from `MessagesHolder`.
- **AI-augmented descriptions.** Authors cannot yet ask the system to enrich a
  description; `DescribeAction` has the integration code commented out.
- **Self-service signup.** No public registration view; ADMIN must create users
  manually.
- **Audit log.** No record of who changed what when; only Spring Data's
  `@CreatedDate` / `@LastModifiedDate` on `DatedData`.
