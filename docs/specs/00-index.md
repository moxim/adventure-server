# Adventure Builder — Specification Suite

This directory contains the rebuild-from-scratch specification for the **Adventure Builder
server** module. Each chapter is a self-contained markdown document that focuses on one
concern. Together they describe the product, its domain, its runtime, its persistence,
its security, its UI, and its build/test/operational envelope in enough detail that a
fresh team can recreate the system on the same technology stack.

## Audience

A working software team that is going to rebuild the Adventure Builder server in the
same stack: **Spring Boot 4.1, Java 25, Vaadin 25, MongoDB, MySQL** (see
[`08-build-test-and-ops.md`](08-build-test-and-ops.md) for exact versions). Engineering
guidance for AI agents working in the existing codebase lives in the repo-root
`AGENTS-*.md` files; the present specifications are upstream of those — they describe
*what* and *why*, not *how to keep editing the current code*.

## Scope

In scope:

- Everything under `server/src/main` and `server/src/test`.
- The `server/pom.xml` build, the `server/.github/workflows/build.yml` CI pipeline,
  and the `server/adventureDb/` Docker compose files.
- The shared API package living **inside** `server/src/main/java/com/pdg/adventure/api/`.

Explicitly out of scope:

- The repo-root `editor/` Maven module — currently a stub with no source.
- The repo-root `api/` Maven module — currently a placeholder; the active interfaces
  live inside `server/src/main/java/com/pdg/adventure/api/` and are covered here.
- Any future split of `server` into `backend` / `editor` / `player` / `api` modules
  (mentioned only as a roadmap item in [`09-rebuild-blueprint.md`](09-rebuild-blueprint.md)).

## Reading order

| # | Chapter | Read this when you need to know… |
|---|---------|----------------------------------|
| [01](01-product-overview.md) | Product overview | What the product is, who uses it, what the ADMIN/AUTHOR/PLAYER roles can do, and the glossary. |
| [02](02-functional-requirements.md) | Functional requirements | The user stories per role and the editor navigation contract (BACK/SAVE/CANCEL/RESET, unsaved-change handling). |
| [03](03-domain-model.md) | Domain model | The Adventure aggregate, every business object and persistence document, and how they relate. |
| [04](04-runtime-engine.md) | Runtime engine | How a typed command becomes a parsed sentence, gets routed through the engine, and is dispatched to an Action. Catalog of all 16 Actions and 12 PreConditions. |
| [05](05-persistence-and-mappers.md) | Persistence & mappers | MongoDB collections, MySQL schema, the cascade-save/delete machinery, and the auto-registered mapper layer. |
| [06](06-security-and-access-control.md) | Security & access control | Spring Security wiring, role hierarchy, URL guards, login flow, the `AdventureAccessService` rules, and seeded users. |
| [07](07-ui-and-navigation.md) | UI & navigation | The full Vaadin view tree, the `@Route` × role matrix, layouts, reusable components, and the view-model pattern. |
| [08](08-build-test-and-ops.md) | Build, test & ops | Maven profiles, dependency choices, Docker compose for the two databases, CI, OWASP, JaCoCo, the testing strategy. |
| [09](09-rebuild-blueprint.md) | Rebuild blueprint | A linear bootstrap order plus the consolidated roadmap & known-gaps log distilled from every chapter. |

If you are rebuilding from a blank repo, read 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08
in order, then use 09 as your build checklist. If you are auditing an in-progress
rebuild, jump straight to the relevant chapter.

## Conventions used across the spec

- **`*` / `*Data` / `*Mapper` / `*View`** — class-name suffixes are normative; see
  [`03-domain-model.md` § Naming](03-domain-model.md#naming-conventions).
- **BO vs DO** — *Business Object* (runtime model, stateful, e.g. `Location`) and
  *Data Object* (persistence document or JPA entity, e.g. `LocationData`).
- **Citations** — every chapter ends with a *Source pointers* section listing the
  files in `server/src` that informed it. File paths are written relative to the
  `server/` directory. Where useful, line numbers are appended (`File.java:NN`).
- **Known gaps** — every chapter has a *Known gaps* section that flags the things
  the current code has not finished. The specifications describe the **intended
  finished product**; gaps are called out so a rebuilder does not re-introduce them.
- **MUST / SHOULD / MAY** — RFC 2119 keywords are used sparingly, only where a
  decision is normatively required (e.g. role hierarchy, mapper registration).

## Glossary (quick lookup)

The full glossary lives in [`01-product-overview.md`](01-product-overview.md#glossary).
This list is just the most-cross-referenced terms.

| Term | One-line definition |
|------|---------------------|
| **Adventure** | The root aggregate; a single text-adventure game with its locations, items, vocabulary, and messages. |
| **Location** | A visitable place in the game world; owns items and exits. |
| **Item** | A `Containable` `Thing`; can be carried, worn, dropped, placed in containers. |
| **Container** | A `Thing` that holds `Containable`s; capacity-limited. The player's *pocket* is a container. |
| **Direction** | An exit from a location; resolved by a verb-noun command. |
| **Vocabulary** | The set of `Word`s an adventure understands, plus the canonical *special verbs* (take, drop, look, etc.). |
| **Command** | A vocabulary-described trigger that, when matched, runs an `Action` after passing all `PreCondition`s. |
| **Action** | An executable side-effect (move player, take item, set variable, …). 16 concrete kinds today. |
| **PreCondition** | A boolean predicate gating an Action (carried, here, worn, variable comparisons, and/or/not composites). 12 concrete kinds today. |
| **Mapper** | A bidirectional translator between a `*Data` document and a business object. Auto-registered via `@AutoRegisterMapper`. |
| **Workflow** | The engine subsystem that runs *global* commands (inventory, quit, help, load) before location-scoped commands. |

## Cross-cutting non-goals

Stated once here so chapters need not repeat them:

- **No public API surface beyond Vaadin views.** There is no REST API contract today;
  the system is a server-rendered web application.
- **No background jobs or schedulers.** All work is request-scoped or in-process.
- **No multi-tenancy.** A single deployment serves a single user base; `Adventure`
  ownership is enforced via `AdventureAuthor` / `AdventurePlayer` JPA rows.
- **No production hardening claims.** The current default admin password and
  remember-me key are development values; `06-security-and-access-control.md`
  enumerates what a production deployment needs to change.
