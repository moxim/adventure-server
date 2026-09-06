# 09 — Rebuild Blueprint

## Purpose

This chapter is the linear bootstrap order for re-creating the Adventure
Builder server module from an empty repository. Every step references the
chapter that contains the contract. It also consolidates the
**roadmap & known gaps** surfaced throughout the suite so a rebuilder does
not re-introduce the gaps unintentionally.

If you only have ten minutes, read this chapter; the others are the
detailed contracts that this one orchestrates.

## Bootstrap order

The order matters: each step depends on the previous one. Take them as a
checklist; do not skip ahead.

### Step 1 — Project skeleton and Maven

1. Create the `server/` Maven module with packaging `jar`, parent
   `org.springframework.boot:spring-boot-starter-parent:4.1.0-M4`,
   `<java.version>25</java.version>`.
2. Add the repository declarations: Maven Central, the two pdg Nexus
   repositories, Vaadin Directory, Spring Milestones (and a pluginRepository
   for `vaadin-prereleases`). Reference values: see
   [`08-build-test-and-ops.md` § Repositories](08-build-test-and-ops.md#repositories).
3. Add the dependency set described in
   [`08-build-test-and-ops.md` § Stack of record](08-build-test-and-ops.md#stack-of-record).
   Pay attention to the `spring-boot-grpc-test` exclusion under
   `spring-boot-starter-test-classic`.
4. Configure plugins: `spring-boot-maven-plugin` (with `wait=500`,
   `maxAttempts=240`), `vaadin-maven-plugin` (`frontendHotdeploy=true`,
   `prepare-frontend` at `process-resources`), Surefire, Failsafe, JaCoCo
   (three executions: prepare-agent / prepare-agent-integration / report).
5. Define the four profiles: `dev`, `production`, `it`, `github`. The `it`
   profile is the load-bearing one — it starts and stops the Spring Boot
   app around Failsafe and injects the Mockito 2.23.4 javaagent via a
   `@{argLine}` Surefire `argLine`.
6. Set `<defaultGoal>spring-boot:run</defaultGoal>`.

### Step 2 — Application properties and Docker DBs

1. Author `src/main/resources/application.properties` per
   [`05-persistence-and-mappers.md` § Connection settings](05-persistence-and-mappers.md#connection-settings).
   Both Mongo and MySQL connection strings, Hibernate `ddl-auto=update`,
   Vaadin hot-deploy, devtools poll/quiet periods, the remember-me key
   placeholder, and the Ollama base URL.
2. Author `src/test/resources/application.properties` with embedded Mongo
   (`spring.mongodb.database=test`,
   `de.flapdoodle.mongodb.embedded.version=7.1.0`) and
   `junit-platform.properties` selecting `ClassOrderer$OrderAnnotation`.
3. Author the two `adventureDb/` Docker compose files:
   `Dockerfile.mongodb.yaml` (mongo 8.0.0 + mongo-express on 8081) and
   `Dockerfile.mysql.yaml` (mysql 9.6.0 + adminer on 8881).
4. Run `docker compose -f adventureDb/Dockerfile.mongodb.yaml up -d` and
   the equivalent for MySQL; verify connectivity.

### Step 3 — JPA security model and Spring Security

1. Implement the JPA entities in `security/model/` per
   [`03-domain-model.md` § Security entities](03-domain-model.md#security--access-control-entities)
   and [`05-persistence-and-mappers.md` § MySQL schema](05-persistence-and-mappers.md#mysql-schema):
   - `Role` enum (`ADMIN`, `AUTHOR`, `PLAYER`).
   - `UserData implements UserDetails` with ULID `@PrePersist`, unique
     `username`, `@ElementCollection(EAGER)` of roles, `isEnabled` flag.
   - `AdventureAuthor` with `adventureId` PK and `@ManyToOne UserData`.
   - `AdventurePlayer` with `@EmbeddedId AdventurePlayerId(adventureId, userId)`
     and `@MapsId @ManyToOne UserData`.
2. Add the JPA repositories in `server/security/repository/`:
   `UserRepository` (with `findByUsername`, `findByRolesContaining`),
   `AdventureAuthorRepository`, `AdventurePlayerRepository`.
3. Add the security services in `server/security/service/`:
   `CustomUserDetailsService`, `UserService`, `AdventureAccessService`.
   `AdventureAccessService` is the cross-store coordinator — see
   [`06-security-and-access-control.md` § AdventureAccessService](06-security-and-access-control.md#adventure-access-bridge-adventureaccessservice).
4. Implement `config/SecurityConfig.java` per
   [`06-security-and-access-control.md`](06-security-and-access-control.md#spring-security-wiring):
   `@EnableWebSecurity @EnableMethodSecurity`, role hierarchy bean,
   `BCryptPasswordEncoder`, `VaadinSecurityConfigurer.vaadin()`,
   `NullRequestCache`, single-session, remember-me, login form,
   URL guards, public allow-list.
5. Implement `config/DataInitializer.java` to seed the bootstrap admin
   when the table is empty. **Production rebuild:** read the password
   from a secret instead of hard-coding `admin123`.

### Step 4 — Database wiring

Implement `config/DatabaseConfig.java`:

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.pdg.adventure.server.security.repository")
@EnableMongoRepositories(basePackages = "com.pdg.adventure.server.storage.repository")
public class DatabaseConfig { }
```

Add `server/storage/mongo/MongoDbConfig` (`@EnableMongoAuditing`),
the `UuidIdGenerationMongoEventListener` (HIGHEST_PRECEDENCE), the
`@CascadeSave` / `@CascadeDelete` annotations, the
`CascadeSaveMongoEventListener`, and `CascadeDeleteHelper`. See
[`05-persistence-and-mappers.md` § Cascade save / delete](05-persistence-and-mappers.md#cascade-save).

### Step 5 — MongoDB documents and repositories

1. Implement `model/basic/` (`BasicData`, `DatedData`, `BasicDescriptionData`,
   `DescriptionData`, `CommandDescriptionData`).
2. Implement the documents in `model/`:
   `AdventureData`, `LocationData`, `ItemData`, `ItemContainerData`,
   `DirectionData`, `CommandData`, `CommandChainData`,
   `CommandProviderData`, `MessageData`, `SystemMessageData` (collection
   `systemMessages`; unique compound index `(adventureId, key)`),
   `VocabularyData`, `Word`, `ThingData`,
   `WorkflowData` (a plain embedded field on `AdventureData` — no `@DBRef`,
   no cascade annotations, unlike its siblings; holds `commands` (Processes)
   and `interceptorCommands` (Responses); see
   [`03-domain-model.md` § Workflow](03-domain-model.md#workflow)).
   Apply the cascade annotations exactly as documented (incl.
   `AdventureData.systemMessages`).
3. Add the `*ActionData` (incl. `BreakActionData`) and `*ConditionData`
   subclasses under `model/action/` and `model/condition/`.
4. Add MongoDB repositories under `server/storage/repository/`:
   `AdventureRepository`, `LocationRepository`, `ItemRepository`,
   `MessageRepository`, `WordRepository`, `VocabularyRepository`
   (rename — the current `VocabularyReporitory` typo SHOULD NOT be
   carried over). System messages have **no** dedicated repository — they
   round-trip only via the `AdventureData.systemMessages` `@DBRef` cascade.
5. Add the storage services: `AdventureService`, `ItemService`,
   `MessageService`. Wire `CascadeDeleteHelper` into `AdventureService.deleteAdventure`.
   Add `server/storage/message/SystemMessageKey` (the fixed engine-text
   catalog enum) and `server/support/PlaceholderSpec` (override validation).

### Step 6 — Business objects

Implement runtime BOs in `server/`:

- `Adventure`, `AdventureConfig` (Spring `@Configuration` with the bean
  factories for `allLocations` / `allItems` / `allContainers` / `allWords` /
  `allMessages` / `allVariables`).
- `server/location/{Location, GenericDirection}`.
- `server/tangible/{Thing, Item, GenericContainer, ItemIdentifier}`.
- `server/vocabulary/Vocabulary` (wrapping `VocabularyData`).
- `server/storage/message/MessagesHolder`.
- `server/support/{Variable, VariableProvider, ArticleProvider, DescriptionProvider}`.
- The `api/` interfaces (`Action`, `PreCondition`, `Command`,
  `CommandChain`, `CommandDescription`, `Container`, `Containable`,
  `Wearable`, `Visitable`, `Actionable`, `HasCommands`, `HasLight`,
  `Describable`, `Ided`, `Mapper`, `ExecutionResult`).

### Step 7 — Mapper layer

1. Implement `api/Mapper<DO, BO>`.
2. Create `server/annotation/AutoRegisterMapper` (TYPE-targeted runtime
   annotation) and `AutoMapperRegistrationProcessor` (single
   `BeanPostProcessor`, `@Order(1001)`, queues registrations in
   `postProcessAfterInitialization`, registers them all in
   `postProcessBeforeInitialization` of the `MapperSupporter` bean).
3. Implement `server/support/MapperSupporter` — the registry that holds
   shared collections (vocabulary, mapped items / locations / containers,
   messages, variables) and the `Mapper` map.
4. Implement the mappers in `server/mapper/`:
   `AdventureMapper`, `VocabularyMapper`, `LocationMapper`,
   `DirectionMapper`, `ItemMapper`, `ItemContainerMapper`, `ThingMapper`,
   `CommandMapper`, `CommandChainMapper`, `CommandProviderMapper`,
   `CommandDescriptionMapper`, `DescriptionMapper`. Plus per-type mappers
   under `mapper/action/` and `mapper/condition/`.
5. Annotate every mapper with `@Component @AutoRegisterMapper(priority = 100, …)`.
6. **Finish the bits the current code marks as TODO:** `CommandMapper`
   (full DO ↔ BO including pre-conditions, action, follow-ups),
   destination resolution in `LocationMapper`, item-content mapping in
   `ItemContainerMapper`. See
   [`05-persistence-and-mappers.md` § Known gaps](05-persistence-and-mappers.md#known-gaps).

### Step 8 — Action / condition library

Implement every `Action` and `PreCondition` in `server/action/` and
`server/condition/` per
[`04-runtime-engine.md` § Action catalog](04-runtime-engine.md#action-catalog) and
[`04-runtime-engine.md` § PreCondition catalog](04-runtime-engine.md#precondition-catalog).

Pay attention to:

- The `AbstractAction` / `IdedAction` / `AbstractVariableAction` base
  classes.
- The `AbstractCondition` / `AbstractVariableCondition` base classes.
- The exact failure-message wording (used by tests and surfaced to the
  player).
- Engine messages: `SystemMessageKey` (`server/storage/message/`) is the
  single source of truth for built-in engine text; actions and conditions
  call `SystemMessageKey.SMnn.defaultText().formatted(...)`. Per-adventure
  overrides live sparsely in `AdventureData.systemMessages`
  (`SystemMessageData`), validated for placeholder parity by `PlaceholderSpec`.

### Step 9 — Parser and engine

1. Implement `server/parser/`:
   `Parser` (line → `CommandSequence` of sub-commands, split on `and`/`then`
   `CONJUNCTION` words and `.`; missing-verb inference from the previous
   sub-command; `PRONOUN` `it` → last noun+adjective, else
   `UnresolvedReferenceException`), `CommandSequence`, `CommandHandler` (with
   `examineFallback` support), `CommandExecutor` (pocket → location → reduce
   by adjective → reduce by noun → best-ranked tier → execute; `SM8` / `SM60`
   / `SM61`), `CommandMatcher`, `GenericCommand`, `GenericCommandDescription`,
   `GenericCommandProvider`, `GenericCommandChain`,
   `CommandExecutionResult`, `ExamineFallbackAction`.
2. Implement `server/engine/`:
   `GameContext`, `Workflow` (preCommands and interceptorCommands as
   `TreeMap<CommandDescription, Command>`, iterated in alphabetical
   verb/adjective/noun order), `GameLoop` (`processCommand(String)` runs each
   sub-command in order, stopping at the first failure), `ContainerSupplier`.
   Make `GameContext.tell()` route through an injectable
   `Consumer<String> outputSink` (default `java.lang.IO::println` — JDK 25's
   built-in `java.lang.IO`; there is **no** custom `IO` class), via
   `GameContext.setOutputSink(...)` — this is what lets a Vaadin view capture
   engine output. Note that `GameContext` (and the `AdventureConfig` beans it
   reaches) are ordinary Spring singletons in the current code: there is no
   per-session isolation, so only one Test/Run session is meaningfully active
   at a time server-wide. Preserve this constraint knowingly, or design it
   away — see
   [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps).
3. Implement `server/engine/AdventureRunSession.java` and
   `AdventureRunSessionFactory.java` — the turn-based (`submit(String) →
   RunResult(lines, gameOver)`) wrapper a Vaadin view needs around
   `GameLoop`/`GameContext`, described in
   [`04-runtime-engine.md` § AdventureRunSession](04-runtime-engine.md#adventurerunsession-the-in-browser-play-surface).
   `AdventureRunSessionFactory.registerBaseVerbs` seeds
   `quit`/`describe`/`help`/`inventory` (+ synonyms) **and** `and`/`then`
   (`CONJUNCTION`) and `it` (`PRONOUN`) directly on the vocabulary.
4. Implement `CommandFactory.java` (top-level, in
   `com.pdg.adventure`): the take/drop/wear/look wiring, plus
   `setUpWorkflowCommands` (built-in `help`/`inventory`/`quit`/`describe`
   Responses + the `SM2` "What now?" Process prompt), exactly as
   described in
   [`04-runtime-engine.md` § CommandFactory](04-runtime-engine.md#commandfactory-wiring-conventions).
5. There is **no CLI runner** (`MiniAdventure` / `AdventureClient` were
   removed; `Adventure.run()` is a vestigial stub). The only play path is
   `AdventureRunView` → `AdventureRunSession` (Step 11).

### Step 10 — AI integration (intentional placeholder)

Implement `server/ai/OllamaConfig.java` to bind
`spring.ai.ollama.base-url` to a config property. Wire `DescribeAction`
to use it via dependency injection (drop the hardcoded URL in the
existing `fillThroughAI`). Default to **disabled** — make AI
augmentation a feature flag per Adventure or per Location, not an
inlined branch. See
[`04-runtime-engine.md` § Action catalog](04-runtime-engine.md#action-behavioural-detail)
and
[`01-product-overview.md` § In-scope but explicitly aspirational](01-product-overview.md#in-scope-but-explicitly-aspirational).

### Step 11 — Vaadin views

1. Implement `AdventureBuilderServer.java` with `@SpringBootApplication`
   and `@PWA(name = "Adventure Builder", ...)`, plus a `main` calling
   `SpringApplication.run(...)` (matches the current code).
2. Implement the layouts (`AdventureAppLayout`, the per-area
   `*MainLayout`s) per
   [`07-ui-and-navigation.md` § Layouts](07-ui-and-navigation.md#layouts).
3. Implement the reusable components:
   `BaseEditorView`, `ResetBackSaveView`, `VocabularyPicker`,
   `VocabularyPickerField`, `AdventureGrid`, `GridFactory`,
   `NavigationHelper`. And the support utilities: `ViewSupporter`,
   `RouteIds`, `GridProvider`, `TrackedUsage`.
4. Implement views in this order so each layer can lean on the previous:
   - `RootView`, `LoginView`, `LogoutView`, `AboutView`.
   - `AdminDashboardView`, `AuthorDashboardView`, `PlayerLibraryView`.
   - Adventure: `AdventuresMenuView`, `AdventureEditorView`.
   - Location: `LocationsMenuView`, `LocationEditorView`,
     `LocationMapView`.
   - Item: `ItemsMenuView`, `AllItemsMenuView`, `ItemEditorView`.
   - Direction: `DirectionsMenuView`, `DirectionEditorView`.
   - Command: `CommandsMenuView`, `CommandEditorView`,
     `view/command/action/*`, `view/command/condition/*`
     (annotation-driven `*EditorRegistry` discovery, not a hand-maintained
     switch — see
     [`07-ui-and-navigation.md` § Action editor factory](07-ui-and-navigation.md#action-editor-factory)).
   - Workflow: `WorkflowMainLayout`, then `CommandListEditorView` and its
     two subclasses `WorkflowEditorView` (`…/workflow`, Processes) and
     `ResponsesEditorView` (`…/responses`, Responses) — they reuse the
     Command editor's `PreconditionActionEditor`, so build them after the
     Command views above, not in parallel with them.
   - Message: `MessagesMenuView`, `MessageEditorView`.
   - System messages: `SystemMessagesView` (`…/system-messages`,
     `AdventuresMainLayout`) + `SystemMessageEntry`.
   - Vocabulary: `VocabularyMenuView`, `WordEditorDialogue` (VERB/NOUN/
     ADJECTIVE only in the type picker), `SpecialWordsView`.
   - Admin: `UserManagementView`, `AdventureAssignmentView`.
   - Play surface: `AdventureRunView`, reached three ways (author **Test**
     from `AdventureEditorView`, author/admin "Run Adventure" from
     `AdventuresMenuView`, player "Run Adventure" from
     `PlayerLibraryView`) — one `@Route` (`…/test`) plus one `@RouteAlias`
     (`player/library/:id/run`), both
     `@RolesAllowed({"ROLE_AUTHOR", "ROLE_PLAYER"})`, disambiguated at
     runtime by URL prefix and a `?from=menu` query parameter (see
     [`07-ui-and-navigation.md` § Route × role matrix](07-ui-and-navigation.md#route--role-matrix)).
     Depends on `AdventureRunSessionFactory` from Step 9.
5. Annotate every view with the right `@Route` and `@RolesAllowed` /
   `@AnonymousAllowed` per
   [`07-ui-and-navigation.md` § Route × role matrix](07-ui-and-navigation.md#route--role-matrix).

### Step 12 — Tests

1. Set up the test layout per
   [`08-build-test-and-ops.md` § Tests](08-build-test-and-ops.md#tests),
   mirroring `src/main/java`.
2. Author `TestSupporter` in `server/testhelper/` first; later tests
   should use it rather than ad-hoc fixture construction.
3. Add unit tests for every Action, Condition, mapper, service, parser
   class, and key view-supporter helper. AssertJ-only; no
   `assertEquals`.
4. Add browserless Vaadin tests for each `*EditorView`. Use
   `com.vaadin.browserless.BrowserlessTest`, scope `$()` queries to the
   view, and apply the ComboBox workarounds when interacting with
   pickers.
5. Add the JSON test fixtures: `OneLocation.json`, `QuickAdventure.json`,
   `Vocab.json`.
6. Add `AdventureBuilderTest` /
   `ApplicationTest` for end-to-end coverage with embedded MongoDB.
7. **Do not** introduce `BaseDTO.java` or `BaseRecord.java` PoC test
   classes — they were dead code in the previous repo and should not be
   carried over.

### Step 13 — CI

Add `.github/workflows/build.yml`:

- Trigger on push to `main` (and ideally `pull_request`).
- JDK 25 (`actions/setup-java@v4.5.0`, distribution `temurin`).
- Cache `~/.sonar/cache` and `~/.m2`.
- Run:
  ```
  mvn -B clean verify \
    org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.projectKey=ABS \
    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
  ```
- Plumb the secrets `SONAR_TOKEN`, `SONAR_HOST_URL`.

Optionally add a separate workflow that runs `mvn dependency-check:check`
on a schedule and fails on HIGH/CRITICAL CVEs.

### Step 14 — Manual smoke

1. `mvn spring-boot:run`.
2. Browse to `http://localhost:8080`. Log in as `admin / admin123`.
3. Verify post-login redirect lands on `/admin/dashboard`.
4. Create an author, sign in as the author, create a tiny adventure,
   add a location, vocabulary, items, a direction, and a command.

When all steps pass, you have parity with the current server module.

## Roadmap & known gaps (consolidated)

The chapters carry per-section gap lists. Here they are gathered into one
table with severity and pointer:

| Severity | Gap | Detail |
|----------|-----|--------|
| **Critical** | Hardcoded admin password | [`06-security-and-access-control.md`](06-security-and-access-control.md#known-gaps) — replace with secret-sourced bootstrap. |
| **Critical** | Hardcoded remember-me key | Same — override via env / secret. |
| **High** | `CommandMapper.mapToDO` incomplete | [`05-persistence-and-mappers.md`](05-persistence-and-mappers.md#known-gaps) — `mapToBO` is done; finish the DO direction (preconditions). |
| **High** | `LocationMapper` destination resolution & `ItemContainerMapper` contents | Same. |
| **High** | `VocabularyReporitory` class-name typo | Same. |
| **Medium** | `GameContext`/`AdventureConfig` are process-wide singletons — no per-session engine isolation | [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps) — at most one Test/Run session is meaningfully active server-wide at a time. |
| **Medium** | NLP parser: no prepositions / multi-noun / articles (compound `and`/`then`/`.` and pronoun `it` are now handled) | [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps). |
| **Medium** | Spring AI / Ollama integration commented out, base URL hardcoded | Same. |
| **Medium** | No per-game save state (variables not persisted); `AdventureRunView` sessions don't wire save/load at all | [`03-domain-model.md` § Known gaps](03-domain-model.md#known-gaps), [`02-functional-requirements.md` § Known gaps](02-functional-requirements.md#known-gaps). |
| **Medium** | `AmbiguousCommandException` declared but unused | [`04-runtime-engine.md` § Known gaps](04-runtime-engine.md#known-gaps). |
| **Low** | `LocationMapView` is a non-functional static-image placeholder, not bound to real location data | [`07-ui-and-navigation.md` § Known gaps](07-ui-and-navigation.md#known-gaps). |
| **Low** | Deleting an adventure or an exit skips confirmation (every other delete path confirms) | Same. |
| **Medium** | `UserService.delete` does not block on referencing rows | [`06-security-and-access-control.md` § Known gaps](06-security-and-access-control.md#known-gaps). |
| **Medium** | Cross-store atomicity gap on adventure create | [`05-persistence-and-mappers.md` § Cross-store consistency](05-persistence-and-mappers.md#cross-store-consistency). |
| **Medium** | No password change view | [`06-security-and-access-control.md` § Known gaps](06-security-and-access-control.md#known-gaps). |
| **Medium** | No account locking / failed-login throttling | Same. |
| **Medium** | No audit log | Same. |
| **Medium** | No HTTPS / HSTS enforcement | Same. |
| **Low** | `MessageData.translations / tags / category / notes` not surfaced in editor | [`03-domain-model.md` § Known gaps](03-domain-model.md#known-gaps). |
| **Low** | `ItemContainerData.holdingDirections` flag unused | Same. |
| **Low** | `CommandDescriptionData.setCommandSpecification` bypasses Vocabulary | Same. |
| **Low** | `AdventureService.preProcess`/`postProcess` empty | [`05-persistence-and-mappers.md` § Known gaps](05-persistence-and-mappers.md#known-gaps). |
| **Low** | `CommandsMenuView` uses `AdventuresMainLayout` instead of `CommandMainLayout` | [`07-ui-and-navigation.md` § Known gaps](07-ui-and-navigation.md#known-gaps). |
| **Low** | `VocabularyMenuView` carries a commented `@RouteAlias` | Same. |
| **Low** | Active-link drawer highlighting is JS-injected | Same. |
| **Low** | Mockito 2.23.4 javaagent quirk | [`08-build-test-and-ops.md` § Known gaps](08-build-test-and-ops.md#known-gaps). |
| **Low** | Milestone / pre-release stack pins | Same. |
| **Low** | No Flyway; schema by `ddl-auto=update` | [`05-persistence-and-mappers.md` § Known gaps](05-persistence-and-mappers.md#known-gaps). |
| **Low** | `BaseDTO.java` / `BaseRecord.java` PoC test classes | [`08-build-test-and-ops.md` § Known gaps](08-build-test-and-ops.md#known-gaps). |
| **Low** | `mvn_test.txt` untracked in repo | Same. |
| **Low** | `AGENTS-architecture.md` mentions two BeanPostProcessors at orders 1000 / 1001 — only one exists | [`05-persistence-and-mappers.md` § Mapper subsystem](05-persistence-and-mappers.md#mapper-subsystem). Update or retire that note. |

## Future module split

The current `server` module is a deliberate monolith. The intended split is:

| Future module | Will contain |
|---------------|--------------|
| `backend` | Domain, services, repositories, mappers, runtime engine, security. |
| `editor` | Vaadin views for authors and admins (every `*EditorView`, `*MenuView`, layouts under author/admin). |
| `player` | The play surface and any player-only support. |
| `api` | Shared interfaces and DTOs bridging the above. |

Guidance for the rebuild today, even before the split:

- Avoid coupling editor code to engine internals; route through services.
- Keep `view/` free of direct engine imports beyond what
  `BaseEditorView` already needs (mainly `AdventureService`).
- When placement is genuinely ambiguous, leave a
  `// TODO: Review placement for future module split` comment so the split
  PR can find it.

## Verification checklist

Before declaring the rebuild done, walk this list:

- [ ] `mvn clean verify` is green.
- [ ] JaCoCo report exists at `target/site/jacoco/index.html`.
- [ ] `mvn dependency-check:check` reports no HIGH or CRITICAL CVEs.
- [ ] `docker compose -f adventureDb/Dockerfile.mongodb.yaml up -d` and the
      MySQL equivalent both come up cleanly.
- [ ] `mvn spring-boot:run` exits cleanly with `Ctrl+C`; no stray
      thread / port leaks.
- [ ] Logging in as the seeded admin lands on `/admin/dashboard`.
- [ ] Creating an author, signing in as them, creating an adventure, a
      location, a vocabulary word, an item, a direction, and a command,
      then editing each one again, all work without errors and respect
      the BACK / SAVE / RESET / CANCEL contract.
- [ ] Clicking **Test** on that adventure (once saved and non-empty) opens
      `AdventureRunView` (title `"Test: …"`) and plays through in the browser;
      **Run Adventure** from the adventures list and from a player's library
      both reach the identical view (the library one titled `"Playing: …"`).
- [ ] A typed `take X and drop it.` runs as two sub-commands; **Manage
      Processes** / **Manage Responses** / **Manage System Messages** all
      open from the Adventure Editor.
- [ ] CI workflow on `main` is green.
- [ ] `find server/docs/specs -name '*.md'` lists this 10-document suite
      and every link inside resolves.

## Source pointers

This chapter draws on every other chapter; pointers per section live with
the relevant chapter. The high-leverage sources to keep open while you
rebuild are:

- `src/main/java/com/pdg/adventure/CommandFactory.java`
- `src/main/java/com/pdg/adventure/server/engine/{GameContext,GameLoop,Workflow,AdventureRunSessionFactory}.java`
- `src/main/java/com/pdg/adventure/server/parser/{Parser,CommandSequence,CommandHandler,CommandExecutor}.java`
- `src/main/java/com/pdg/adventure/server/storage/message/SystemMessageKey.java`
- `src/main/java/com/pdg/adventure/server/support/MapperSupporter.java`
- `src/main/java/com/pdg/adventure/server/annotation/AutoMapperRegistrationProcessor.java`
- `src/main/java/com/pdg/adventure/server/security/service/AdventureAccessService.java`
- `src/main/java/com/pdg/adventure/config/{SecurityConfig,DatabaseConfig,DataInitializer}.java`
- `src/main/java/com/pdg/adventure/view/component/{AdventureAppLayout,BaseEditorView,ResetBackSaveView}.java`
- `pom.xml` and `src/main/resources/application.properties`.
