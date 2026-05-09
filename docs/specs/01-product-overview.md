# 01 — Product Overview

## Purpose

This chapter establishes *what* Adventure Builder is, *who* uses it, and *what* the
top-level capabilities are. It does not prescribe implementation; later chapters do.

## Vision

Adventure Builder is a **web platform for authoring and running text-based adventure
games**. A small team of authors creates self-contained adventures by composing
locations, items, vocabulary, commands, and messages in a browser. Players sign in
to a library of published adventures and play them turn-by-turn through a parser.

The platform is single-deployment and multi-user, with a strict role hierarchy.
Production-grade payments, marketplaces, social features, and an external API are
deliberately out of scope for the present specification.

## Target users and roles

The system has exactly three roles, in a strict hierarchy:

```
ROLE_ADMIN   ⊃   ROLE_AUTHOR   ⊃   ROLE_PLAYER
```

(`⊃` reads "inherits all permissions of".)

| Role | Persona | Primary jobs |
|------|---------|--------------|
| **ADMIN** | The platform operator. | Manage users (create/disable, assign roles); assign authors and players to specific adventures; oversight. Inherits AUTHOR and PLAYER capabilities. |
| **AUTHOR** | A game designer. | Create, edit, and delete adventures they author. Manage every nested concept inside their adventure: locations, items, directions, commands, messages, vocabulary. Inherits PLAYER capabilities. |
| **PLAYER** | An end user. | Browse the library of adventures they have been assigned to, and play them. |

Role inheritance is enforced both at the URL level (Spring Security
`RoleHierarchy` bean) and at the view level (`@RolesAllowed`); see
[`06-security-and-access-control.md`](06-security-and-access-control.md).

A bootstrap admin (`admin` / `admin123`) is seeded by `DataInitializer` if the
users table is empty. This is a development convenience; a production rebuild
MUST replace it with an environment-supplied secret before first deployment.

## Top-level capabilities

The product provides the following capabilities. Each capability is expanded into
user stories in [`02-functional-requirements.md`](02-functional-requirements.md)
and into UI structure in [`07-ui-and-navigation.md`](07-ui-and-navigation.md).

### A. Authentication and session

- Form-based login at `/login`.
- Single active session per user.
- Optional remember-me cookie, signed with a configured key.
- Role-aware post-login routing: `RootView` redirects each user to their highest-
  privilege dashboard.

### B. User and access administration (ADMIN)

- CRUD over users (create, set roles, enable/disable).
- Assign authors and players to specific adventures (one author per adventure;
  many players per adventure).
- View overall adventure list across all authors.

### C. Adventure authoring (AUTHOR)

- Create, edit, and delete adventures the author owns.
- For each adventure, manage:
  - **Locations** — name, descriptions, light level, items, directions, commands.
  - **Items** — name, descriptions, container/wearable flags, parent container.
  - **Vocabulary** — nouns, adjectives, verbs, synonyms, and the special verbs
    (take, drop, look, examine, inventory, go, help, quit, save, load).
  - **Directions** — exits between locations, gated by a command.
  - **Commands** — verb/adjective/noun triggers that run Actions when their
    PreConditions hold; chained follow-up Actions are supported.
  - **Messages** — reusable text snippets emitted by `MessageAction`.
- Visualise the adventure as a location map (`LocationMapView`).
- Save/Reset/Cancel/Back semantics consistent across all editor views (see
  [`02-functional-requirements.md`](02-functional-requirements.md#editor-navigation-contract)).

### D. Adventure play (PLAYER)

- Browse a library of adventures the player has been granted access to.
- Launch and play an adventure turn-by-turn through a parser interface that
  accepts verb [adjective] noun input, processes a sentence, runs PreConditions,
  executes the matching Action, and emits text output.

### E. Author tooling

- Vocabulary picker components prefilled with the adventure's words.
- Word-usage tracking: every vocabulary word, item, and location can show the
  list of commands and other entities that reference it (`*UsageTracker`).
- Confirmation dialogs for destructive operations and unsaved-change checks
  before navigating away from an editor.

### F. Operability

- Embedded Mongo for tests; Dockerised Mongo and MySQL for local development.
- Vaadin frontend hot-deploy in dev; ahead-of-time compiled frontend in
  production (`-Pproduction`).
- JaCoCo coverage at `target/site/jacoco/`.
- OWASP dependency-check plugin (`mvn dependency-check:check`).
- GitHub Actions CI on push to `main` with SonarQube.

## What the product is **not**

| Not in product | Why it matters here |
|----------------|--------------------|
| A REST or GraphQL API for external clients | All interaction is through Vaadin views; do not introduce DTOs or controllers expecting external consumers. |
| A marketplace, payments, or licensing layer | The library shows assigned adventures; there is no purchase flow. |
| Real-time collaboration on the same adventure | Editor views are single-author; concurrent edits are not designed for. |
| Social features (comments, ratings, profiles) | Out of scope. |
| Self-service signup | Users are created by an ADMIN. There is no public registration page. |
| Email or notifications | The system never sends mail. |

## In-scope but explicitly aspirational

These are referenced by the current code but not yet wired up. They appear in
"Known gaps" sections of the relevant chapters and are consolidated in
[`09-rebuild-blueprint.md` § Roadmap](09-rebuild-blueprint.md#roadmap--known-gaps):

- **AI-augmented descriptions.** `DescribeAction` contains commented-out Spring AI /
  Ollama wiring; the long-term intent is to allow authors to enrich location and
  item descriptions through a configured LLM. The spec describes the integration
  shape; a rebuild MAY ship it as a feature flag.
- **NLP command parser.** The current parser is vocabulary-token based. The
  product intends a richer parser eventually; the spec leaves this as a clearly
  pluggable interface.
- **Module split.** The current `server` module is intended to be carved into
  `backend`, `editor`, `player`, and `api` Maven modules. The spec is written so
  that this split is a refactor, not a redesign.

## Glossary

This is the canonical list. Other chapters reference it.

| Term | Definition |
|------|-----------|
| **Adventure** | The root aggregate. A self-contained game: title, starting location, set of locations, vocabulary, message catalog, and the player's pocket (a container). One owner (`AdventureAuthor`); zero-to-many players (`AdventurePlayer`). |
| **Location** | A `Thing` extended with directions and an item container. Has a *lumen* (light level) and a visit counter. Players move between locations through directions. |
| **Direction** | An exit from one location to another. Carries its own `Command` so the verb that triggers it (e.g. *north*, *enter*, *climb*) is data, not hard-coded. |
| **Item** | A `Thing` that is `Containable` (can be placed in a container) and optionally `Wearable`. May be in a container or in a location. |
| **Container** | A `Thing` that owns a list of `Containable`s with a capacity. The player's *pocket* is a container; locations have a container; items can be containers. |
| **Thing** | Base abstract for every described object: holds a description provider and a map of commands. Both `Location` and `Item` extend it. |
| **Vocabulary** | The dictionary an adventure understands. A `Vocabulary` wraps a `VocabularyData` and exposes lookup, synonym creation, and the special-word slots. |
| **Word** | A string + a `Word.Type` (NOUN, ADJECTIVE, VERB) + an optional synonym pointing at the canonical word. |
| **Special words** | The vocabulary entries used by the engine for built-in mechanics: `take`, `drop`, `inventory`, `look`, `examine`, `go`, `help`, `quit`, `save`, `load`. |
| **Command** | A unit composed of a `CommandDescription` (verb/adjective/noun), a list of `PreCondition`s, an `Action`, and a list of follow-up `Action`s. |
| **CommandDescription** | A 3-slot tuple `(verb, adjective?, noun?)` produced by the parser and used by the matcher to find a `Command`. |
| **Action** | A side-effect executed when a Command's PreConditions all pass. Returns an `ExecutionResult`. 16 concrete kinds (see [`04-runtime-engine.md`](04-runtime-engine.md#action-catalog)). |
| **PreCondition** | A boolean predicate evaluated in the current `GameContext`; gates an Action. 12 concrete kinds (see [`04-runtime-engine.md`](04-runtime-engine.md#precondition-catalog)). |
| **GameContext** | The runtime carrier: current location, player pocket, message holder, workflow, variable provider, IO. |
| **Workflow** | A list of *global* commands processed before location-scoped commands. Holds inventory, quit, help, load and any other engine-level Commands. |
| **Variable** | A named integer/value tracked in the `VariableProvider`; readable/writable by Actions and PreConditions. |
| **Message** | A reusable text snippet keyed by ID; emitted via `MessageAction`. |
| **Mapper** | A `Mapper<DO, BO>` bidirectional translator. Auto-registered into `MapperSupporter` by an `@AutoRegisterMapper` annotation processed by a `BeanPostProcessor`. |
| **MapperSupporter** | The central registry of mappers and shared collections (locations, items, containers, vocabulary, messages, variables) used during BO ↔ DO conversion. |
| **AdventureAuthor / AdventurePlayer** | JPA rows linking a `UserData` (MySQL) to an `AdventureData` (MongoDB) by adventure ID. Implements ownership and access rights respectively. |

## Source pointers

- `src/main/java/com/pdg/adventure/AdventureBuilderServer.java` — entry point and `@PWA`.
- `src/main/java/com/pdg/adventure/security/model/Role.java` — the three role enum values.
- `src/main/java/com/pdg/adventure/config/SecurityConfig.java` — role hierarchy bean.
- `src/main/java/com/pdg/adventure/config/DataInitializer.java` — seeded admin.
- `src/main/java/com/pdg/adventure/view/RootView.java` — post-login dispatcher.
- `src/main/java/com/pdg/adventure/server/Adventure.java` — root business object.
- `src/main/java/com/pdg/adventure/model/AdventureData.java` — root persistence document.

## Known gaps

- The bootstrap admin password (`admin123`) is hardcoded in
  `config/DataInitializer.java:30`. A production rebuild MUST source it from
  configuration. See [`06-security-and-access-control.md`](06-security-and-access-control.md#known-gaps).
- `AdventureBuilderServer` has its `@SpringBootApplication` annotation
  commented out (`AdventureBuilderServer.java:12`). The application currently
  works because of Vaadin's Spring Boot starter; a rebuild SHOULD restore
  `@SpringBootApplication` for clarity and to enable component scanning of
  packages outside the auto-discovered set.
